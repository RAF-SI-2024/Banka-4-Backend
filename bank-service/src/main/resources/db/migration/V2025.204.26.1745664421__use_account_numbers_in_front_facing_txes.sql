-- Create new columns for the ID.
ALTER TABLE transactions ADD COLUMN from_account VARCHAR(255);
ALTER TABLE transactions ADD COLUMN to_account VARCHAR(255);

-- Populate them.
UPDATE transactions
       SET from_account = (SELECT account_number FROM accounts
                                  WHERE id = from_account_id);
UPDATE transactions
       SET to_account = (SELECT account_number FROM accounts
                                WHERE id = to_account_id);

-- Make them mandatory.
ALTER TABLE transactions ALTER COLUMN from_account SET NOT NULL;
ALTER TABLE transactions ALTER COLUMN to_account SET NOT NULL;

-- Drop constraints.
ALTER TABLE transactions DROP CONSTRAINT fk_transactions_from_accounts;
ALTER TABLE transactions DROP CONSTRAINT fk_transactions_to_accounts;

-- Drop old columns.
ALTER TABLE transactions DROP COLUMN from_account_id;
ALTER TABLE transactions DROP COLUMN to_account_id;
