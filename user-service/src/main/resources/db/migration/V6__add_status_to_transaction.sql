DO $$
    BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='transactions' AND column_name='status') THEN
        ALTER TABLE transactions
            ADD COLUMN status VARCHAR(255) NOT NULL;
    END IF;
END $$;