import java.util.*;

import java.util.Map.Entry;

/**
 * A Tray configuration represented by its width, length, and the adjacency of 
 * the Blocks it contains.The adjacency list is implemented with a Map of Block 
 * keys to their Array (length 4) values. The Array values contain info about 
 * each of the Block's 4 edges. Indices 0,1,2,3 correspond to TOP_EDGE, RIGHT_EDGE, 
 * BOTTOM_EDGE, and LEFT_EDGE respectively and they hold Lists of other blocks touching
 * that edge (empty if none) or null if the edge is against the side of the Tray.
 *
 */
public class Tray implements Comparable<Tray>{
	
	/**
	 * @param args
	 */
	private static final int TOP_EDGE = 0;
	private static final int RIGHT_EDGE = 1;
	private static final int BOTTOM_EDGE = 2;
	private static final int LEFT_EDGE = 3;
	private static final int NOT_TOUCHING = -1;
	private static int trayLength;
	private static int trayWidth;
	private Tray myParent = null;
	private String stepToGoal = "";
	private int numberOfMove = 0;
	private int trayKey = 0;
	private static ArrayList<String> isChecked;
	
	/**Adjacency Relationships**/
	private HashMap<Block, List<Block>[]> myConfig;
	
	/**Goal and Score**/
	private static HashSet<Block> goalBlocks = new HashSet<Block>();
	private int myScore = 0;
	
	/**
	 * Creates a new Tray by retrieving dimensions and Block data
	 * from the input source. 
	 * @param initialConfig
	 */
	public Tray(String initialConfig) {
		InputSource inputFile = new InputSource(initialConfig);
		String s = inputFile.readLine();
		String [] lineVals = s.split(" ");
		trayLength = Integer.parseInt(lineVals[0]);
		trayWidth = Integer.parseInt(lineVals[1]);
		myConfig = new HashMap<Block, List<Block>[]>();
		isChecked = new ArrayList<String>();
		while (true) {
			s = inputFile.readLine ( );
			if (s == null) {
				break;
			}
			lineVals = s.split(" ");
			Block newBlock = new Block(lineVals);
			addBlock(newBlock);
			trayKey = trayKey + newBlock.blockKey;
		}
	}
	
	/**
	 * Creates a new Tray by retrieving dimensions and Block data
	 * from the input source. 
	 * @param initialConfig, finalConfig
	 */
	public Tray(String initialConfig, String finalConfig) {
		String s;
		String[] lineVals;
		InputSource finalFile = new InputSource(finalConfig);
		while (true) {
			s = finalFile.readLine ( );
			if (s == null) {
				break;
			}
			lineVals = s.split(" ");
			Block newBlock = new Block(lineVals);
			goalBlocks.add(newBlock);
		}
	}
	
	/**
	 * Creates a new Tray using length, width, and Block data passed
	 * in as arguments, assigning them directly to the object variables.
	 * @param length
	 * @param width
	 * @param blocks
	 */
	public Tray(HashSet<Block> blocks, String step, int counter, int key){
		trayKey = key;
		myConfig = new HashMap<Block, List<Block>[]>();
		stepToGoal = step;
		numberOfMove = counter;
		for (Block block : blocks) {
			String a = block.toString();
			String [] lineVals =  a.split(" ");
			Block newB = new Block(lineVals);
			addBlock(newB);
		}
	}
	
	/**
	 * Adds a new Block to the Tray by placing it on the adjacency list
	 * and forming edges with the other blocks it touches. To form an
	 * edge, the Blocks are added to each others' edgelist
	 * @param newBlock
	 */
	private void addBlock(Block newBlock){
		List<Block> [] edges = new List [4];
		for(int i = 0; i < 4; i++){
			if (newBlock.isTouchingBoundary(i)){
				edges[i] = null;
			}
			else{
				edges[i] = new ArrayList<Block>();
			}
		}
		for (Block otherBlock : myConfig.keySet()) {
			int contact = newBlock.touchingSide(otherBlock);
			if (contact >= 0) {
				edges[contact].add(otherBlock);
				otherBlock.addContact(newBlock, (contact+2)%4);
			}
		}
		myConfig.put(newBlock, edges);
	}
	
