import java.util.Random;


public class PlayerSkeleton {

	// Implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

        // Fully randomized approach
        int n = legalMoves.length;
        int pick = new Random().nextInt(n);
        return pick;
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
                Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("You have completed " + s.getRowsCleared() + " rows.");
	}
	
}
