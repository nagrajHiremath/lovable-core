#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  CREATE DATABASE account_db;
  CREATE DATABASE workspace_db;
  CREATE DATABASE intelligence_db;

  CREATE USER account_user WITH PASSWORD '$ACCOUNT_DB_PASSWORD';
  CREATE USER workspace_user WITH PASSWORD '$WORKSPACE_DB_PASSWORD';
  CREATE USER intelligence_user WITH PASSWORD '$INTELLIGENCE_DB_PASSWORD';

  GRANT ALL PRIVILEGES ON DATABASE account_db TO account_user;
  GRANT ALL PRIVILEGES ON DATABASE workspace_db TO workspace_user;
  GRANT ALL PRIVILEGES ON DATABASE intelligence_db TO intelligence_user;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "account_db" <<-EOSQL
  GRANT ALL ON SCHEMA public TO account_user;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "workspace_db" <<-EOSQL
  GRANT ALL ON SCHEMA public TO workspace_user;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "intelligence_db" <<-EOSQL
  GRANT ALL ON SCHEMA public TO intelligence_user;
EOSQL

