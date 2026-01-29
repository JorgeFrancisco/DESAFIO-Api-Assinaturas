-- V1__init.sql
-- Domínio: usuários, assinaturas, planos (regras em código), preços versionados por vigência.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE app_user (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(200) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE plan (
  code VARCHAR(32) PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE plan_price (
  id UUID PRIMARY KEY,
  plan_code VARCHAR(32) NOT NULL REFERENCES plan(code),
  price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  valid_from TIMESTAMPTZ NOT NULL,
  valid_to TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Apenas 1 preço vigente (valid_to is null) por plano
CREATE UNIQUE INDEX ux_plan_price_current ON plan_price(plan_code) WHERE valid_to IS NULL;

CREATE TABLE subscription (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_user(id),
  plan_code VARCHAR(32) NOT NULL REFERENCES plan(code),
  status VARCHAR(20) NOT NULL,
  start_date DATE NOT NULL,
  expiration_date DATE NOT NULL,
  failed_attempts INT NOT NULL DEFAULT 0,
  next_retry_at TIMESTAMPTZ NULL,
  last_attempt_at TIMESTAMPTZ NULL,
  last_charged_amount NUMERIC(10,2) NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 1 assinatura ATIVA por usuário (garantia forte no banco)
CREATE UNIQUE INDEX ux_subscription_one_active_per_user
  ON subscription(user_id)
  WHERE status = 'ACTIVE';

-- Índices úteis pro scheduler
CREATE INDEX ix_subscription_due ON subscription(status, expiration_date);
CREATE INDEX ix_subscription_next_retry ON subscription(next_retry_at);

CREATE TABLE payment_attempt (
  id UUID PRIMARY KEY,
  subscription_id UUID NOT NULL REFERENCES subscription(id),
  attempt_no INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  error_message VARCHAR(500) NULL,
  charged_amount NUMERIC(10,2) NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Seeds: planos + preço inicial vigente
INSERT INTO plan(code, name, active) VALUES
  ('BASIC', 'Básico', true),
  ('PREMIUM', 'Premium', true),
  ('FAMILY', 'Família', true);

INSERT INTO plan_price(id, plan_code, price, valid_from, valid_to)
VALUES
  (uuid_generate_v4(), 'BASIC', 19.90, now(), NULL),
  (uuid_generate_v4(), 'PREMIUM', 39.90, now(), NULL),
  (uuid_generate_v4(), 'FAMILY', 59.90, now(), NULL);
