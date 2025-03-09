from banka4_exchange import create_app
import pytest
import flask

@pytest.fixture()
def app() -> flask.Flask:
    app = create_app()
    app.config["COMMISSION_RATE"] = 0.1
    app.config["EXCHANGERATE_API_KEY"] = "280153fe0016f484aedcecdd"
    return app
