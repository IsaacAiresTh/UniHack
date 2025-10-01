// index.js
const express = require('express');
const { exec } = require('child_process'); // Módulo para executar comandos do sistema
const app = express();
const port = 80;

app.use(express.urlencoded({ extended: true })); // Para processar dados de formulários

const htmlTemplate = (result = '') => `
    <!DOCTYPE html>
    <html lang="pt-BR">
    <head>
        <meta charset="UTF-8">
        <title>UniHack | Ferramenta de Diagnóstico de Rede</title>
        <style>
            body { font-family: monospace; background-color: #111; color: #0f0; text-align: center; margin-top: 50px; }
            .container { background-color: #000; border: 1px solid #0f0; padding: 40px; border-radius: 8px; display: inline-block; }
            input[type=text] { padding: 10px; width: 300px; border-radius: 4px; border: 1px solid #0f0; background-color: #222; color: #0f0; }
            button { padding: 10px 20px; background-color: #0f0; color: #000; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
            pre { background-color: #000; text-align: left; padding: 15px; border-radius: 5px; white-space: pre-wrap; word-wrap: break-word; }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>Diagnóstico de Ping</h1>
            <form action="/" method="POST">
                <input type="text" name="ip" placeholder="Digite um endereço IP (ex: 8.8.8.8)" autocomplete="off" required />
                <button type="submit">Pingar</button>
            </form>
            ${result ? `<h3>Resultado:</h3><pre>${result}</pre>` : ''}
        </div>
    </body>
    </html>
`;

app.get('/', (req, res) => {
    res.send(htmlTemplate());
});

app.post('/', (req, res) => {
    const ip = req.body.ip;

    if (!ip) {
        return res.send(htmlTemplate('Por favor, forneça um endereço IP.'));
    }

    // VULNERABILIDADE: A entrada do usuário é concatenada diretamente no comando!
    const command = `ping -c 3 ${ip}`; 

    exec(command, (error, stdout, stderr) => {
        if (error) {
            // Mesmo que dê erro, mostramos a saída, pois ela pode ser útil para o atacante.
            res.send(htmlTemplate(error.message));
            return;
        }
        res.send(htmlTemplate(stdout + stderr));
    });
});

app.listen(port, () => {
    console.log(`Desafio de Command Injection rodando na porta ${port}`);
});