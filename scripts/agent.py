import numpy as np


class Agent:
    num_features = 22

    def __init__(self):
        self.lf = 0.5  # Learning factor lambda
        self.data = []  # The features' values for all the games
        self.rewards = []  # Reward values for moving from 1 state to the next
        self.rt = np.array([])

    def set_learning_factor(self, learning_factor):
        self.lf = learning_factor

    def set_rt(self, rt):
        self.rt = rt

    def set_data(self, data):
        self.data = []
        self.rewards = []

        for game in data:
            self.data.append(game[:, :-1])
            self.rewards.append(game[:, -1:])

    def eval_func(self, m, k, r):
        """
        The evaluation function value for the set of weights (vector) r
        at the mth game and kth board state
        """
        Nm = self.data[m].shape[0] - 1

        if (k == Nm):
            return 0

        return np.dot(r, self.data[m][k])

    def get_reward(self, s):
        """
        Get reward for moving from state s to state (s + 1)
        """
        return self.rewards[s + 1]

    def temporal_diff(self, m, s):
        """
        The temporal diffence value for state s to state (s+1) in the mth game
        """
        return (self.get_reward(s) + self.eval_func(m, s + 1, self.rt) -
                self.eval_func(m, s, self.rt))

    def temporal_diff_sum(self, m, k):
        Nm = self.data[m].shape[0] - 1
        result = 0

        for s in range(k, Nm):
            result += self.lf**(s - k) * self.temporal_diff(m, s)

        return result

    def optimized_func(self, r):
        result = 0
        M = len(self.data)

        for m in range(M):
            Nm = self.data[m].shape[0] - 1

            for k in range(Nm + 1):
                result += (self.eval_func(m, k, r) -
                           self.eval_func(m, k, self.rt) -
                           self.temporal_diff_sum(m, k)) ** 2

        return result
