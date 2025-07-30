-- Database initialization script for Personal Blog
-- This script will be executed when the PostgreSQL container starts

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS personal_blog;

-- Connect to the database
\c personal_blog;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- Create indexes for full-text search
-- These will be created by Hibernate, but we can prepare the database

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE personal_blog TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- Insert initial data (optional)
-- This can be used for seeding the database with initial admin user, categories, etc.

COMMIT;