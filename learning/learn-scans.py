#!/usr/bin/python

import csv
import json
import cgitb
import numpy
import fileinput
import tensorflow as tf

def read_scans_from_stdin():
    reader = csv.reader(fileinput.input(), delimiter=',', quotechar='"')
    next(reader, None)  # skip the headers

    scans = []
    for row in reader:
        scan_id = int(row[0])
        room = row[1]
        tag = row[2]
        rssi = int(row[3])
        scan = Scan(scan_id, room, tag, rssi)
        scans.append(scan)
        
    return scans
    
def get_tags_from_scans(scans):
    tags = {}
    index = 0
    
    for scan in scans:
        if not (scan.tag in tags):
            tags[scan.tag] = index
            index = index + 1
    
    return tags
    
def get_rooms_from_scans(scans):
    rooms = {}
    index = 0
    
    for scan in scans:
        if not (scan.room in rooms):
            rooms[scan.room] = index
            index = index + 1
    
    return rooms

cgitb.enable()

print("Content-Type: application/json; charset=utf-8")
print("")

class Scan:
    id = 0
    room = 0
    tag = ""
    rssi = -100
    
    def __init__(self, scan_id, room, tag, rssi):
        self.id = scan_id
        self.room = room
        self.tag = tag
        self.rssi = rssi

scans = read_scans_from_stdin()
tags = get_tags_from_scans(scans)
x_dim = len(tags)
rooms = get_rooms_from_scans(scans)
y_dim = len(rooms)

rssi_array = []
room_array = []

current_scan_id = scans[0].id
min_value = -106
rssi_values = [min_value] * x_dim
room_values = [0.0] * y_dim
current_room = 0    
for scan in scans:
    if scan.id != current_scan_id:
        rssi_array.append(rssi_values)
        room_index = rooms[current_room]
        room_values[room_index] = 1.0
        room_array.append(room_values)
        
        rssi_values = [min_value] * x_dim
        room_values = [0.0] * y_dim
        current_scan_id = scan.id
    
    current_room = scan.room
    if scan.tag in tags:
        tag_index = tags[scan.tag]
        rssi_values[tag_index] = scan.rssi
    
rssi_array.append(rssi_values)
room_index = rooms[current_room]
room_values[room_index] = 1.0
room_array.append(room_values)

min_rssi = 0
max_rssi = -120
for values in rssi_array:
    for value in values:
        if (value > max_rssi):
            max_rssi = value
        if (value < min_rssi):
            min_rssi = value

rssi_range = max_rssi - min_rssi
def normalizeRssi(values):
    return map(lambda x: (x - min_rssi) * 1.0 / rssi_range, values)

normalized_rssi = map(normalizeRssi, rssi_array)

num_samples = len(normalized_rssi)
num_training_samples = num_samples * 8 / 10
num_test_samples = num_samples - num_training_samples
indices = numpy.random.permutation(num_samples)
training_idx, test_idx = indices[:num_training_samples], indices[num_training_samples:]

train_samples = [normalized_rssi[index] for index in training_idx]
train_labels = [room_array[index] for index in training_idx]
test_samples = [normalized_rssi[index] for index in test_idx]
test_labels = [room_array[index] for index in test_idx]

with tf.Session() as sess:
    x = tf.placeholder(tf.float32, shape=[None, x_dim])
    y_ = tf.placeholder(tf.float32, shape=[None, y_dim])

    W = tf.Variable(tf.zeros([x_dim, y_dim]))
    b = tf.Variable(tf.zeros([y_dim]))

    sess.run(tf.initialize_all_variables())

    y = tf.nn.softmax(tf.matmul(x, W) + b)

    cross_entropy = -tf.reduce_sum(y_ * tf.log(y))
    train_step = tf.train.GradientDescentOptimizer(0.01).minimize(cross_entropy)

    for i in range(1000):
        train_step.run(feed_dict={x: train_samples, y_: train_labels}, session=sess)
    
    out_W = sess.run(W)
    out_b = sess.run(b)
    
    correct_prediction = tf.equal(tf.argmax(y,1), tf.argmax(y_,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))
    out_acc = accuracy.eval(feed_dict={x: test_samples, y_: test_labels}, session=sess)
   
    output = {
        'acc': numpy.asscalar(out_acc),
        'tags': tags,
        'rooms': rooms,
        'W': out_W.tolist(),
        'b': out_b.tolist(),
        'min_rssi': min_rssi,
        'max_rssi': max_rssi
    }
    print(json.dumps(output, sort_keys=True, indent=2))
