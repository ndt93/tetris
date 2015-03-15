import numpy as np
import sys

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

    agent = Agent()
    agent.set_data(data)
    agent.set_learning_factor(0.5)
