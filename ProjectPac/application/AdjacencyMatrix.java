package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/*
 * AdjacencyMatrix creates a representation of a graph from a 2D array of objects
 * Used for calculating paths through the maze
 *  
 *  */
public class AdjacencyMatrix{
	
	HashMap<String, Integer> map = new HashMap<String, Integer>();
	private int[][] matrix;
	
	public AdjacencyMatrix(Object[][] array) {
		for (Integer i = 0; i < array.length; i++) {
			for (Integer j = 0; j < array[0].length; j++) {
				if (!(array[i][j] instanceof Wall)) {
					System.out.println(Arrays.toString(new Integer[] {i,j}) + " is not a wall, mapping it to " + Integer.toString(map.size()));
					map.put(Arrays.toString(new Integer[] {i,j}), map.size());
				}
			}
		}
		
		matrix = new int[map.size()][map.size()]; // number of vertices is number of entries in our hashmap
		
		for (Integer i = 0; i < map.size();i++) {
			matrix[i][i] = 1; // Connect every vertex to itself
		}
		System.out.println(map.values());
		
		for (Integer i = 0; i < array.length; i++) {
			for (Integer j = 0; j < array[0].length; j++) { // Check to see if connected to neighbours
				if (!(array[i][j] instanceof Wall)) {
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
		System.out.println(Arrays.deepToString(matrix).replace("], ", "]\n"));
	}
	
	public void addEdge(Integer[] source, Integer[] destination) {
		System.out.println(Arrays.toString(source) + Arrays.toString(destination));
		matrix[map.get(Arrays.toString(source))][map.get(Arrays.toString(destination))] = 1;
	}
	
	public boolean isConnected(Integer[] source, Integer[] destination) {
		if (matrix[map.get(Arrays.toString(source))][map.get(Arrays.toString(destination))] == 1) {
			return true;
		}
		else {return false;}
	}
	
	public boolean isConnected(Integer source, Integer destination) {
		if (matrix[source][destination] == 1) {
			return true;
		}
		else {return false;}
	}
	
	public ArrayList<Main.Direction> findDijkstraPath(Integer[] source, Integer[] destination){
		int sourceIndex = map.get(Arrays.toString(source));
		int destinationIndex = map.get(Arrays.toString(destination));
		int currentNode;
		ArrayList<Main.Direction> path  = new ArrayList<Main.Direction>();
		int numElements = map.size() - 1; 
		Queue<Integer> unexplored = new LinkedList<Integer>(); // Vertices remaining
		Queue<Integer> explored = new LinkedList<Integer>(); // Vertices already explored
		int[] parent = new int[numElements]; // Previously visited node in path
		
		
		double[] shortestDistance = new double[numElements]; //Currently known shortest distance form source to node
		for (int i = 0; i < shortestDistance.length; i++){ //Initialise shortest distance array
			if (i == (double)map.get(Arrays.toString(source))){
				shortestDistance[i] = 0.0; // If this is the node we start at, it is distance zero
			}
			else {
				shortestDistance[i] = Double.POSITIVE_INFINITY;
			}
		}
		unexplored.add(sourceIndex);
		while (!unexplored.isEmpty()){
			currentNode = unexplored.poll();
			
			explored.add(currentNode);
			for (int testNode = 0; testNode < numElements; testNode++){
				if (isConnected(source, destination) && explored.contains(testNode)){
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
		int currentParent = destinationIndex;
		// TO BE CONTINUED
		return path;
	}
	
}