	/**
	 * Returns a "score" a move merits. Score determines the location a resulting tray configuration is placed on the priorityqueue.
	 */
	public int setScore() {
		int min = 512;
		for (Block goalBlock: goalBlocks) {
			for (Block block: myConfig.keySet()) {
				if(block.getLength() == goalBlock.getLength() && block.getWidth() == goalBlock.getWidth()){
					int tmp = Math.abs(block.getRow() - goalBlock.getRow()) + Math.abs(block.getCol() - goalBlock.getCol());
					min = Math.min(min, tmp);
				}
			}
			myScore = myScore + min;
		}
		return myScore;
	}
	
	/**
	 * Takes a block and a direction and moves that block towards the given direction.
	 * 
	 * @param b			block to be moved.
	 * @param direction	the direction the block is to be moved.
	 * @return			the resulting tray configuration after the move.
	 */
	public Tray makeMove(Block b, int direction){
		Tray result = null;
		//{Start Debugging!}
		if (Debug.printMoves == Debug.doDebug) {
			String[] dirInfo = new String[4];
			dirInfo[0] = "up";
			dirInfo[1] = "right";
			dirInfo[2] = "down";
			dirInfo[3] = "left";
            System.out.println("{Debugging} Move " + b.toString() + " " + dirInfo[direction]);
        }
		//{End Debugging...}
		Block newB = b.shiftBlock(direction);
		HashSet<Block> blocks = new HashSet<Block>(myConfig.keySet());
		String step = b.getRow() + " " + b.getCol() + " " + newB.getRow() + " " + newB.getCol();
		blocks.remove(b);
		blocks.add(newB);
		int temp = trayKey - b.getBlockKey() + newB.getBlockKey();
		String block = String.valueOf(temp);
		if (!isChecked.contains(block)) {
			isChecked.add(block);
		    result = new Tray(blocks, step, numberOfMove+1, temp);
		}
		return result;
	}
	
	/**
	 * A method that returns the list of moves a configuration has undergone thus far.
	 * 
	 * @return	Returns an ArrayList of Tray (tray configurations) as a result from the moves
	 */
	public ArrayList<Tray> getMoves(){
		ArrayList<Tray> moves = new ArrayList<Tray>();
		for (Entry<Block, List<Block>[]> entry : myConfig.entrySet()) {
			Block b = entry.getKey();
			List<Block> [] edges = entry.getValue();
			for(int i = 0; i<4; i++){
				if (edges[i] != null && edges[i].isEmpty()) {
					Tray nextTray = makeMove(b, i);
					if (nextTray != null) {
					    nextTray.myParent = this;
					    nextTray.myScore = nextTray.setScore();
					    moves.add(nextTray);
					}
				}
			}
		}
		return moves;
	}
	
	/**
	 * Returns the number of moves from the initial.
	 * @return total moves to get to the state from the initial.
	 */
	public int showNumberOfMoves(){
		return numberOfMove;
	}
	
	/**
	 * Returns a parent configuration. Null if this happens to be the initial tray configuration.
	 * 
	 * @return Returns a tray configuration before the latest move.
	 */
	public Tray getParent() {
		return myParent;
	}
	
	/**
	 * Gets the initial coordinate of a block before a move and the resulting coordinate after the move.
	 * 
	 * @return Returns a String of 4 variables: the initial y and x and the final y and x.
	 */
	public String getStep() {
		return stepToGoal;
	}
	
	/**
	 * Gets the score of a resulting tray configuration after a move.
	 * 
	 * @return Returns an integer value detailing the priority of a move.
	 */
	public int getScore() {
		return myScore;
	}
	
	/**
	 * Compares a score between tray configurations to determine their location on a priority queue.
	 * 
	 */
	public int compareTo(Tray otherTray) {
		return myScore - otherTray.myScore;
	}
	
	/**
	 * Returns a boolean checking if two tray configurations are the same.
	 */
	public boolean equals(Object otherTray){
		return ((Tray)otherTray).trayKey == this.trayKey;
	}
	
