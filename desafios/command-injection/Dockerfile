# Usar uma imagem oficial do Node.js
FROM node:18-alpine

# ---> ADICIONE ESTA LINHA <---
# Instalar ferramentas de rede (incluindo ping)
RUN apk add --no-cache iputils

# Criar e definir o diretório de trabalho
WORKDIR /usr/src/app

# Criar o arquivo da flag
RUN echo "FLAG{o_ponto_e_virgula_eh_poderoso}" > /flag.txt

# Copiar os arquivos de dependências
COPY package*.json ./

# Instalar as dependências
RUN npm install

# Copiar o restante dos arquivos da aplicação
COPY . .

# A aplicação vai rodar na porta 80
EXPOSE 80

# Comando para iniciar o servidor
CMD [ "node", "index.js" ]