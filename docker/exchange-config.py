import os

COMMISSION_RATE = 0.01
EXCHANGERATE_API_KEY = os.getenv("EXCHANGERATE_API_KEY")

if not EXCHANGERATE_API_KEY:
    raise RuntimeError("EXCHANGERATE_API_KEY not provided (no env var)")

EXCHANGE_STORAGE_PATH = "/data/exchanges.json"
LOCKFILE_PATH = "/work/.refresh-task.lck"
