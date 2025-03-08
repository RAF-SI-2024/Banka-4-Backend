-- Drop existing foreign keys
ALTER TABLE accounts DROP CONSTRAINT fk_accounts_on_client;
ALTER TABLE accounts DROP CONSTRAINT fk_accounts_on_employee;
ALTER TABLE client_contacts DROP CONSTRAINT fk_clicon_on_client;
ALTER TABLE companies DROP CONSTRAINT fk_companies_on_majorityowner;

-- Convert character varying ID columns to UUID
ALTER TABLE clients ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE tokens ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE client_contacts ALTER COLUMN client_id TYPE UUID USING client_id::uuid;
ALTER TABLE accounts ALTER COLUMN employee_id TYPE UUID USING employee_id::uuid;
ALTER TABLE accounts ALTER COLUMN client_id TYPE UUID USING client_id::uuid;
ALTER TABLE companies ALTER COLUMN majority_owner_id TYPE UUID USING majority_owner_id::uuid;
ALTER TABLE employees ALTER COLUMN id TYPE UUID USING id::uuid;  -- Dodajemo konverziju employees.id

-- Re-add foreign keys with correct types
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_on_client FOREIGN KEY (client_id) REFERENCES clients(id);
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_on_employee FOREIGN KEY (employee_id) REFERENCES employees(id);
ALTER TABLE client_contacts ADD CONSTRAINT fk_clicon_on_client FOREIGN KEY (client_id) REFERENCES clients(id);
ALTER TABLE companies ADD CONSTRAINT fk_companies_on_majorityowner FOREIGN KEY (majority_owner_id) REFERENCES clients(id);
