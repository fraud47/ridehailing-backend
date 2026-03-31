CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    owner_type VARCHAR(30) NOT NULL,
    owner_id UUID NOT NULL,
    balance NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    CONSTRAINT uq_wallets_owner UNIQUE (owner_type, owner_id)
);

INSERT INTO wallets (id, owner_type, owner_id, balance, currency)
SELECT id, 'RIDER', id, wallet_balance, 'USD'
FROM riders;

INSERT INTO wallets (id, owner_type, owner_id, balance, currency)
SELECT id, 'DRIVER', id, wallet_balance, 'USD'
FROM drivers;
