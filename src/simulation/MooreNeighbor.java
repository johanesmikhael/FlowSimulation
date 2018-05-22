package simulation;

public class MooreNeighbor {
	public static final int[][] neighBorList = new int[][] {{0,-1}, {-1,-1}, {-1,0}, {-1,1}, {0,1}, {1,1}, {1,0}, {1,-1}};
	
	public static int getNeighborIndex(int x, int y, int neighborX, int neighborY){
		int returnValue = 0;
		int dX = neighborX - x;
		int dY = neighborY - y;
		//System.out.println(dX + ", " + dY);
		boolean found = false;
		for (int i = 0; i < 8; i++){
			int[] nb = neighBorList[i];
			if(nb[0] == dX && nb[1] == dY){
				returnValue = i;
				found = true;
				break;
			}
		}
		if (found == false){
			returnValue = 9;
			System.out.println("CURRENT TILE DOESNT EXIST IN MOORE NEIGHBORHOOD AREA");
		}
		return returnValue;
	}
}
