# Use an official Node.js runtime as a parent image
FROM node:18-alpine

# Set the working directory to /app inside the container
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install any dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the application
RUN npm run build

# Use a production-ready server as the base image
FROM nginx:stable-alpine

# Copy the built application to the Nginx default location
COPY --from=0 /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]