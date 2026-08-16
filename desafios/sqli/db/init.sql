-- O entrypoint do MySQL já cria este banco a partir de MYSQL_DATABASE=sqli no
-- compose, então aqui basta selecioná-lo.
USE sqli;

CREATE TABLE IF NOT EXISTS logins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(50),
    pass VARCHAR(50)
);

INSERT INTO logins (user, pass) VALUES ('admin', 'admin123');
