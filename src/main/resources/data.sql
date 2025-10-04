-- Script para popular roles iniciais no sistema
-- Este script é executado automaticamente pelo Spring Boot na inicialização

-- Insere as roles padrão do sistema
-- ROLE_USER: Usuário comum do sistema
-- ROLE_ADMIN: Administrador com permissões especiais

INSERT IGNORE INTO role (nome) VALUES ('ROLE_USER');
INSERT IGNORE INTO role (nome) VALUES ('ROLE_ADMIN');
