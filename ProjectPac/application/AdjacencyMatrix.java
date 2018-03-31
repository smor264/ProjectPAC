package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/*
 * AdjacencyMatrix creates a representation of a graph from a 2D array of objects
 * Used for calculating paths through the maze
 *  
 *  */
public class AdjacencyMatrix{

	HashMap<String, Integer> map = new HashMap<String, Integer>(); // Used to map coordinate pairs to a unique node ID
	HashMap<Integer, String> reverseMap = new HashMap<Integer, String>(); // Used to map those node IDs back to our coordinates
	private int[][] matrix;
	private int[] dimensions = new int[2];
	
	public AdjacencyMatrix(Object[][] array) {
		for (Integer i = 0; i < array.length; i++) {
			for (Integer j = 0; j < array[0].length; j++) {
				if (!(array[i][j] instanceof Wall)) {
					map.put(Arrays.toString(new Integer[] {i,j}), map.size());
					reverseMap.put(map.size() - 1, Arrays.toString(new Integer[] {i,j}));
				}
			}
		}
		
		matrix = new int[map.size()][map.size()]; // number of vertices is number of entries in our hashmap
		
		for (Integer i = 0; i < map.size();i++) {
			matrix[i][i] = 1; // Connect every vertex to itself
		}
		
		for (Integer i = 0; i < array.length; i++) {
			for (Integer j = 0; j < array[0].length; j++) { // Check to see if connected to neighbours
				if (!(array[i][j] instanceof Wall)) {
					if ((i == 0) && !(array[array.length-1][j] instanceof Wall)) { 
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {array.length-1, j});
						
						//Also check for (valid) regular neighbours
						if (!(array[i+1][j] instanceof Wall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i+1, j});
						}
						continue;
					}
					else if (((i == array.length-1) && !(array[0][j] instanceof Wall))) {
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {array.length-1, j});
						
						//Also check for (valid) regular neighbours
						if (!(array[i-1][j] instanceof Wall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i-1, j});
						}
						continue;
					}
					
					if ((j == 0) && !(array[i][array[0].length-1] instanceof Wall)) {
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {i, array[0].length-1});
						
