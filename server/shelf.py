import time
import json

books = {}
user = {}
bookid = 0

def get_books():
    return books.values()

def add_new_book(data):
    global bookid
    global books
    global user
    bookid = bookid + 1
    books[str(bookid)] = {'id': str(bookid), 'name': data['book_name'],
            'description': data['book_info'], 'Lat': float(data['Lat']),
            'Lng': float(data['Lng']), 'taken': 0}
    user_id = data['user']
    if not user_id in user:
        user[user_id] = []

    user[user_id].append({'type': 'give', 'name': data['book_name'], 'time': time.time(), 'Lat': data['Lat'], 'Lng': data['Lng']})
    dump_data('./')
    return str(bookid)

def take_book(data):
    global bookid
    global books
    global user
    user_id = data['user']
    if not user_id in user:
        user[user_id] = []

    book_id = data['id']
    if not book_id in books:
        return 'fail'
    else:
        if books[book_id]['taken'] == 1:
            return 'fail'
        books[book_id]['taken'] = 1
        user[user_id].append({'type': 'take', 'name': books[book_id]['name'], 'time': time.time(), 'Lat': books[book_id]['Lat'],
                'Lng': books[book_id]['Lng']})
        dump_data('./')
        return 'ok'

def get_user_history(user_id):
    global bookid
    global books
    global user
    if not user_id in user:
        return None
    else:
        return user[user_id]

def dump_data(path):
    with open(path + 'data.txt', 'w') as fout:
        fout.write(str(bookid) + '\n')
        fout.write(json.dumps(books.values()) + '\n')
        for uid in user:
            fout.write(uid + '\n')
            with open(path + uid + '.user', 'w') as f_user:
                f_user.write(json.dumps(user[uid]))

def init(path):
    global bookid
    global books
    global user
    with open(path + 'data.txt', 'r') as fid:
        lines = [line[:-1] for line in fid.readlines()]
        current_id = int(lines[0])
        tbooks = json.loads(lines[1])
        books = {}
        for item in tbooks:
            books[item['id']] = item
        for user_id in lines[2:]:
            with open(user_id + '.user', 'r') as f_user:
                uline = f_user.readline()
                user[user_id] = json.loads(uline)
