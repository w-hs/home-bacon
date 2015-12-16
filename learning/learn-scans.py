#!/usr/bin/python

import csv
import cgitb
import numpy
import fileinput
import tensorflow as tf

cgitb.enable()

print("Content-Type: text/plain; charset=utf-8")
print("")

tags = { 
    '7C:2F:80:99:DE:B1' : 0, 
    '7C:2F:80:99:DE:CD' : 1, 
    '7C:2F:80:99:DE:25' : 2,
    '7C:2F:80:99:DE:70' : 3,
    '7C:2F:80:99:DE:88' : 4,
}

rssiArray = []
roomArray = []

currentScanId = 1
minValue = -106
rssiValues = [minValue, minValue, minValue, minValue, minValue]

reader = csv.reader(fileinput.input(), delimiter=',', quotechar='"')
next(reader, None)  # skip the headers

for row in reader:
	scanId = int(row[0])
	room = int(row[1])
	tag = row[2]
	rssi = int(row[3])
	if scanId != currentScanId:
		rssiArray.append(rssiValues)
		if (room == 1):
			roomArray.append([1.0, 0.0, 0.0])
		elif (room == 2):
			roomArray.append([0.0, 1.0, 0.0])
		elif (room == 3):
			roomArray.append([0.0, 0.0, 1.0])
		rssiValues = [minValue, minValue, minValue, minValue, minValue]
		currentScanId = scanId
		
	if tag in tags:
		tagId = tags[tag]
		rssiValues[tagId] = rssi
		
rssiArray.append(rssiValues)
if (room == 1):
    roomArray.append([1.0, 0.0, 0.0])
elif (room == 2):
    roomArray.append([0.0, 1.0, 0.0])
elif (room == 3):
    roomArray.append([0.0, 0.0, 1.0])
	
minRssi = 0
maxRssi = -120
for values in rssiArray:
    for value in values:
        if (value > maxRssi):
            maxRssi = value
        if (value < minRssi):
            minRssi = value

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

    results = sess.run(tf.matmul(x, W) + b, feed_dict= {x: normalizedRssi})
    softmax = sess.run(tf.nn.softmax(tf.matmul(x, W) + b), feed_dict= {x: normalizedRssi})