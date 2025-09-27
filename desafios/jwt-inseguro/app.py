# app.py
from flask import Flask, request, jsonify
import jwt
import datetime

app = Flask(__name__)

# ATENÇÃO: Segredo fraco e conhecido! Esta é a vulnerabilidade.
app.config['SECRET_KEY'] = 'segredo'

@app.route('/login', methods=['POST'])
def login():
    # Em um app real, você validaria username e password aqui.
    # Para o desafio, qualquer um pode "logar" para receber um token.
    username = request.json.get('username', 'guest')
    
    token = jwt.encode({
        'user': username,
        'role': 'user',
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    }, app.config['SECRET_KEY'], algorithm='HS256')
    
    return jsonify({'token': token})

@app.route('/admin')
def admin():
    token = request.headers.get('Authorization')
    if not token:
        return jsonify({'message': 'Token não fornecido!'}), 401
        
    try:
        # Tenta decodificar o token com o mesmo segredo fraco
        data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])
        
        # Verifica se o usuário tem a role de 'admin'
        if data.get('role') == 'admin':
            return jsonify({'message': 'Acesso concedido!', 'flag': 'FLAG{jwt_forjado_com_sucesso}'})
        else:
            return jsonify({'message': 'Acesso negado. Apenas administradores.'}), 403
            
    except jwt.ExpiredSignatureError:
        return jsonify({'message': 'Token expirado!'}), 401
    except jwt.InvalidTokenError:
        return jsonify({'message': 'Token inválido!'}), 401

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)