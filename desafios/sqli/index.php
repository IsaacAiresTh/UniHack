<?php
session_start();

if (isset($_GET['user']) && isset($_GET['pass'])) {
    $conn = new mysqli("sqli-db", "root", "root", "sqli");

    if ($conn->connect_error) {
        die("<p class='error'>Erro ao conectar</p>");
    }

    $user = $_GET['user'];
    $pass = $_GET['pass'];

    $query = "SELECT * FROM logins WHERE user = '$user' AND pass = '$pass'";
    $result = $conn->query($query);

    if ($result && $result->num_rows > 0) {
        $_SESSION['flag_unlocked'] = true;
        header("Location: flag.php");
        exit; // Certifique-se de que o script termina aqui
    } else {
        echo "<p class='error'>Credenciais inválidas.</p>";
    }

    $conn->close();
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>NeoBank | Acesso Restrito</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="login-container">
        <h1>NeoBank</h1>
        <h2>Top-Secret</h2>
        <form method="GET" action="">
            <input type="text" name="user" placeholder="Usuário" required>
            <input type="password" name="pass" placeholder="Senha" required>
            <button type="submit">Entrar</button>
        </form>
    </div>
</body>
</html>
