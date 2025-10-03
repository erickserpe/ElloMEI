-- V1__Initial_Schema.sql
-- Initial migration to establish baseline for existing Hibernate-managed schema
-- This migration is intentionally empty as the schema was already created by Hibernate

-- =====================================================
-- BASELINE MIGRATION FOR EXISTING SCHEMA
-- =====================================================
-- This migration serves as a baseline for the existing database schema
-- that was previously managed by Hibernate with ddl-auto=update.
-- 
-- The actual schema creation was handled by Hibernate JPA annotations:
-- - @Entity classes automatically created tables
-- - @JoinColumn annotations created foreign keys
-- - @Column annotations defined column properties
-- 
-- Starting from this point, all schema changes will be managed by Flyway migrations.
-- =====================================================

-- No SQL statements needed - this is a baseline migration
-- The schema already exists and is managed by spring.flyway.baseline-on-migrate=true
