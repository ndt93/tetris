import sys
import random

import numpy as np

from agent import Agent


def read_data(filename):
    data = []

    with open(filename, 'r') as f:
        game = []

        for line in f:
            if line[0] == '#':
                data.append(np.array(game))
                game = []
            else:
                state = line.split()
                state = [int(x) for x in state]
                game.append(state)

    data.append(np.array(game))
    return data


if __name__ == "__main__":
    data = read_data(sys.argv[1])
    num_iter = int(sys.argv[2])

    agent = Agent()
    agent.set_data(data)
    agent.set_learning_factor(0.5)
    agent.set_rt(np.array([random.randint(-10, 10) for i in range(22)]))
    agent.set_iter(num_iter)

    print agent.compute_next_rt()
