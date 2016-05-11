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
features = np.loadtxt("traces/features/humanFm")
targets = np.loadtxt("traces/targets/humanTm")

print(features.shape)
print(targets.shape)

# split features into training and validation sets
#val_sz = features.shape[0] // 5
#train_sz = features.shape[0] - val_sz
#X_train = features[:train_sz][:]
#X_test = features[train_sz:][:]

#print(X_train.shape[0], 'train samples')
#print(X_test.shape[0], 'test samples')

# split targets into training and validation sets
#Y_train = targets[:train_sz][:]
#Y_test = targets[train_sz:][:]

nn = Sequential()
nn.add(Dense(103, activation = 'sigmoid', input_dim = 103))
nn.add(Dropout(0.2))
nn.add(Dense(num_hidden, activation = 'sigmoid'))
nn.add(Dropout(0.2))
#nn.add(Dense(num_hidden, activation = 'sigmoid'))
#nn.add(Dropout(0.2))
#nn.add(Dense(num_hidden, activation = 'sigmoid'))
#nn.add(Dropout(0.2))
nn.add(Dense(5, activation = 'sigmoid'))
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
open('human_net.json', 'w').write(json_string)
nn.save_weights('human_weights.h5')

