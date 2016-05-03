from __future__ import print_function
import numpy as np
import sys
stdout = sys.stdout
sys.stdout = open('/dev/null', 'w')
import keras
sys.stdout = stdout
from keras.models import model_from_json
from keras.utils.visualize_util import plot
np.random.seed(1337)
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.optimizers import SGD, Adam, RMSprop
from keras.utils import np_utils

nb_classes = 5
test = "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 -10 -10 -10 -10 -10 -10 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0"
#trace = sys.argv[1]
trace = test
x = np.array(trace.split(), dtype = int)
x = x.reshape(56,1)
x = x.transpose()
nn = model_from_json(open('/Users/giorgio/projects/neuromario/competition/research/kp/giorgio_net.json').read())
nn.load_weights('/Users/giorgio/projects/neuromario/competition/research/kp/giorgio_weights.h5')
nn.compile(loss='mse',
              optimizer=RMSprop(),
              metrics=['accuracy'])

y = nn.predict_on_batch(x)[0]
print(y)
move = [1 if z > 0.5 else -1 for z in y]
for m in move:
  print(m, end = " ")
print("done")
#print(move)
