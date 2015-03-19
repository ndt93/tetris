import java.util.Arrays;



public class PlayerSkeleton {

    public static boolean DEBUG = true;

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
    private static final int REWARD_FACTOR = 5;

	// Implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

        // Record the move that produces max utility and reward
        int moveWithMaxUtilityAndReward = 0;
        int maxUtilityAndReward = -1;

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
                int utility = evalUtility(simulatedNextField,
                        simulatedColumnHeights);

                if (reward + utility > maxUtilityAndReward) {
                    if (DEBUG) {
                        System.out.println("Move: " + i + ", Reward: " + reward
                                + ", Utility:" + utility);
                    }
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
        return pick;
	}
	
    private int evalUtility(int[][] field, int[] columnHeights) {
        double utility = 0.0;
        double absoulte_column_height_weight = 0.05;
        double relative_column_height_weight = 0.5;

        double utility_to_be_added = 0.0;

        // Add utility for absolute column heights
        // The more rows left for each column, the higher the utility
        for (int i = 0; i < columnHeights.length; i++) {
            utility_to_be_added = absoulte_column_height_weight
                    * (ROWS - columnHeights[i]);
            // if (DEBUG) {
            // System.out.println("Utility added for absolute:"
            // + utility_to_be_added);
            // }
            utility += utility_to_be_added;
        }

        // Add utility for relative heights for neighboring columns
        int height_diff = 0;
        for (int i = 0; i < columnHeights.length - 1; i++) {
            height_diff += Math.abs(columnHeights[i] - columnHeights[i + 1]);
        }
        utility_to_be_added = relative_column_height_weight
                * (ROWS - height_diff);
        // if (DEBUG) {
        // System.out.println("Utility added for relative:"
        // + utility_to_be_added);
        // }
        utility += utility_to_be_added;
        return (int) utility;
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
        int turn = s.getTurnNumber();

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
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();

		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);

			try {
                Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("You have completed " + s.getRowsCleared() + " rows.");
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
