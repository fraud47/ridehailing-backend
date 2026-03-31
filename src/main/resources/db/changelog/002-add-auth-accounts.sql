ALTER TABLE riders
    ALTER COLUMN phone_number DROP NOT NULL;

CREATE TABLE auth_accounts (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    rider_id UUID,
    CONSTRAINT uq_auth_accounts_provider_subject UNIQUE (provider, provider_subject),
    CONSTRAINT fk_auth_accounts_rider FOREIGN KEY (rider_id) REFERENCES riders (id)
);

CREATE INDEX idx_auth_accounts_email_address ON auth_accounts (email_address);
