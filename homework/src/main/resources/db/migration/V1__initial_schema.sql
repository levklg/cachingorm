CREATE SEQUENCE hibernate_sequence;

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(255)
);

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    address_id BIGINT,
    FOREIGN KEY (address_id) REFERENCES addresses(id)
);

CREATE TABLE phones (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(255),
    client_id BIGINT,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);