	/**
	 * Returns a boolean checking if the goal configuration has been reached.
	 * 
	 * @param otherTray	The goal configuration.
	 * @return			Returns true if goal has been reached, false otherwise.
	 */
	public boolean equalsGoal(Object otherTray){
		for (Block block : goalBlocks) {
			if (!myConfig.keySet().contains(block)) {
				return false;
			}
		}
		return true;
	}
	
    /**
     * Checks the invariants that make sure none of the blocks overlap and their demensions are negative, and that all blocks 
     * are completely on the tray. 
     * 
     * @return True if all tray invariants hold.
     */
    public boolean isOK() {
    	boolean [][] boardChecker = new boolean [trayLength][trayWidth];
    	for (Block block : myConfig.keySet()){
            int startRow = block.getRow();
            int startCol = block.getCol();
            int endRow = block.getLength() + startRow;
            int endCol = block.getWidth() + startCol;
            // Checks if block dimensions are not negative number or bigger than the tray dimensions.
            if (startRow < 0 || startCol < 0 || endRow < 0 || endCol < 0) {
            	throw new IllegalStateException("Move out of the tray");
            } 
            if (startRow > trayLength || startCol > trayWidth || endRow > trayLength || endCol > trayWidth) {
            	throw new IllegalStateException("Move out of the tray");
            }
            // Checks if any blocks overlap in the tray
            for (int i = startRow; i < endRow; i++) {
                    for (int j = startCol; j < endCol; j++) {
                    	if (boardChecker[i][j]) {
                    		throw new IllegalStateException("Overlap with another block.");
                    	} else {
                    		boardChecker[i][j] = true;
                    	}
                    }
            }
    	}
    	return true;
    }
	
	/**
	 * Returns a string representation of a tray configuration.
	 */
	public String toString(){
		String s = "";
		Iterator<Block> iter = myConfig.keySet().iterator();
		while(iter.hasNext()){
			Block b = iter.next();
			s = s + this.printblock(b) + "\n";
		}
		return s;
	}
	
	/**
	 * Returns a string representation of a block.
	 * 
	 * @param b	The block to represented as a string.
	 * @return	Returns a String representing the block.
	 */
	public String printblock(Block b){
		String s = b.toString() + "\n";
		for(int i = 0; i<4; i++){
			List<Block> edge = myConfig.get(b)[i];
			if(edge == null){
				s = s + ("Edge " + i + ": touching side " + i + "\n");
			}
			else{
				s = s + ("Edge " + i + ": " + myConfig.get(b)[i] + "\n");
			}
		}
		return s;
	}
	
	//A private Block class
	private class Block {

		private int myLength;
		private int myWidth;
		private int myRow;
		private int myCol;
		private String name;
		private String hashCode;
		private int blockKey;
		
		/**
		 * A constructor for the block class.
		 * 
		 * @param inputLine A string array listing the parameters and location of a block.
		 */
		public Block (String [] inputLine) {
			name = makeSentence(inputLine);
			hashCode = inputLine[2] + inputLine[3];
			myLength = Integer.parseInt(inputLine[0]);
			myWidth = Integer.parseInt(inputLine[1]);
			myRow = Integer.parseInt(inputLine[2]);
			myCol = Integer.parseInt(inputLine[3]);
			blockKey = 107*myLength*myLength*myLength+313*myWidth*myWidth*(myRow*419*myRow+myCol*947*myCol)
					+ Integer.parseInt(""+myWidth+myLength+myRow+myCol)*Integer.parseInt(""+myCol+myRow+myLength+myWidth);
		}
		
		/**
		 * Returns up to four integers representing each side a block is touching another block.
		 * 
		 * @param b	Block whose sides we're interested in
		 * @return	Integer(s) representing whether or not the block's side is touching anothers'.
		 */
		public int touchingSide(Block b){
			if (b.myRow > this.myRow - b.myLength && b.myRow < this.myRow + this.myLength){
				if (b.myCol == this.myCol + this.myWidth){
					return RIGHT_EDGE;
				}
				if (b.myCol == this.myCol - b.myWidth){
					return LEFT_EDGE;
				}
			}
			if (b.myCol > this.myCol - b.myWidth && b.myCol < this.myCol + this.myWidth){
				if (b.myRow == this.myRow - b.myLength){
					return TOP_EDGE;
				}
				if (b.myRow == this.myRow + this.myLength){
					return BOTTOM_EDGE;
				}
			}
			return NOT_TOUCHING;
		}
		
