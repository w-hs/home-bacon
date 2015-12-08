#!/usr/bin/env python
# -*- coding: utf-8 -*-

import numpy
import csv
import tensorflow as tf

tags = { '7C:2F:80:8D:E2:3B' : 0, '7C:2F:80:8D:E2:45' : 1 }

rssiArray = []
roomArray = []

currentScanId = 1
rssiValues = [0.0, 0.0]
room = 0

with open('scans2.csv', 'rb') as csvfile:
    reader = csv.reader(csvfile, delimiter=',', quotechar='"')
    next(reader, None)  # skip the headers
    
    for row in reader:
        scanId = int(row[0])
        room = int(row[1])
        tag = row[2]
        rssi = int(row[3])
        if scanId != currentScanId:
            rssiArray.append(rssiValues)
            if (room == 1):
                roomArray.append([1.0, 0])
            elif (room == 2):
                roomArray.append([0.0, 1.0])
            rssiValues = [-100, -100]
            currentScanId = scanId
            
        if tag in tags:
            tagId = tags[tag]
            rssiValues[tagId] = rssi

rssiArray.append(rssiValues)
if (room == 0):
    roomArray.append([1.0, 0])
else:
    roomArray.append([0.0, 1.0])

minRssi = 0
maxRssi = -120
for values in rssiArray:
    for value in values:
        if (value > maxRssi):
            maxRssi = value
        if (value < minRssi):
            minRssi = value

print("min: {0}, max: {1}".format(minRssi, maxRssi))

rssiRange = maxRssi - minRssi
def normalizeRssi(values):
    return map(lambda x: (x - minRssi) * 1.0 / rssiRange, values)
               
normalizedRssi = map(normalizeRssi, rssiArray)

numSamples = len(normalizedRssi)
trainingSampleCount = numSamples * 8 / 10
testSampleCount = numSamples - trainingSampleCount
indices = numpy.random.permutation(numSamples)
training_idx, test_idx = indices[:trainingSampleCount], indices[trainingSampleCount:]

train_samples = [normalizedRssi[index] for index in training_idx]
train_labels = [roomArray[index] for index in training_idx]
test_samples = [normalizedRssi[index] for index in test_idx]
test_labels = [roomArray[index] for index in test_idx]

with tf.Session() as sess:

    x = tf.placeholder(tf.float32, shape=[None, 2])
    y_ = tf.placeholder(tf.float32, shape=[None, 2])

    W = tf.Variable(tf.zeros([2, 2]))
    b = tf.Variable(tf.zeros([2]))

    sess.run(tf.initialize_all_variables())

    y = tf.nn.softmax(tf.matmul(x, W) + b)
    #y = tf.matmul(x, W) + b

    cross_entropy = -tf.reduce_sum(y_ * tf.log(y))
    train_step = tf.train.GradientDescentOptimizer(0.01).minimize(cross_entropy)

    for i in range(1000):
        train_step.run(feed_dict={x: train_samples, y_: train_labels}, session=sess)

    outW = sess.run(W)
    outB = sess.run(b)

    print(outW)
    print(outB)

    correct_prediction = tf.equal(tf.argmax(y,1), tf.argmax(y_,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))

    print(accuracy.eval(feed_dict={x: test_samples, y_: test_labels}, session=sess))

    results = sess.run(tf.matmul(x, W) + b, feed_dict= {x: normalizedRssi})
    softmax = sess.run(tf.nn.softmax(tf.matmul(x, W) + b), feed_dict= {x: normalizedRssi})
    print(normalizedRssi[0])
    print(results[0])

import matplotlib.pyplot as plt

fig = plt.figure()
ax = fig.add_subplot(111)
n_points = numpy.array(normalizedRssi)
roomColors = []
for room in roomArray:
    if room[0] > room[1]:
        roomColors.append('yellow') # Küche
    else:
        roomColors.append('cyan') # Flur

p = ax.scatter(n_points[:,0], n_points[:,1], c=roomColors, s=40.0)
ax.set_xlabel('3B: Kueche')
ax.set_ylabel('45: Flur')
ax.set_title('RSSI fuer zwei Beacons')
fig.show()

input("Press Enter to continue...")
