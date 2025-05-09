FROM php:8.1-apache

# Copia os arquivos do app para o diretório padrão do Apache
COPY . /var/www/html/

# Dá permissões corretas (opcional)
RUN chown -R www-data:www-data /var/www/html

# Ativa extensões se necessário (por exemplo, mysqli)
RUN docker-php-ext-install mysqli

EXPOSE 80
