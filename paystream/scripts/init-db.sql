-- Create one database per service (each service has its own DB)
-- This file runs automatically when Postgres first starts
 
CREATE DATABASE auth_db;
CREATE DATABASE payment_db;
CREATE DATABASE transaction_db;
CREATE DATABASE ledger_db;
 
-- Grant all permissions to the paystream user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO paystream;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO paystream;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO paystream;
GRANT ALL PRIVILEGES ON DATABASE ledger_db TO paystream;