		/**
		 * Determines whether or not a block's side is touching either another block or the tray boundary.
		 * 
		 * @param direction	The block's side we're testing represented by an integer.
		 * @return			Returns a boolean if it is touching and false otherwise.
		 */
		public boolean isTouchingBoundary (int direction) {
			if (direction == TOP_EDGE){
				return myRow == 0;
			}
			if (direction == BOTTOM_EDGE){
				return myRow + myLength == trayLength;
			}
			if (direction == RIGHT_EDGE){
				return myCol + myWidth == trayWidth;
			}
			if (direction == LEFT_EDGE){
				return myCol == 0;
			}
			throw new IllegalArgumentException("There are only four edges for a block.");
		}
		
		public void addContact(Block newB, int side){
			List<Block> [] edges = myConfig.get(this);
			if (edges != null && !edges[side].contains(newB)) {
			    edges[side].add(newB);
			    myConfig.put(this, edges);
			}
		}
		
		/**
		 * updates a block's info after a move that causes it to come into contact with another block.
		 * 
		 * @param newB	The new block it's come into contact with
		 * @param side	The side of the original block that's now in contact with another block.
		 */
		public Block shiftBlock(int direction){
			Block result;
			String[] blockInfo = new String[4];
			blockInfo[0] = myLength + "";
			blockInfo[1] = myWidth + "";
			blockInfo[2] = myRow + "";
			blockInfo[3] = myCol + "";
			if (direction == TOP_EDGE){
				int newRow = myRow - 1;
				blockInfo[2] = newRow + "";
			} else if (direction == BOTTOM_EDGE){
				int newRow = myRow + 1;
				blockInfo[2] = newRow + "";
			} else if (direction == RIGHT_EDGE){
				int newCol = myCol + 1;
				blockInfo[3] = newCol + "";
			} else if (direction == LEFT_EDGE){
				int newCol = myCol - 1;
				blockInfo[3] = newCol + "";
			}
			result = new Block(blockInfo);
			return result;
			
		}
		
		/**
		 * Gets the length of a block.
		 * 
		 * @return	The integer value of a block's length.
		 */
		public int getLength(){
			return myLength;
		}
		
		/**
		 * Gets the width of a block.
		 * 
		 * @return	The integer value of a block's width.
		 */
		public int getWidth(){
			return myWidth;
		}
		
		/**
		 * Gets the row location of a block (the blocks distance from the top of the tray)
		 * 
		 * @return	The integer value of a block's row.
		 */
		public int getRow(){
			return myRow;
		}
		
		/**
		 * Gets the column location of a block (the blocks distance from the top of the tray)
		 * 
		 * @return	The integer value of a block's column.
		 */
		public int getCol(){
			return myCol;
		}
		
		/**
		 * returns an integer representation of the block.
		 * 
		 * @return	An int representation of the block.
		 */
		public int getBlockKey(){
			return this.blockKey;
		}

		/**
		 * Returns a boolean true if two block objects are equal and false otherwise.
		 * 
		 */
		public boolean equals(Object obj){
			if (((Block)obj).myLength == this.myLength && 
					((Block)obj).myWidth == this.myWidth &&
					((Block)obj).myRow == this.myRow && 
					((Block)obj).myCol == this.myCol){
				return true;
			}
			return false;
		}
		
		/**
		 * Returns a integer representation of a blocks hashcode.
		 * 
		 */
		public int hashCode(){
			return Integer.parseInt(hashCode);
		}
		
		/**
		 * Returns a string representation of a block (The 4 integers representing the block).
		 */
		public String toString(){
			return name;
		}
		
		/**
		 * Returns a string that results from concatenating a cluster of strings
		 * 
		 * @param words	a string array of strings to be concatenated.
		 * @return		Returns a concatenated final string
		 */
		public String makeSentence(String[] words) {
			   
			   StringBuffer sentence = new StringBuffer();
			   for (String w : words) {
			       sentence.append(w);
			       sentence.append(" ");
			    }
			    return sentence.toString();
		}
	}
}

