-- Inserindo algumas categorias de despesa padrão

INSERT INTO categoria_despesa(nome) VALUES ('Moradia');

INSERT INTO categoria_despesa(nome) VALUES ('Supermercado');

INSERT INTO categoria_despesa(nome) VALUES ('Fornecedores');

INSERT INTO categoria_despesa(nome) VALUES ('Transporte');

INSERT INTO categoria_despesa(nome) VALUES ('Pessoal');

INSERT INTO categoria_despesa(nome) VALUES ('Impostos');



-- Inserindo algumas contas iniciais com saldo zero

INSERT INTO conta(nome_conta, tipo, saldo_inicial, saldo_atual) VALUES ('Caixa Físico', 'Caixa', 0.0, 0.0);

INSERT INTO conta(nome_conta, tipo, saldo_inicial, saldo_atual) VALUES ('Sicoob', 'Conta Corrente', 0.0, 0.0);

INSERT INTO conta(nome_conta, tipo, saldo_inicial, saldo_atual) VALUES ('Sicredi', 'Conta Corrente', 0.0, 0.0);

-- Criando o primeiro usuário 'erick' com a senha '5522' criptografada
-- ATENÇÃO: A senha NUNCA é guardada em texto puro. Este código abaixo é a senha '5522' já criptografada.
INSERT INTO usuario (username, password, nome_completo, roles) VALUES ('erick', '$2a$10$3g5vJ0pBCx23kDUgPBz84.w881xV43G6k54TRd5Xl7Nlp.b.v598e', 'Erick Luan', 'USER');