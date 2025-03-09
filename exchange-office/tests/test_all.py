import pytest


def test_eur_buy_over_sell(app):
    with app.test_client() as client:
        response = client.get("/exchange-rate")
        assert response.status_code == 200
        data = response.get_json()
        eur_rate = data["exchanges"]["EUR"]
        assert eur_rate is not None
        assert eur_rate["Buy"] < eur_rate["Sell"]


def test_eur_neutral_above_1(app):
    with app.test_client() as client:
        response = client.get("/exchange-rate")
        assert response.status_code == 200
        data = response.get_json()
        eur_rate = data["exchanges"].get("EUR")
        assert eur_rate is not None
        assert eur_rate["Neutral"] > 1
