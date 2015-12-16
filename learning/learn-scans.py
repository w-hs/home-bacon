#!/usr/bin/python

import csv
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
        room = int(row[1])
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

cgitb.enable()

print("Content-Type: text/plain; charset=utf-8")
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
print("tags=" + str(tags))

rssi_array = []
room_array = []

current_scan_id = scans[0].id
minValue = -106
rssi_values = [minValue, minValue, minValue, minValue, minValue]
room = 0    
for scan in scans:
    if scan.id != current_scan_id:
        rssi_array.append(rssi_values)
        if (scan.room == 1):
            room_array.append([1.0, 0.0, 0.0])
        elif (scan.room == 2):
            room_array.append([0.0, 1.0, 0.0])
        elif (scan.room == 3):
            room_array.append([0.0, 0.0, 1.0])
        rssi_values = [minValue, minValue, minValue, minValue, minValue]
        current_scan_id = scan.id
    
    room = scan.room
    if scan.tag in tags:
        tag_index = tags[scan.tag]
        rssi_values[tag_index] = scan.rssi
    
rssi_array.append(rssi_values)
if (room == 1):
    room_array.append([1.0, 0.0, 0.0])
elif (room == 2):
    room_array.append([0.0, 1.0, 0.0])
elif (room == 3):
    room_array.append([0.0, 0.0, 1.0])

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

numSamples = len(normalized_rssi)
trainingSampleCount = numSamples * 8 / 10
testSampleCount = numSamples - trainingSampleCount
indices = numpy.random.permutation(numSamples)
training_idx, test_idx = indices[:trainingSampleCount], indices[trainingSampleCount:]

train_samples = [normalized_rssi[index] for index in training_idx]
train_labels = [room_array[index] for index in training_idx]
test_samples = [normalized_rssi[index] for index in test_idx]
test_labels = [room_array[index] for index in test_idx]

with tf.Session() as sess:
    
    x_dim = len(tags)
    y_dim = 3
    x = tf.placeholder(tf.float32, shape=[None, x_dim])
    y_ = tf.placeholder(tf.float32, shape=[None, y_dim])

    W = tf.Variable(tf.zeros([x_dim, y_dim]))
    b = tf.Variable(tf.zeros([y_dim]))

    sess.run(tf.initialize_all_variables())

    y = tf.nn.softmax(tf.matmul(x, W) + b)
    #y = tf.matmul(x, W) + b

    cross_entropy = -tf.reduce_sum(y_ * tf.log(y))
    train_step = tf.train.GradientDescentOptimizer(0.01).minimize(cross_entropy)

    for i in range(1000):
        train_step.run(feed_dict={x: train_samples, y_: train_labels}, session=sess)
    
    outW = sess.run(W)
    outB = sess.run(b)
    
    print('W=' + str(outW))
    print('b=' + str(outB))
    
    correct_prediction = tf.equal(tf.argmax(y,1), tf.argmax(y_,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))
    
    acc_result = accuracy.eval(feed_dict={x: test_samples, y_: test_labels}, session=sess)
    print('acc=' + str(acc_result))