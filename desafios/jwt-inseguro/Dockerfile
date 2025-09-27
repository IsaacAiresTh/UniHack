# Usar uma imagem oficial e leve do Python
FROM python:3.9-alpine

# Definir o diretório de trabalho
WORKDIR /app

# Copiar o arquivo de dependências
COPY requirements.txt .

# Instalar as dependências
RUN pip install --no-cache-dir -r requirements.txt

# Copiar o restante dos arquivos da aplicação
COPY . .

# A aplicação vai rodar na porta 80
EXPOSE 80

# Comando para iniciar o servidor Flask
CMD ["python", "app.py"]