import flask
import pytest
import pathlib

from banka4_exchange import create_app


@pytest.fixture()
def app(tmpdir: pathlib.Path) -> flask.Flask:
    app = create_app()
    app.config["COMMISSION_RATE"] = 0.1
    app.config["EXCHANGERATE_API_KEY"] = "280153fe0016f484aedcecdd"
    app.config["EXCHANGE_STORAGE_PATH"] = str(tmpdir / "exchanges.json")
    return app
