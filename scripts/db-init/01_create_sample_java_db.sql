-- Creates the database and user for the Java sample API.
-- This script runs automatically when the Postgres container first starts.

CREATE USER sample_java WITH PASSWORD 'sample_java_secret';
CREATE DATABASE sample_java_db OWNER sample_java;
GRANT ALL PRIVILEGES ON DATABASE sample_java_db TO sample_java;
