CREATE TABLE customers (
    id UUID PRIMARY KEY,
    customer_number VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    address VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_customers_customer_number
        UNIQUE (customer_number),

    CONSTRAINT uk_customers_email
        UNIQUE (email),

    CONSTRAINT chk_customers_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_customers_last_name
    ON customers (last_name);

CREATE INDEX idx_customers_status
    ON customers (status);