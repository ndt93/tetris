import sys
import os
import random
import subprocess

import numpy as np

from agent import Agent

#CONSTANTS

GAME_FILE = 'game_data'
WEIGHT_FILE = 'weight_data'
COUNT = 1
WEIGHTS = [random.randint(-10, 10) for i in range(22)]
INIT_VALS = '1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5'
NUM_GAMES = '100'
PATH = os.path.abspath('../src')
CMD = ['java', 'PlayerSkeleton', NUM_GAMES, INIT_VALS]

def run_game():
    global COUNT, CMD, FILE_NAME
    print('Running game %s'%COUNT)
    with open(GAME_FILE+str(COUNT), 'w') as f:
        print(CMD)
        subprocess.call(CMD, stdout=f, cwd=PATH)
    COUNT+=1

def read_data(filename):
    data = []

    with open(filename, 'r') as f:
        game = []
        rows = []

        for line in f:
            if line[0] == '#':
                data.append(np.array(game))
                game = []
            elif line[0] == "Y":
                row = (line.split(" "))[3] 
                rows.append(int(row))
            else:
                state = line.split()
                state = [float(x) for x in state]
                game.append(state)

    print("Average rows completed: "+ str(np.mean(rows)))
    print("Max rows completed: "+ str(max(rows)))
    data.append(np.array(game))
    return data


'''
arg1: number of cycle 
arg2: start count
arg3: initial weight_file
'''

if __name__ == "__main__":
    agent = Agent()
    agent.set_learning_factor(0.2)

    subprocess.call(['javac', 'PlayerSkeleton.java'], cwd=PATH) #Compile java files
    os.chdir('./data02')

    if len(sys.argv) == 4:
        #Initialization step for COUNT and WEIGHTS
        with open(sys.argv[3], 'r') as w:
            weight = w.readline()
            WEIGHTS = [float(x) for x in weight.split()]
            INIT_VALS = ' '.join(["%.4f"%w for w in WEIGHTS])
            CMD = ['java', 'PlayerSkeleton', NUM_GAMES, INIT_VALS]
            print(CMD)
            COUNT = int(sys.argv[2])

        for i in range(int(sys.argv[1])):
            run_game()
            data = read_data(GAME_FILE+str(COUNT-1))
            agent.set_data(data)
            agent.set_rt(np.array(WEIGHTS))
            WEIGHTS = agent.compute_next_rt()
            INIT_VALS = ' '.join(["%.4f"%w for w in WEIGHTS])
            CMD = ['java', 'PlayerSkeleton', NUM_GAMES, INIT_VALS]
            with open(WEIGHT_FILE+str(COUNT-1),'w') as f:
                f.write(INIT_VALS)

    else:
        print ("Usage: python learning_driver.py iterations start_count weight_file\n\
        weight_file can be found in ./data")
