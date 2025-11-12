-- Cuentas según el caso (IDs fijos para relacionar transacciones)
INSERT INTO accounts (number, type, initial_amount, is_active, client_id) VALUES
('478758', 'Ahorro',    2000.00, TRUE, 1),  -- Jose Lema
('225487', 'Corriente',  100.00, TRUE, 2),  -- Marianela Montalvo
('495878', 'Ahorros',     0.00, TRUE, 3),   -- Juan Osorio
('496825', 'Ahorros',   540.00, TRUE, 2),   -- Marianela Montalvo
('585545', 'Corriente', 1000.00, TRUE, 1);  -- Nueva de Jose Lema

-- Movimientos (Transaction). Usa las fechas del reto y balances calculados
INSERT INTO transactions (date, type, amount, balance, account_id) VALUES
(TIMESTAMP '2022-02-05 00:00:00', 'RETIRO',   -575.00, 1425.00, 1), -- 2000 - 575
(TIMESTAMP '2022-02-10 00:00:00', 'DEPOSITO',  600.00,  700.00, 2), -- 100 + 600
(TIMESTAMP '2022-02-09 00:00:00', 'DEPOSITO',  150.00,  150.00, 3), -- 0 + 150
(TIMESTAMP '2022-02-08 00:00:00', 'RETIRO',   -540.00,    0.00, 4), -- 540 - 540
(TIMESTAMP '2022-02-15 00:00:00', 'DEPOSITO', 200.00, 1625.00, 1); -- 1425 + 200
