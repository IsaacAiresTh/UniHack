CREATE DATABASE sqli;
USE sqli;

CREATE TABLE logins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(50),
    pass VARCHAR(50)
);

INSERT INTO logins (user, pass) VALUES ('admin', 'admin123');
