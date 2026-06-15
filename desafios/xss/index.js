// index.js (VERSÃO CORRIGIDA)
const express = require('express');
const app = express();
const port = 80;

app.get('/', (req, res) => {
    const searchTerm = req.query.search || '';

    // A flag agora está escondida em um atributo 'data-flag' de um elemento invisível.
    const body = `
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>UniHack | Busca Interna</title>
            <style>
                body { font-family: sans-serif; background-color: #f0f2f5; color: #333; text-align: center; margin-top: 50px; }
                .container { background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); display: inline-block; }
                input[type=text] { padding: 10px; width: 300px; border-radius: 4px; border: 1px solid #ddd; }
                button { padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
                .result { margin-top: 20px; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Pesquisa de Documentos</h1>
                <form method="GET">
                    <input type="text" name="search"  placeholder="Digite seu termo de busca..." required autocomplete="off">
                    <button type="submit">Buscar</button>
                </form>
                <div class="result">
                    ${searchTerm ? `<p>Resultados para: ${searchTerm}</p>` : ''}
                </div>
            </div>

            <div id="flag-holder" data-flag="FLAG{xss_dom_exploitation_ftw}" style="display: none;"></div>
        </body>
        </html>
    `;
    
    res.send(body);
});

app.listen(port, () => {
    console.log(`Desafio de XSS (v2) rodando na porta ${port}`);
});