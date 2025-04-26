-- Transactions have gained an executingTransaction field.
ALTER TABLE transactions
            ADD COLUMN executing_transaction_routing_number BIGINT;
ALTER TABLE transactions
            ADD COLUMN executing_transaction_id VARCHAR(255);
