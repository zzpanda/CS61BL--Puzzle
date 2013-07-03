import java.util.*;
import java.io.*;

public class Solver {
	
	private PriorityQueue<Tray> fringe;
	
	//constructs a new solver object. 
	public Solver (String initialConfig, String finalConfig) {
		long initTime = System.nanoTime();
		Tray initialTray = new Tray(initialConfig);
		Tray finalTray = new Tray(initialConfig, finalConfig);
		fringe = new PriorityQueue<Tray>();
		try {
			printPath(initialTray, finalTray);
		} finally {
			long endTime = System.nanoTime();
			long totalTime = endTime - initTime;
			//{Start Debugging!}
			if (Debug.solverTime == Debug.doDebug) {
				long runTime = totalTime / 1000000;
				System.out.println("{Debugging} run time is " + runTime + "ms");
                //{End Debugging...}
			}
		}
	}
	
	/**
	 * Prints a list of paths the trays undergone to the goal.
	 * 
	 * @param initial	The initial tray configuration.
	 * @param goal		The desired goal configuration.
	 */
	public void printPath (Tray initial, Tray goal) {
		ArrayList<Tray> path = path (initial, goal);
		for (Tray correctMove: path) {
			System.out.println(correctMove.getStep());
		}
        return;
	}
	
	/**
	 * Returns an ArrayList containing all the tray configurations included in the path.
	 * 
	 * @param initial	The initial tray configuration.
	 * @param goal		The desired goal configuration.
	 * @return			The ArrayList of tray configurations.
	 */
	public ArrayList<Tray> path (Tray initial, Tray goal) {
		Tray newTray = null;
		ArrayList<Tray> result = new ArrayList<Tray> ( );
		fringe.add(initial);
		while (!fringe.isEmpty()) {
			newTray = (Tray) fringe.poll();
			if (newTray.equalsGoal(goal)) {
				//{Start Debugging!}
				if (Debug.ShowMoveNumber == Debug.doDebug) {
					int totalMoveNumber = newTray.showNumberOfMoves();
					System.out.println("{Debugging} The total moves are " + totalMoveNumber);
				}
	                //{End Debugging...}
				break;
			} else {
				for (Tray nextTray : newTray.getMoves()) {
					    fringe.add(nextTray);
				}
			}
		}
		if (newTray.equalsGoal(goal)) {
		    while (newTray.getParent() != null) {
			    result.add(newTray);
			    newTray = newTray.getParent();
		    }
		    Collections.reverse(result);
		    return result;
		} else {
			System.exit(1);
			return null;
		}
	}
	/**
     * Design for three argument input.
     * Call solver method and Show debug information.
     * 
     * @param DebugInfo     Debugging argument
	 * @param initial		The initial tray configuration.
	 * @param goal			The desired goal configuration.
     */
	
	private static void DebuggingSolver(String DebugInfo, String initialConfig, String finalConfig){
		if (DebugInfo.equals("options") || DebugInfo.equals("")) {
			System.out.println("{{{Debug Menu}}}");
			System.out.println("Instruction: Please put your multiple debuging arguments together after '-o'");
			System.out.println("without any space in between. For example, if you want to know the runtime as");
			System.out.println(" well as the number of total moves,the first input argument will be '-oTN'.");
			System.out.println("Option 1) T: show RUNTIME of Solver");
			System.out.println("Option 2) M: show all the MOVES");
			System.out.println("Option 3) N: show the NUMBER of total MOVES");
		} else {
			for (int i = 0; i < DebugInfo.length(); i++) {
				String current = DebugInfo.substring(i, i+1);
				if (current.equals("T")) {
					Debug.solverTime = Debug.doDebug;
				} else if (current.equals("M")){
					Debug.printMoves = Debug.doDebug;
				} else if (current.equals("N")){
					Debug.ShowMoveNumber = Debug.doDebug;
				} else {
					System.out.println("No such debug option.");
				}
			}
		}
		Solver stepToGoal = new Solver(initialConfig, finalConfig);
	}
	
	//static void main method.
	public static void main(String[] args) throws FileNotFoundException {
		//with two arguments
		if (args.length == 2) {
		    String initialConfig = args[0];
		    String finalConfig = args[1];
            Solver stepToGoal = new Solver(initialConfig, finalConfig);     
        //with three arguments
		} else if (args.length == 3 && args[0].substring(0, 2).equals("-o")) {
			String debugSpec = args[0].substring(2);
		    String initialConfig = args[1];
		    String finalConfig = args[2];
		    DebuggingSolver(debugSpec, initialConfig, finalConfig); 
		} else if (args.length == 3 && args[0].substring(0, 2).equals("-O")) {
			String debugSpec = args[0].substring(2);
		    String initialConfig = args[1];
		    String finalConfig = args[2];
		    DebuggingSolver(debugSpec, initialConfig, finalConfig); 
		} else {
			throw new IllegalArgumentException("Invalid input arguments");
		}
	}
}
