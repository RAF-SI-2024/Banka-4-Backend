import flask
import pytest
import pathlib
import os.path as path

from banka4_exchange import create_app

@pytest.fixture(autouse=True)
def disable_remake(monkeypatch):
    monkeypatch.setattr("banka4_exchange.should_remake", lambda: False)

@pytest.fixture()
def app() -> flask.Flask:
    app = create_app()
    app.config["COMMISSION_RATE"] = 0.1
    app.config["EXCHANGERATE_API_KEY"] = "WRONGAPIKEY"
    project_root = path.abspath(path.join(path.dirname(__file__), ".."))
    app.config["EXCHANGE_STORAGE_PATH"] = path.join(project_root, "tests/test_exchange.json")
    return app
