import contextlib
import fcntl
import json
import logging
import os
from datetime import datetime, timezone
from time import sleep, time

import requests
from flask import Flask

__doc__ = "Exchange office rates caching API"
__version__ = "0.1"

exchanges_path = "./exchanges.json"
secrets_path = "./secrets.json"
config_path = "./config.json"
currencies = ["USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF"]
commission = json.loads(open(config_path).read())["commission"]
key = json.loads(open(secrets_path).read())["key"]
url = f"https://v6.exchangerate-api.com/v6/{key}/latest/RSD"

app = Flask(__name__)


@app.after_request
def set_default_content_type(response):
    response.headers["Content-Type"] = "application/json"
    return response


app.logger.setLevel(logging.INFO)


@contextlib.contextmanager
def file_lock(filename: str):
    fd = os.open(filename, os.O_RDWR | os.O_CREAT)
    # Relased when the FD gets closed.
    fcntl.flock(fd, fcntl.LOCK_EX)
    with os.fdopen(fd, "w+"):
        yield


def call_exchanges_api():
    ok = False
    retries = 0
    max_retry = 4
    while not ok and retries < max_retry:
        response = requests.get(url)
        ok = response.status_code == 200
        if not ok:
            sleep(5000)
            retries += 1
    if response.status_code != 200:
        raise Exception("Network error")
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
            "Buy": 1 / rate * (1 - commission),
            "Neutral": 1 / rate,
            "Sell": 1 / rate * (1 + commission),
        }
        for currency, rate in neutral_rates.items()
    }

    table = json.dumps(
        {
            "lastUpdatedISO8061withTimezone": last_update_iso_time,
            "lastUpdatedUnix": time_last_update_unix,
            "nextUpdateISO8061withTimezone": next_update_iso_time,
            "nextUpdateUnix": time_next_update_unix,
            "lastLocalUpdate": int(time()),
            "exchanges": exchanges,
        }
    )
    tmp_file = exchanges_path + ".tmp"
    open(tmp_file, "w").write(table)
    os.replace(tmp_file, exchanges_path)


def is_old():
    table = json.loads(open(exchanges_path).read())
    return (
        time() - table["lastLocalUpdate"] > 2 * 60 * 60
        and table["nextUpdateUnix"] < time() - 300
    )
    # api refreshes every 24h
    # 2h test as to not spam accidentally
    # 5min lag to API as to not spam


def should_remake():
    return not os.path.exists(exchanges_path) or is_old()


@app.route("/exchange-rate", methods=["GET"])
def get_exchange_table():
    if should_remake():
        with file_lock(".refresh-task.lck"):
            if should_remake():
                app.logger.info("remaking exchanges table")
                make_exchange_table(call_exchanges_api())
                app.logger.info("spent 1 api token (out of 1500 monthly)")
    return open(exchanges_path).read()


if __name__ == "__main__":
    app.run()
