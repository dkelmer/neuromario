import socket
import sys
import numpy as np
import keras
from keras.models import model_from_json
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.optimizers import SGD, Adam, RMSprop
from keras.utils import np_utils

print "Running server on port 2016..."
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_address = ('localhost', 2016)
sock.bind(server_address)
sock.listen(1)
print "Server listening on port 2016"
print "Loading neural net from file..."
nb_classes = 5
net = model_from_json(open('a5m.json').read())
net.load_weights('a5m.h5')
net.compile(loss = 'mse', optimizer = RMSprop(), metrics = ['accuracy'])
print "Neural net loaded and ready to go"

count = 0
try:
  while True:
    connection,client_address = sock.accept()
    data = connection.recv(512)
    count += 1
    x = np.array(data.split(), dtype = float)
    print data
#    print x.shape
    x = x.reshape(103,1)
    x = x.transpose()
    y = net.predict_on_batch(x)[0]
    moves = [1 if z > 0 else -1 for z in y]
    #moves
    response = " ".join(map(str,moves))
    if count % 60 == 0:
      response = "0 0 0 0 0"
    if data:
      connection.sendall(response + "\n")
    else:
      break
finally:
  connection.close()
