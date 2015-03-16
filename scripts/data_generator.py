import random

num_games = 100
num_states = 22
w = 10
h = 21

if __name__ == "__main__":
    for game in range(num_games):
        for state in range(num_states):
            features = []
            cols = [random.randint(0, h) for col in range(w)]

            for k in range(w):
                features.append(str(cols[k]))
            for k in range(w - 1):
                features.append(str(abs(cols[k] - cols[k + 1])))
            features.append(str(max(cols)))
            features.append(str(random.randint(0, w)))

            reward = random.randint(0, 4)

            print (str(1) + " " + " ".join(features) + " " + str(reward))

        if game < num_games - 1:
            print '#'
