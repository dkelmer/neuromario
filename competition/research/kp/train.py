from __future__ import print_function
import numpy as np
from keras.utils.visualize_util import plot

from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.optimizers import SGD, Adam, RMSprop
from keras.utils import np_utils

batch_size = 32
nb_classes = 5
nb_epoch = 1000

# read in features and targets
features = np.loadtxt("traces/astarfeatures.txt")
targets = np.loadtxt("traces/astartargets.txt")

print(features.shape)
print(targets.shape)
# split features into training and validation sets
val_sz = features.shape[0] // 5
train_sz = features.shape[0] - val_sz
X_train = features[:train_sz][:]
X_test = features[train_sz:][:]

print(X_train.shape[0], 'train samples')
print(X_test.shape[0], 'test samples')

# split targets into training and validation sets
Y_train = targets[:train_sz][:]
Y_test = targets[train_sz:][:]

nn = Sequential()
nn.add(Dense(28, activation = 'sigmoid', input_dim = 28))
nn.add(Dense(16, activation = 'sigmoid'))
nn.add(Dense(5, activation = 'tanh'))
nn.summary()

nn.compile(loss='mse',
              optimizer=SGD(),
              metrics=['accuracy'])

history = nn.fit(X_train, Y_train,
                    batch_size=batch_size, nb_epoch=nb_epoch,
                    verbose=1, validation_data=(X_test, Y_test))

score = nn.evaluate(X_test, Y_test, verbose=1)
print('Test score:', score[0])
print('Test accuracy:', score[1])

json_string = nn.to_json()
open('astar_arch.json', 'w').write(json_string)
nn.save_weights('astar_weights.h5')

