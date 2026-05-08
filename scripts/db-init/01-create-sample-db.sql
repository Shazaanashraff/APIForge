-- Create the separate database and user for the Java sample API.
-- This runs inside the postgres container on first startup.

SELECT 'CREATE DATABASE sample_java_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sample_java_db') \gexec

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'sample_java') THEN
    CREATE ROLE sample_java WITH LOGIN PASSWORD 'sample_java_secret';
  END IF;
END $$;

GRANT ALL PRIVILEGES ON DATABASE sample_java_db TO sample_java;
