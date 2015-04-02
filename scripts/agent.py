import random
from datetime import datetime

import numpy as np
from scipy.optimize import minimize


class Agent:
    num_features = 22

    def __init__(self):
        self.lf = 0.2  # Learning factor lambda
        self.data = []  # The features' values for all the games
        self.rewards = []  # Reward values for moving from 1 state to the next
        self.rt = np.array([])
        self.max_iter = 50

    def set_learning_factor(self, learning_factor):
        assert(learning_factor >= 0 and learning_factor <= 1)
        self.lf = learning_factor

    def set_rt(self, rt):
        assert(len(rt) == self.num_features)
        self.rt = rt

    def set_iter(self, max_iter):
        self.max_iter = max_iter

    def set_data(self, data):
        self.data = []
        self.rewards = []

        for game in data:
            game = np.vstack((game, np.zeros(self.num_features + 1)))
            self.data.append(game[:, :-1])
            self.rewards.append(game[:, -1:])

    def eval_func(self, m, k, r):
        """
        The evaluation function value for the set of weights (vector) r
        at the mth game and kth board state
        """
        return np.dot(r, self.data[m][k])

    def eval_func_der(self, m, k, r, i):
        """
        Find the derivative of the evaluation function with respect
        to the ith component of the vector r
        """
        return self.data[m][k][i]

    def get_reward(self, m, s):
        """
        Get reward for moving from state s to state (s + 1)
        """
        return self.rewards[m][s + 1][0]

    def temporal_diff(self, m, s):
        """
        The temporal diffence value for state s to state (s+1) in the mth game
        """
        return (self.get_reward(m, s) + self.eval_func(m, s + 1, self.rt) -
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

    def optimized_func_i_der(self, r, i):
        """
        The derivative of the optimized function with respect to the
        ith component of the vector r
        """
        result = 0
        M = len(self.data)

        for m in range(M):
            Nm = self.data[m].shape[0] - 1

            for k in range(Nm + 1):
                result += ((self.eval_func(m, k, r) -
                            self.eval_func(m, k, self.rt) -
                            self.temporal_diff_sum(m, k)) * 2 *
                           self.eval_func_der(m, k, r, i))

        return result

    def optimized_func_der(self, r):
        return np.array([self.optimized_func_i_der(r, i)
                         for i in range(len(r))])

    def callback(self, r):
        print("Iteration %d completed at %s" %
              (self.cur_iter, datetime.now().strftime("%d/%m/%Y %H:%M:%S")))
        self.cur_iter += 1

    def compute_next_rt(self):
        self.cur_iter = 1

        r0 = np.array([random.randint(-10, 10)
                       for i in range(self.num_features)])

        res = minimize(self.optimized_func, r0, method='BFGS',
                       jac=self.optimized_func_der,
                       options={'maxiter': self.max_iter, 'disp': True},
                       callback=self.callback)

        return res.x
