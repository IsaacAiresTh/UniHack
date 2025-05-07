<?php
session_start();
?>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>NeoBank | Área Restrita</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="login-container">
        <h1>NeoBank</h1>
        <h2>Acesso Restrito</h2>
        <form method="GET" action="">
            <input type="text" name="username" placeholder="Usuário" required>
            <input type="password" name="password" placeholder="Senha" required>
            <button type="submit">Entrar</button>
        </form>

        <?php
        if (isset($_GET['username']) && isset($_GET['password'])) {
            $host = 'sqli-db';
            $user = 'root';
            $pass = 'root';
            $db = 'sqli';

            $conn = new mysqli($host, $user, $pass, $db);

            if ($conn->connect_error) {
                die("<p class='error'>Erro ao conectar ao banco</p>");
            }

            $username = $_GET['username'];
            $password = $_GET['password'];

            $query = "SELECT * FROM users WHERE username = '$username' AND password = '$password'";
            $result = $conn->query($query);

            if ($result && $result->num_rows > 0) {
                $_SESSION['flag_unlocked'] = true;
                header("Location: flag.php");
                exit;
            } else {
                echo "<p class='error'>Login falhou</p>";
            }

            $conn->close();
        }
        ?>
    </div>
</body>
</html>
