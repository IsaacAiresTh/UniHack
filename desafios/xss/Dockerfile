# Usar uma imagem oficial do Node.js
FROM node:18-alpine

# Criar e definir o diretório de trabalho dentro do contêiner
WORKDIR /usr/src/app

# Copiar os arquivos de dependências
COPY package*.json ./

# Instalar as dependências do projeto
RUN npm install

# Copiar o restante dos arquivos da aplicação
COPY . .

# A aplicação vai rodar na porta 80
EXPOSE 80

# Comando para iniciar o servidor
CMD [ "node", "index.js" ]