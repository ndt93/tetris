import java.util.Arrays;



public class PlayerSkeleton {

    public static boolean DEBUG = false;

    public static boolean PRINT_UTILITY = true;

    // Make copies of static variables
    public static final int ORIENT = State.ORIENT;
    public static final int SLOT = State.SLOT;

    public static final int COLS = State.COLS;
    public static final int ROWS = State.ROWS;
    public static final int N_PIECES = State.N_PIECES;
    public static final int[][][] pTop = State.getpTop();
    public static final int[][][] pBottom = State.getpBottom();
    public static final int[][] pWidth = State.getpWidth();
    public static final int[][] pHeight = State.getpHeight();

    // Factor for scaling reward against utility
    private static final int REWARD_FACTOR = 1;

    // Waiting time between consecutive moves
    private static final long WAITING_TIME = 0;

    // Total number of games to be played
    private static int NO_OF_GAMES = 40;
    
//    Weights
    private static double constant_weight;
    private static double[] absoulte_column_height_weight;
    private static double[] relative_column_height_weight;
    private static double highest_column_height_weight;
    private static double holes_weight;

    // Record the utility and reward values of all moves for printing
    // 22 feature values followed by reward value
    int[][] utility_reward_values;

	// Implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

        // initialize value array for all moves
        utility_reward_values = new int[legalMoves.length][23];

        // Record the move that produces max utility and reward
        int moveWithMaxUtilityAndReward = 0;
        int maxUtilityAndReward = -9999;

        int[][] currentField = deepCopy2D(s.getField());
        int[] currentColumnHeights = deepCopy(s.getTop());

        int[][] simulatedNextField;
        int[] simulatedColumnHeights;

        for (int i = 0; i < legalMoves.length; i++) {
            simulatedNextField = deepCopy2D(currentField);
            simulatedColumnHeights = deepCopy(currentColumnHeights);
            int rowRemoved = tryMakeMove(legalMoves[i][ORIENT],
                    legalMoves[i][SLOT], s, simulatedNextField,
                    simulatedColumnHeights);

            // The move does not result in end game, we proceed to evaluate
            if (rowRemoved != -1) {
                int reward = rowRemoved * REWARD_FACTOR;

                // Update constant and reward value for the move for printing
                utility_reward_values[i][0] = (int) constant_weight;
                utility_reward_values[i][22] = reward;
                int utility = evalUtility(i, simulatedNextField,
                        simulatedColumnHeights);
                if (DEBUG) {
                    System.out.println("Eval: Move: " + i + ", Reward: "
                            + reward
                            + ", Utility:" + utility);
                }
                if (reward + utility > maxUtilityAndReward) {

                    maxUtilityAndReward = reward + utility;
                    moveWithMaxUtilityAndReward = i;
                }
            }
        }
        
        // In the case where all moves lead to losing, it will choose the first
        // move with index 0
        if (DEBUG) {
            System.out.println();
            System.out.println("Choice: " + "Move: "
                    + moveWithMaxUtilityAndReward + ", Reward+Utility:"
                    + maxUtilityAndReward);
            System.out.println();
        }
        int pick = moveWithMaxUtilityAndReward;

        // Print the feature and reward values for interfacing with learner
        for (int i = 0; i < utility_reward_values[pick].length; i++) {
            System.out.print(utility_reward_values[pick][i]);
            System.out.print(' ');
        }
        System.out.println();


