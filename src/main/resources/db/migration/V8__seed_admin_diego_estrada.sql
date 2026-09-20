-- Primer usuario administrador para acceso al panel.
-- Contraseña en claro: admindiego1 (hasheada con BCrypt, strength 10, $2a$).

INSERT INTO user_admin (dpi, full_name, phone, email)
VALUES (
    '1234567891234',
    'Diego Estrada',
    '58589955',
    'diegojoseavilaestrada@gmail.com'
)
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    phone     = VALUES(phone),
    email     = VALUES(email);

INSERT INTO user_account (username, password_hash, dpi)
VALUES (
    'diego.estrada',
    '$2a$10$34.reoNEe0Vakq2VvqTkquIP0VFevdjOlxir9wUKdPsTbt3X2Sw9C',
    '1234567891234'
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    dpi           = VALUES(dpi);
