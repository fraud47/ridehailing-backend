ALTER TABLE drivers
    ADD COLUMN blocked BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE drivers
    ADD COLUMN blocked_reason VARCHAR(255);

ALTER TABLE drivers
    ADD COLUMN blocked_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE rides
    ADD COLUMN platform_commission NUMERIC(10, 2) NOT NULL DEFAULT 0.00;

CREATE TABLE platform_accounts (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    account_key VARCHAR(100) NOT NULL UNIQUE,
    available_balance NUMERIC(12, 2) NOT NULL,
    total_commission_earned NUMERIC(12, 2) NOT NULL,
    total_withdrawn NUMERIC(12, 2) NOT NULL
);

CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    owner_type VARCHAR(30) NOT NULL,
    owner_id UUID NOT NULL,
    balance NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    CONSTRAINT uq_wallets_owner UNIQUE (owner_type, owner_id)
);

CREATE TABLE ride_chat_messages (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ride_id UUID NOT NULL,
    sender_role VARCHAR(30) NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    CONSTRAINT fk_ride_chat_messages_ride FOREIGN KEY (ride_id) REFERENCES rides (id)
);

CREATE INDEX idx_ride_chat_messages_ride_id_created_at
    ON ride_chat_messages (ride_id, created_at);
