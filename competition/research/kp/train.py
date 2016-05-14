from __future__ import print_function
import numpy as np

from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.optimizers import SGD, Adam, RMSprop
from keras.utils import np_utils

batch_size = 16
nb_classes = 5
nb_epoch = 1000
num_hidden = 54

# read in features and targets
features = np.loadtxt("traces/features/human1level/humanF19")
targets = np.loadtxt("traces/targets/human1level/humanT19")

print(features.shape)
print(targets.shape)

nn = Sequential()
nn.add(Dense(103, activation = 'tanh', input_dim = 103))
nn.add(Dense(num_hidden, activation = 'tanh'))
nn.add(Dense(num_hidden, activation = 'tanh'))
nn.add(Dense(5, activation = 'tanh'))
nn.summary()

nn.compile(loss='mse',
              optimizer=RMSprop(),
              metrics=['accuracy'])

history = nn.fit(features, targets,
                    batch_size=batch_size, nb_epoch=nb_epoch,
                    verbose=1, validation_split=0.2)

score = nn.evaluate(features, targets, batch_size=batch_size, verbose=1)
print('Test score:', score[0])
print('Test accuracy:', score[1])

json_string = nn.to_json()
open('h1m.json', 'w').write(json_string)
nn.save_weights('h1m.h5')

