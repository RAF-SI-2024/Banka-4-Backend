import contextlib
import fcntl
import json
import logging
import os
from datetime import datetime, timezone
from time import sleep, time

import requests
from flask import Blueprint, Flask, current_app, g, send_file

__doc__ = "Exchange office rates caching API"
__version__ = "0.1"

exchanges_path = "./exchanges.json"
currencies = ["USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF"]

root_bp = Blueprint("root", __name__)


@root_bp.before_request
def load_and_validate_config():
    commission = current_app.config.get("COMMISSION_RATE")
    if not isinstance(commission, float):
        raise RuntimeError("COMMISION_RATE must be a float")
    if not (0 <= commission <= 1):
        raise RuntimeError("COMMISION_RATE must be [0, 1]")
    g.commission = commission

    api_key = current_app.config.get("EXCHANGERATE_API_KEY")
    if not isinstance(api_key, str):
        raise RuntimeError("EXCHANGERATE_API_KEY must be a float")
    g.api_url = f"https://v6.exchangerate-api.com/v6/{api_key}/latest/RSD"


@contextlib.contextmanager
def file_lock(filename: str):
    fd = os.open(filename, os.O_RDWR | os.O_CREAT)
    # Relased when the FD gets closed.
    fcntl.flock(fd, fcntl.LOCK_EX)
    with os.fdopen(fd, "w+"):
        yield


class ExchangeRateFetchFailedError(RuntimeError):
    """Raised when the exchange rate API is unreachable"""

    pass


@root_bp.errorhandler(ExchangeRateFetchFailedError)
def exchange_rate_fetch_error(e: ExchangeRateFetchFailedError):
    # Formatted per user-service convention.
    return dict(failed=True, code="ExchangeRateFetchFailed"), 503


def call_exchanges_api():
    ok = False
    retries = 0
    max_retry = 4
    while not ok and retries < max_retry:
        response = requests.get(g.api_url)
        ok = response.status_code == 200
        if not ok:
            sleep(5000)
            retries += 1
    if not ok:
        raise ExchangeRateFetchFailedError()
    return response.json()


def make_exchange_table(api_response):
    time_last_update_unix = api_response["time_last_update_unix"]
    time_next_update_unix = api_response["time_next_update_unix"]

    utc_time = datetime.fromtimestamp(time_last_update_unix).replace(
        tzinfo=timezone.utc
    )
    last_update_iso_time = utc_time.isoformat()

    utc_time = datetime.fromtimestamp(time_next_update_unix).replace(
        tzinfo=timezone.utc
    )
    next_update_iso_time = utc_time.isoformat()

    neutral_rates = {
        currency: api_response["conversion_rates"][currency] for currency in currencies
    }
    exchanges = {
        currency: {
            "Base": currency,
            "Quote": "RSD",
            "Buy": 1 / rate * (1 - g.commission),
            "Neutral": 1 / rate,
            "Sell": 1 / rate * (1 + g.commission),
        }
        for currency, rate in neutral_rates.items()
    }

    tmp_file = exchanges_path + ".tmp"
    with open(tmp_file, "w") as f:
        json.dump(
            {
                "lastUpdatedISO8061withTimezone": last_update_iso_time,
                "lastUpdatedUnix": time_last_update_unix,
                "nextUpdateISO8061withTimezone": next_update_iso_time,
                "nextUpdateUnix": time_next_update_unix,
                "lastLocalUpdate": int(time()),
                "exchanges": exchanges,
            },
            f,
        )
    os.replace(tmp_file, exchanges_path)


def is_old():
    with open(exchanges_path) as f:
        table = json.load(f)
    return (
        time() - table["lastLocalUpdate"] > 2 * 60 * 60
        and table["nextUpdateUnix"] < time() - 300
    )
    # api refreshes every 24h
    # 2h test as to not spam accidentally
    # 5min lag to API as to not spam


def should_remake():
    return not os.path.exists(exchanges_path) or is_old()


@root_bp.get("/exchange-rate")
def get_exchange_table():
    if should_remake():
        with file_lock(".refresh-task.lck"):
            if should_remake():
                current_app.logger.info("remaking exchanges table")
                make_exchange_table(call_exchanges_api())
                current_app.logger.info("spent 1 api token (out of 1500 monthly)")
    return send_file(exchanges_path)


def create_app():
    app = Flask(__name__, instance_relative_config=True)
    app.logger.setLevel(logging.INFO)
    app.config.from_pyfile("config.py", silent=True)
    app.register_blueprint(root_bp)
    return app


if __name__ == "__main__":
    app.run()
