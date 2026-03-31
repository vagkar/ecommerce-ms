#!/bin/bash
# Creates all three service databases and their dedicated users in a single PostgreSQL instance.
# Postgres executes every *.sh file in /docker-entrypoint-initdb.d/ on first startup.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER product WITH PASSWORD 'product';
    CREATE DATABASE productdb OWNER product;

    CREATE USER "order" WITH PASSWORD 'order';
    CREATE DATABASE orderdb OWNER "order";

    CREATE USER "user" WITH PASSWORD 'user';
    CREATE DATABASE userdb OWNER "user";
EOSQL