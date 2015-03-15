import numpy as np


class Agent:
    def __init__(self):
        self.lf = 0.5
        self.data = np.array([])

    def set_learning_factor(self, learning_factor):
        self.lf = learning_factor

    def set_data(self, data):
        self.data = data