						//Also check for (valid) regular neighbours
						if (!(array[i][j+1] instanceof Wall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i, j+1});
						}
						continue;
					}
					else if ((j == array[0].length-1) && !(array[i][0] instanceof Wall)) {
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {i, array[0].length-1});
						
						//Also check for (valid) regular neighbours
						if (!(array[i][j-1] instanceof Wall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i, j-1});
						}
						continue;
					}
					//Else, see if regular neighbours are connectable
					if (!(array[i][j+1] instanceof Wall) && (j!= array[0].length-1)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i, j+1});
					}
					if (!(array[i][j-1] instanceof Wall) && (j != 0)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i, j-1});
					}
					if (!(array[i+1][j] instanceof Wall) && (i != array.length-1)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i+1, j});
					}
					if (!(array[i-1][j] instanceof Wall) && (i != 0)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i-1, j});
					}
				}
			}
		}
		dimensions[0] = array[0].length;
		dimensions[1] = array.length;
	}
	
	public void addEdge(Integer[] source, Integer[] destination) {
		matrix[map.get(Arrays.toString(source))][map.get(Arrays.toString(destination))] = 1;
		matrix[map.get(Arrays.toString(destination))][map.get(Arrays.toString(source))] = 1;
	}
	
	public boolean isConnected(Integer[] source, Integer[] destination) {
		if (matrix[map.get(Arrays.toString(source))][map.get(Arrays.toString(destination))] == 1 || matrix[map.get(Arrays.toString(destination))][map.get(Arrays.toString(source))] == 1) {
			return true;
		}
		else {return false;}
	}
	
	//Overloaded function
	private boolean isConnected(Integer source, Integer destination) {
		if ((matrix[source][destination] == 1) || (matrix[destination][source] == 1)) {
			return true;
		}
		else {return false;}
	}
	
	public ArrayList<Main.Direction> findDijkstraPath(Integer[] source, Integer[] destination){
		int sourceIndex = map.get(Arrays.toString(source));
		int destinationIndex = map.get(Arrays.toString(destination));
		int currentNode;
		
		ArrayList<Integer> path = new ArrayList<Integer>(); // Our shortest path in terms of graph node indices e.g 1-> 2 -> 4
		ArrayList<Integer[]> coordPath = new ArrayList<Integer[]>(); // Our shortest path in terms of coordinates e.g (1,2)-> (2,2) -> (3,2)
		ArrayList<Main.Direction> directionPath  = new ArrayList<Main.Direction>(); // Our node in terms of directions, e.g left -> up, -> down...
		
		int numElements = map.size(); 
		Queue<Integer> unexplored = new LinkedList<Integer>(); // Vertices remaining
		Queue<Integer> explored = new LinkedList<Integer>(); // Vertices already explored
		
		int[] parent = new int[numElements]; // Previously visited node in path
		Arrays.fill(parent, -1);
		parent[sourceIndex] = 0;
		
		/*Initialise shortest path array*/
		double[] shortestDistance = new double[numElements]; //Currently known shortest distance form source to node
		for (int i = 0; i < shortestDistance.length; i++){ //Initialise shortest distance array
			if (i == (double)map.get(Arrays.toString(source))){
				shortestDistance[i] = 0.0; // If this is the node we start at, it is distance zero
			}
			else {
				shortestDistance[i] = Double.POSITIVE_INFINITY;
			}
		}
		
		/* Calculate shortest path array */
		unexplored.add(sourceIndex);
		while (!unexplored.isEmpty()){
			currentNode = unexplored.poll();
			
			
			
			explored.add(currentNode);
			for (int testNode = 0; testNode < numElements; testNode++){
				if (isConnected(currentNode, testNode) && !(explored.contains(testNode))){
					if (shortestDistance[testNode] > shortestDistance[currentNode] + 1){
						shortestDistance[testNode] = shortestDistance[currentNode] + 1;
						parent[testNode] = currentNode;

					}
					if(!unexplored.contains(testNode)){
						unexplored.add(testNode);
					}
				}
			}
		}

		/*Find shortest path nodes*/
		int currentParent = destinationIndex;
		path.add(destinationIndex); // Start at destination
		while (currentParent != sourceIndex) {
			if (parent[currentParent] == -1) { // If our node has no parent, we can't reach the source
				throw new ArithmeticException("No path from source to destination");
			}
			path.add(parent[currentParent]); // Add our current node's parent
			currentParent = parent[currentParent]; // And update our current node to be the node visited before this one
		}
		Collections.reverse(path); // Since we went from destination -> source, we reverse the order to get source -> destination

		/*Translate graph nodes into coordinates*/
		for (Integer node : path) {
			String[] stringCoords = (reverseMap.get(node)).replace("[", "").replace("]", "").split(", "); //Splice our string "[x, y]" into "x" and "y" 
			Integer[] coords = {Integer.parseInt(stringCoords[0]), Integer.parseInt(stringCoords[1])}; // Parse our "x" and "y" strings into ints
			
			coordPath.add(coords); // Add {x,y} to our coordPath
		}
		
		/*Translate coordinates into directions*/
		for (Integer i = 0; i < coordPath.size()-1; i++) {// Convert coordinates into moves e.g {(1,1) -> (1,2) -> (2,2)} => {right -> down}
			if (coordPath.get(i)[0] == coordPath.get(i+1)[0]) { // If move is horizontal
				//If we need to wrap around the screen...
				if (coordPath.get(i)[1] == 0 && coordPath.get(i+1)[1] == dimensions[0] - 1) {
					directionPath.add(Main.Direction.left); // Wrap right
				}
				else if (coordPath.get(i+1)[1] == 0 && coordPath.get(i)[1] == dimensions[0] - 1) {
					directionPath.add(Main.Direction.right); // Wrap left
				}
				
				//Otherwise regular movements
				else if (coordPath.get(i)[1] < coordPath.get(i+1)[1]) {
					directionPath.add(Main.Direction.right);
				}
				else if (coordPath.get(i)[1] > coordPath.get(i+1)[1]) {
					directionPath.add(Main.Direction.left);
				}
				else {
					throw new ArithmeticException("Path involves not moving!");
				}
			}
			else if (coordPath.get(i)[1] == coordPath.get(i+1)[1]) { // If move is vertical
				//If we need to wrap around the screen...
				if (coordPath.get(i)[0] == 0 && coordPath.get(i+1)[0] == dimensions[1] - 1) {
					directionPath.add(Main.Direction.up); // Wrap down to the bottom
				}
				else if (coordPath.get(i+1)[0] == 0 && coordPath.get(i)[0] == dimensions[1] - 1) {
					directionPath.add(Main.Direction.down); // Wrap up to the top
				}
				
				//Otherwise regular movements
				else if (coordPath.get(i)[0] < coordPath.get(i+1)[0]) {
					directionPath.add(Main.Direction.down);
				}
				else if (coordPath.get(i)[0] > coordPath.get(i+1)[0]) {
					directionPath.add(Main.Direction.up);
				}
				else {
					throw new ArithmeticException("Path involves not moving!");
				}
			}
			else { // Path moves both vertically and horizontally 
				throw new ArithmeticException("Path involves moving diagonally!");
			}
		}
		return directionPath;
	}
	
}
