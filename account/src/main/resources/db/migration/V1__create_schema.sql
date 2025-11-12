-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  number VARCHAR(255),
  type VARCHAR(50),
  initial_amount DOUBLE PRECISION,
  is_active BOOLEAN,
  client_id BIGINT
);

-- Transactions (evitamos la palabra reservada "transaction")
CREATE TABLE IF NOT EXISTS transactions (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  date TIMESTAMP,
  type VARCHAR(50),
  amount DOUBLE PRECISION,
  balance DOUBLE PRECISION,
  account_id BIGINT,
  CONSTRAINT fk_txn_account
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