        return pick;
	}
	
    private int evalUtility(int move, int[][] field, int[] columnHeights) {
        double utility = 0.0;

        double utility_to_be_added = 0.0;

        // Add utility for absolute column heights
        // The more rows left for each column, the higher the utility
        for (int i = 0; i < columnHeights.length; i++) {
            utility_to_be_added = absoulte_column_height_weight[i]
                    * columnHeights[i];

            // update index 1 to 10
            utility_reward_values[move][i + 1] = (int) utility_to_be_added;

            // if (DEBUG) {
            // System.out.println("Utility added for absolute:"
            // + utility_to_be_added);
            // }
            utility += utility_to_be_added;
        }

        // Add utility for relative heights for neighboring columns
        int height_diff = 0;
        for (int i = 0; i < columnHeights.length - 1; i++) {
            height_diff = Math.abs(columnHeights[i] - columnHeights[i + 1]);
            utility_to_be_added = relative_column_height_weight[i]
                    * height_diff;

            // update index 11 to 19
            utility_reward_values[move][i + 11] = (int) utility_to_be_added;

            utility += utility_to_be_added;
            // if (DEBUG) {
            // System.out.println("Utility added for relative:"
            // + utility_to_be_added);
            // }
        }

        // Add utility for max height of column
        int highest = 0;
        for (int i = 0; i < columnHeights.length; i++) {
            if (columnHeights[i] > highest) {
                highest = columnHeights[i];
            }
        }
        utility_to_be_added = highest_column_height_weight * highest;

        // update index 20
        utility_reward_values[move][20] = (int) utility_to_be_added;

        utility += utility_to_be_added;

        // Add utility for holes
        int no_of_holes = getNumberOfHoles(field);
        utility_to_be_added = holes_weight * no_of_holes;
        if (DEBUG) {
            System.out.println("Move: " + move + ", No of holes: "
                    + no_of_holes
                    + ", utility added for holes:" + utility_to_be_added);
        }
        // update index 21
        utility_reward_values[move][21] = (int) utility_to_be_added;

        utility += utility_to_be_added;

        return (int) utility;
    }

    private int getNumberOfHoles(int[][] field) {
        int count = 0;
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (isHole(i, j, field)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isHole(int i, int j, int[][] field) {
        if (field[i][j] != 0) {
            return false;
        }

        // Row - 1, down
        if (i - 1 >= 0 && field[i - 1][j] == 0) {
            return false;
        }
        // Row + 1, up
        if (i + 1 < ROWS && field[i + 1][j] == 0) {
            return false;
        }
        // Column + 1, right
        if (j + 1 < COLS && field[i][j + 1] == 0) {
            return false;
        }
        // Column - 1, left
        if (j - 1 >= 0 && field[i][j - 1] == 0) {
            return false;
        }
        return true;
    }

    /**
     * Simulate the effect of making a specific move
     * 
     * Modifies simulated next field and height of each column for further
     * evaluation
     * 
     * @param orient
     * @param slot
     * @param s
     * @return number of rows removed, -1 if lost
     */
    public int tryMakeMove(int orient, int slot, State s, int[][] field,
            int[] top) {

        // Initialize state-related variables

        int nextPiece = s.getNextPiece();
        int turn = s.getTurnNumber() + 1;

        // Height if the first column makes contact
        int height = top[slot] - pBottom[nextPiece][orient][0];

        // For each column beyond the first in the piece
        for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
            height = Math.max(height,
                              top[slot + c] - pBottom[nextPiece][orient][c]);
        }


        // Check if game ended
        if (height + pHeight[nextPiece][orient] >= ROWS) {
            return -1;
        }


        // For each column in the piece - fill in the appropriate blocks
        for (int i = 0; i < pWidth[nextPiece][orient]; i++) {
            // From bottom to top of brick
            for (int h = height + pBottom[nextPiece][orient][i];
                 h < height + pTop[nextPiece][orient][i];
                 h++) {
                field[h][i + slot] = turn;
            }
        }

        // adjust top
        for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot + c] = height + pTop[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        // check for full rows - starting at the top
        for (int r = height + pHeight[nextPiece][orient] - 1;
             r >= height;
             r--) {
            // check all columns in the row
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            // if the row was full - remove it and slide above stuff down
            if (full) {
                rowsCleared++;
                // for each column
                for (int c = 0; c < COLS; c++) {

                    // slide down all bricks
                    for (int i = r; i < top[c]; i++) {
                        field[i][c] = field[i + 1][c];
                    }
                    // lower the top
                    top[c]--;
                    while (top[c] >= 1 && field[top[c] - 1][c] == 0)
                        top[c]--;
                }
            }
        }

        return rowsCleared;
    }

	public static void main(String[] args) {

        if (args.length == 23) {
            // Take in weights from the command line if present
            NO_OF_GAMES = Integer.parseInt(args[0]);
            initializeWeights(args);
        } else {
            // Use default weights if arguments are not present
            initializeWeights();
        }

        State s;
        TFrame t;
        for (int i = 0; i < NO_OF_GAMES; i++) {

            s = new State();
            t = new TFrame(s);

            PlayerSkeleton p = new PlayerSkeleton();

            while(!s.hasLost()) {
                s.makeMove(p.pickMove(s, s.legalMoves()));
                s.draw();
                s.drawNext(0, 0);

                try {
                    Thread.sleep(WAITING_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Remove the windows as we finish the game, leave the last window
            if (i < NO_OF_GAMES - 1) {
                t.dispose();
            }
            System.out.println("#");
            System.out.println("You have completed " + s.getRowsCleared()
                    + " rows.");
        }

	}

    private static void initializeWeights(String[] args) {
        // args[1] for constant
        constant_weight = Integer.parseInt(args[1]);

        // args[2] to args[11] for absolute column heights
        absoulte_column_height_weight = new double[COLS];
        for (int i1 = 0; i1 < absoulte_column_height_weight.length; i1++) {
            absoulte_column_height_weight[i1] = Integer.parseInt(args[2 + i1]);
        }

        // args[12] to args[20] for relative column heights
        relative_column_height_weight = new double[COLS - 1];
        for (int i1 = 0; i1 < relative_column_height_weight.length; i1++) {
            relative_column_height_weight[i1] = Integer.parseInt(args[12 + i1]);
        }

        // args[21] for relative column heights
        highest_column_height_weight = Integer.parseInt(args[21]);

        // args[22] for relative column heights
        holes_weight = Integer.parseInt(args[22]);

        assert (absoulte_column_height_weight.length == 10);
        assert (relative_column_height_weight.length == 9);

    }

    private static void initializeWeights() {
        constant_weight = 1.0;
        absoulte_column_height_weight = new double[COLS];
        for (int i1 = 0; i1 < absoulte_column_height_weight.length; i1++) {
            absoulte_column_height_weight[i1] = -0.05;
        }

        relative_column_height_weight = new double[COLS - 1];
        for (int i1 = 0; i1 < relative_column_height_weight.length; i1++) {
            relative_column_height_weight[i1] = -0.5;
        }

        highest_column_height_weight = -0.5;
        holes_weight = -1.0;

        assert (absoulte_column_height_weight.length == 10);
        assert (relative_column_height_weight.length == 9);
    }
	
    public static int[][] deepCopy2D(int[][] original) {
        if (original == null) {
            return null;
        }

        final int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    public static int[] deepCopy(int[] original) {
        if (original == null) {
            return null;
        }

        int[] result = new int[original.length];
        result = Arrays.copyOf(original, original.length);
        return result;
    }

}
