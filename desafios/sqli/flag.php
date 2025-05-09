<?php
session_start();
if (!isset($_SESSION['flag_unlocked']) || $_SESSION['flag_unlocked'] !== true) {
    header("Location: index.php");
    exit;
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>NeoBank - Flag</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="flag-container">
        <h1>🏁 Flag Capturada!</h1>
        <p class="flag">FLAG{sql_injection_concluido_com_sucesso}</p>
    </div>
</body>
</html>