from __future__ import print_function
import numpy as np
import sys
import keras
from keras.models import model_from_json
from keras.utils.visualize_util import plot
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.optimizers import SGD, Adam, RMSprop
from keras.utils import np_utils

nb_classes = 5
features = np.loadtxt("traces/features/astar1level/astarF0")
nn = model_from_json(open('/Users/giorgio/projects/neuromario/competition/research/kp/astar1level_net.json').read())
nn.load_weights('/Users/giorgio/projects/neuromario/competition/research/kp/astar1level_weights.h5')
nn.compile(loss='msle',
              optimizer=RMSprop(),
              metrics=['accuracy'])

y = nn.predict(features, batch_size = 32, verbose = 0)
#print(y)
move = [1 if z > 0 else -1 for z in w for w in y]
for m in move:
  print(m, end = " ")
print("done")
print(move)
