package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/*
 * PathfindingMatrix creates a representation of a graph from a 2D array of objects
 * Used for pathfinding through the maze*/
public class PathfindingMatrix{

	/**Maps a coordinate pair to a single int, e.g [1][1] => 1, [1][2] => 2, etc...*/
	HashMap<String, Integer> map = new HashMap<String, Integer>(); 
	HashMap<Integer, String> reverseMap = new HashMap<Integer, String>(); // Used to map those node IDs back to our coordinates
	private int[][] matrix;
	private int[] dimensions = new int[2];
	
	public PathfindingMatrix(Object[][] array) {
		for (Integer i = 0; i < array.length; i++) {
			for (Integer j = 0; j < array[0].length; j++) {
				if (!(array[i][j] instanceof SolidWall)) {
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
				if (!(array[i][j] instanceof SolidWall)) {
					if ((i == 0) && !(array[array.length-1][j] instanceof SolidWall)) { 
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {array.length-1, j});
						
						//Also check for (valid) regular neighbours
						if (!(array[i+1][j] instanceof SolidWall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i+1, j});
						}
						continue;
					}
					else if (((i == array.length-1) && !(array[0][j] instanceof SolidWall))) {
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {array.length-1, j});
						
						//Also check for (valid) regular neighbours
						if (!(array[i-1][j] instanceof SolidWall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i-1, j});
						}
						continue;
					}
					
					if ((j == 0) && !(array[i][array[0].length-1] instanceof SolidWall)) {
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {i, array[0].length-1});
						
						//Also check for (valid) regular neighbours
						if (!(array[i][j+1] instanceof SolidWall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i, j+1});
						}
						continue;
					}
					else if ((j == array[0].length-1) && !(array[i][0] instanceof SolidWall)) {
						// If a non-wall piece is on the edge of the map, check if the side opposite is also free, if so, we can connect the two
						addEdge(new Integer[] {i,j}, new Integer[] {i, array[0].length-1});
						
						//Also check for (valid) regular neighbours
						if (!(array[i][j-1] instanceof SolidWall)) {
							addEdge(new Integer[] {i, j}, new Integer[] {i, j-1});
						}
						continue;
					}
					//Else, see if regular neighbours are connectable
					if (!(array[i][j+1] instanceof SolidWall) && (j!= array[0].length-1)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i, j+1});
					}
					if (!(array[i][j-1] instanceof SolidWall) && (j != 0)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i, j-1});
					}
					if (!(array[i+1][j] instanceof SolidWall) && (i != array.length-1)) {
						addEdge(new Integer[] {i, j}, new Integer[] {i+1, j});
					}
					if (!(array[i-1][j] instanceof SolidWall) && (i != 0)) {
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
	
	//Private overloaded version to save from having to convert there and back
	private boolean isConnected(Integer source, Integer destination) {
		if ((matrix[source][destination] == 1) || (matrix[destination][source] == 1)) {
			return true;
		}
		else {return false;}
	}
	
	public ArrayList<Main.Direction> findDijkstraPath(Integer[] source, Integer[] destination){
		Integer sourceIndex = map.get(Arrays.toString(source));
		Integer destinationIndex = map.get(Arrays.toString(destination));
		int currentNode;
		
		ArrayList<Integer> path = new ArrayList<Integer>(); // Our shortest path in terms of graph node indices e.g 1-> 2 -> 4
		ArrayList<Integer[]> coordPath = new ArrayList<Integer[]>(); // Our shortest path in terms of coordinates e.g (1,2)-> (2,2) -> (3,2)
		ArrayList<Main.Direction> directionPath  = new ArrayList<Main.Direction>(); // Our node in terms of directions, e.g left -> up, -> down...
		
		int numElements = map.size(); 
		Queue<Integer> unexplored = new LinkedList<Integer>(); // Vertices remaining
		Queue<Integer> explored = new LinkedList<Integer>(); // Vertices already explored
		
		Integer[] parent = new Integer[numElements]; // Previously visited node in path
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

		/*Once we have the parent array, we can follow it back to find the shortest path*/
		path = findPathFromParents(sourceIndex, destinationIndex, parent);

		/*Then we need to convert from our array indices back to the grid coordinates*/
		coordPath = convertPathToCoordinates(path);

		/* The we need to convert those coordinates into directions for the ghost to follow*/
		directionPath = convertCoordPathToDirections(coordPath);
		
		return directionPath;
	}
	
	/* BFS does fine with short paths for unweighted graphs. No guarantees on short-est- path though*/
	public ArrayList<Main.Direction> findBFSPath(Integer[] source, Integer[] destination){
		int sourceIndex = map.get(Arrays.toString(source));
		int destinationIndex = map.get(Arrays.toString(destination));
		
		Queue<Integer> queue = new PriorityQueue<Integer>();
		
		Integer[] visited = new Integer[map.size()];
		Arrays.fill(visited, 0);
		visited[sourceIndex] = 1;
		
		Integer[] traceBack = new Integer[map.size()];
		Arrays.fill(traceBack, 0);
		traceBack[sourceIndex] = -1;
		
		queue.add(sourceIndex);
		
		while (!queue.isEmpty()) {
			Integer path = queue.poll();
			ArrayList<Integer> neighbours = new ArrayList<Integer>();
			neighbours = getNeighbours(path);
			for (Integer node : neighbours) {
				if (visited[node] != 1) {
					queue.add(node);
					visited[node] = 1;
					traceBack[node] = path;
				}				
			}
		}
		
		ArrayList<Integer> path = new ArrayList<Integer>(); // Our shortest path in terms of graph node indices e.g 1-> 2 -> 4
		ArrayList<Integer[]> coordPath = new ArrayList<Integer[]>(); // Our shortest path in terms of coordinates e.g (1,2)-> (2,2) -> (3,2)
		ArrayList<Main.Direction> directionPath  = new ArrayList<Main.Direction>(); // Our node in terms of directions, e.g left -> up, -> down...
		
		/*Once we have the parent array, we can follow it back to find the shortest path*/
		path = findPathFromParents(sourceIndex, destinationIndex, traceBack);

		/*Then we need to convert from our array indices back to the grid coordinates*/
		coordPath = convertPathToCoordinates(path);
		
		/* The we need to convert those coordinates into directions for the ghost to follow*/
		directionPath = convertCoordPathToDirections(coordPath);
		return directionPath;
	}
	
	/* DFS usually takes a pretty winding path */
	public ArrayList<Main.Direction> findDFSPath(Integer[] source, Integer[] destination){
		int sourceIndex = map.get(Arrays.toString(source));
		int destinationIndex = map.get(Arrays.toString(destination));
		int currentNode;
		
		ArrayList<Main.Direction> directionPath = new ArrayList<Main.Direction>();
		ArrayList<Integer[]> coordPath = new ArrayList<Integer[]>();
		ArrayList<Integer> path = new ArrayList<Integer>();
		
		Integer[] parent = new Integer[map.size()]; // We will use this to trace back our path
		Arrays.fill(parent, -1);
		
		Set<Integer> visited = new HashSet<Integer>(); // Store visited nodes to prevent cycles
		
		Stack<Integer> stack = new Stack<Integer>(); // Nodes waiting to be explored
		stack.push(sourceIndex);
		
		while(!stack.isEmpty()) {
			currentNode = stack.pop();
			if (visited.contains(currentNode)) {
				continue;
			}
			if (currentNode == destinationIndex) {
				break;
			}
			visited.add(currentNode);
			ArrayList<Integer> neighbours = new ArrayList<Integer>();
			neighbours = getNeighbours(currentNode);
			for (Integer node : neighbours) {
				if (!visited.contains(node)) {
					stack.push(node);
					if (parent[node] == -1) {
						parent[node] = currentNode;
					}
				}
			}
		}
		
		/*Once we have the parent array, we can follow it back to find a path*/
		path = findPathFromParents(sourceIndex, destinationIndex, parent);
		
		/*Then we need to convert from our array indices back to the grid coordinates*/
		coordPath = convertPathToCoordinates(path);
	
		/* Then we need to convert those coordinates into directions for the ghost to follow*/
		directionPath = convertCoordPathToDirections(coordPath);
		
		return directionPath;
	}

	private ArrayList<Integer> findPathFromParents(Integer source, Integer destination, Integer[] parent){
		ArrayList<Integer> path = new ArrayList<Integer>();
		
		int currentParent = destination;
		
		path.add(destination); // Start at destination
		while (currentParent != source) {
			if (parent[currentParent] == -1) { // If our node has no parent, we can't reach the source
				throw new ArithmeticException("No path from source to destination");
			}
			path.add(parent[currentParent]); // Add our current node's parent
			currentParent = parent[currentParent]; // And update our current node to be the node visited before this one
		}
		Collections.reverse(path); // Since we went from destination -> source, we reverse the order to get source -> destination
		return path;
	}
	
	private ArrayList<Integer[]> convertPathToCoordinates(ArrayList<Integer> path) {
		ArrayList<Integer[]> coordPath = new ArrayList<Integer[]>();
		for (Integer node : path) {
			//Splice our string "[x, y]" into "x" and "y" 
			String[] stringCoords = (reverseMap.get(node)).replace("[", "").replace("]", "").split(", "); 
			
			// Parse our "x" and "y" strings into ints
			Integer[] coords = {Integer.parseInt(stringCoords[0]), Integer.parseInt(stringCoords[1])}; 
			
			// Add {x,y} to our coordPath
			coordPath.add(coords); 
		}
		return coordPath;
	}
	
	private ArrayList<Main.Direction> convertCoordPathToDirections(ArrayList<Integer[]> coordPath){
		// Convert coordinates into moves e.g {(1,1) -> (1,2) -> (2,2)} => {right -> down}
		ArrayList<Main.Direction> directionPath = new ArrayList<Main.Direction>();
		
		for (Integer i = 0; i < coordPath.size()-1; i++) {
			// If move is horizontal...
			if (coordPath.get(i)[0] == coordPath.get(i+1)[0]) { 
				//If we need to wrap around the screen...
				if (coordPath.get(i)[1] == 0 && coordPath.get(i+1)[1] == dimensions[0] - 1) {
					directionPath.add(Main.Direction.LEFT); // Wrap right
				}
				else if (coordPath.get(i+1)[1] == 0 && coordPath.get(i)[1] == dimensions[0] - 1) {
					directionPath.add(Main.Direction.RIGHT); // Wrap left
				}
				
				//Otherwise regular movements
				else if (coordPath.get(i)[1] < coordPath.get(i+1)[1]) {
					directionPath.add(Main.Direction.RIGHT);
				}
				else if (coordPath.get(i)[1] > coordPath.get(i+1)[1]) {
					directionPath.add(Main.Direction.LEFT);
				}
				else {
					throw new ArithmeticException("Path involves not moving!");
				}
			}
			
			// If move is vertical...
			else if (coordPath.get(i)[1] == coordPath.get(i+1)[1]) { 
				//If we need to wrap around the screen...
				if (coordPath.get(i)[0] == 0 && coordPath.get(i+1)[0] == dimensions[1] - 1) {
					directionPath.add(Main.Direction.UP); // Wrap down to the bottom
				}
				else if (coordPath.get(i+1)[0] == 0 && coordPath.get(i)[0] == dimensions[1] - 1) {
					directionPath.add(Main.Direction.DOWN); // Wrap up to the top
				}
				
				//Otherwise regular movements
				else if (coordPath.get(i)[0] < coordPath.get(i+1)[0]) {
					directionPath.add(Main.Direction.DOWN);
				}
				else if (coordPath.get(i)[0] > coordPath.get(i+1)[0]) {
					directionPath.add(Main.Direction.UP);
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
	
	public Main.Direction findEuclideanDirection(Integer[] source, Integer[] destination, boolean getCloser) {
		/* Finds the direction which minimises/maximises (depending on getCloser) the euclidean distance to the destination.
		 * Returns null if staying still is the best move */
		int sourceIndex = map.get(Arrays.toString(source));
		
		Main.Direction direction;
		
		ArrayList<Integer> neighbours = new ArrayList<Integer>();
		neighbours = getNeighbours(sourceIndex);
		
		Double[] distanceArray = new Double[neighbours.size()];
		Arrays.fill(distanceArray, Double.POSITIVE_INFINITY);
		
		/* Loop through node's neighbours, store distance from this node to destination*/
		for (int i = 0; i < neighbours.size(); i++) {
			//Splice our string "[x, y]" into "x" and "y" 
			String[] stringCoords = (reverseMap.get(neighbours.get(i))).replace("[", "").replace("]", "").split(", "); 

			// Parse our "x" and "y" strings into ints
			Integer[] coords = {Integer.parseInt(stringCoords[0]), Integer.parseInt(stringCoords[1])}; 
			distanceArray[i] = calcDistance(coords, destination);
		}

		int index;
		if (getCloser) {
			/*Find min distance, and consequently the index of the point that gives least distance*/
			Double minDist = Double.POSITIVE_INFINITY;
			int minIndex = 0;
			
			for (int i = 0; i < distanceArray.length; i++) {
				if (distanceArray[i] < minDist) {
					
					minDist = distanceArray[i];
					minIndex = i;
				}
			}
			index = minIndex;
			
			/* If we are better off not moving, return null*/
			if(minDist > calcDistance(source, destination)) {
				return null;
			}
		}
		else {
			/*Find max distance, and consequently the index of the point that gives most distance*/
			Double maxDist = 0.0;
			int maxIndex = 0;
			
			for (int i = 0; i < distanceArray.length; i++) {
				if (distanceArray[i] > maxDist) {
					
					maxDist = distanceArray[i];
					maxIndex = i;
				}
			}
			index = maxIndex;
			
			/* If we are better off not moving, return null*/
			if(maxDist < calcDistance(source, destination)) {
				return null;
			}
		}
		
		/*Convert the coordinates of our point that minimises distance*/
		String[] minStringCoords = (reverseMap.get(neighbours.get(index))).replace("[", "").replace("]", "").split(", "); 
		Integer[] minCoords = {Integer.parseInt(minStringCoords[0]), Integer.parseInt(minStringCoords[1])}; 
		
		ArrayList<Integer[]> coordPath = new ArrayList<Integer[]>();
		coordPath.add(source);
		coordPath.add(minCoords);
		
		/*Find direction from the two coordinates*/
		ArrayList<Main.Direction> directionPath = new ArrayList<Main.Direction>();
		directionPath = convertCoordPathToDirections(coordPath);

		direction = directionPath.get(0);
		return direction;
		
	}
	public static Double calcDistance(Integer[] source, Integer[] destination) {
		// sqrt( (x1-x2)^2 + (y1-y2)^2 )
		
		Double total = 0.0;
		for (int i = 0; i < source.length; i++) {
			Double diff = (double) (source[i] - destination[i]);
			diff = Math.pow(diff, 2);
			total += diff;
		}
		total = Math.sqrt(total);
		
		return total;
	}
	
	private ArrayList<Integer> getNeighbours(Integer currentNode) {
		ArrayList<Integer> neighbours = new ArrayList<Integer>();
		int numElements = map.size();
		
		for (int testNode = 0; testNode < numElements; testNode++) {
			if (isConnected(currentNode, testNode) && (currentNode != testNode)) {
				neighbours.add(testNode);
			}
		}
		return neighbours;
	}
	
}
