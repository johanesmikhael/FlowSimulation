package simulation;

public class VonNeumannNeighbor {
	public static final int[][] neighBorList = new int[][] {{0,-1}, {-1,0}, {0,1}, {1,0}};
	
	public static int getVonNeumannIndex(int x, int y, int neighborX, int neighborY){
		int returnValue = 0;
		int dX = neighborX - x;
		int dY = neighborY - y;
		boolean found = false;
		for (int i = 0; i < 4; i++){
			int[] nb = neighBorList[i];
			if(nb[0] == dX && nb[1] == dY){
				returnValue = i;
				found = true;
				break;
			}
		}
		if (found == false){
			returnValue = 5;
			System.out.println("NOT IN VON NEUMANN NEIGHBOR");
		}
		return returnValue;
	}
}
