DO $$
BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='currencies' AND column_name='version') THEN
ALTER TABLE currencies
    ADD version BIGINT;
END IF;
END $$;