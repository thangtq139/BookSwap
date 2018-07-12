import sys
import json
import shelf
from flask import Flask, jsonify, make_response, request;

app = Flask(__name__)

@app.route('/book/get', methods = ['GET'])
def get_book():
    data = shelf.get_books()
    return jsonify(data), 200

@app.route('/user/give', methods = ['POST'])
def user_send_book():
    if not request.json or not 'user' in request.json or not 'book_name' in request.json or not 'book_info' in request.json:
        abort(400)

    book_id = shelf.add_new_book(request.json)
    if book_id is None:
        return jsonify({'URI': 'NULL'}), 406
    else:
        return jsonify({'URI': book_id}), 201

@app.route('/user/take', methods = ['POST'])
def user_get_gook():
    if not request.json or not 'user' in request.json or not 'id' in request.json:
        abort(400)

    status = shelf.take_book(request.json)

    return jsonify({'status': status}), 200

@app.route('/user/history', methods = ['GET'])
def get_history():
    if not request.json or not 'user' in request.json:
        abort(400)

    history = shelf.get_user_history(request.json['user'])

    if history is None:
        return jsonify({}), 200
    else:
        return jsonify(history), 200

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Nothing here!!!'}), 400

if __name__ == '__main__':
    sv_address = '0.0.0.0'
    sv_port = '13097'
    shelf.init('./')
    app.run(host = sv_address, port = sv_port, debug = False)


