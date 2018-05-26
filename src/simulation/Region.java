package simulation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


//import sim.engine.*;
import sim.util.*;
//import sim.field.grid.*;
import sim.field.network.*;

public class Region {
	static int regionCount = 0;
	private int regionCounter;
	private int regionIndex;
	private String regionName;
	private String regionID;
	private int regionLevel;
	private int regionElevation;
	//private int regionGridElevation;
	private int type;
	private int accessibility;
	private BoundingBox boundingBox;
	private Int3D center;
	private Floor floor;
	//type
	public static final int FLOORREGION = 0;
	public static final int DOORREGION = 1;
	public static final int ELEVATORREGION = 2;	
	//accessibility
	public static final int STAFFONLY = 0;
	public static final int STAFFANDPATIENT = 1;
	public static final int GENERAL = 2;
	//collections
	private List<Int3D> gridList3D;
	private HashMap<Int3D, Tile> hashTile;
	private HashMap<Int3D, Tile> regionBorderTileHash;
	private List<Tile> regionBorderTileList;
	private HashMap<String, RegionNode> regionNode;
	private Network tilesNetwork;
	//private int priority;
	
	private boolean isElevatorLobby = false;

	//density
	private double density;
	private int humanCounter;
	private double area;


	public Region(){
		
	}
	
	public Region(int index, String name, String id, Floor floor){
		regionCount++;
		this.regionCounter = regionCount;
		this.floor = floor;
		this.regionIndex = index;
		this.regionName = name;
		this.regionID = id;
		this.regionLevel = floor.getLevel();
		this.regionElevation = floor.getElevation();
		this.gridList3D = new ArrayList<Int3D>();
		this.hashTile = new HashMap<>();
		this.regionBorderTileHash = new HashMap<>();
		this.regionBorderTileList = new ArrayList<Tile>();
		this.regionNode = new HashMap<>();
		//regionGridElevation = regionElevation / Simulation.GRIDDIMENSION;
		this.boundingBox = new BoundingBox();
		//create undirected graph
		this.tilesNetwork = new Network(false);
		//this.priority = 0;
		this.density = 0.0;
		this.humanCounter = 0;
		this.area = 0.0;
	}

	public int decrementHuman(){
		if (this.type != ELEVATORREGION){
			this.humanCounter -= 1;
			//System.out.println(this.getID());
			//System.out.println("decreased : " + Integer.toString(this.humanCounter));
		}
		return this.humanCounter;
	}

	public int incrementHuman(){
		if (this.type != ELEVATORREGION){
			this.humanCounter += 1;
			//System.out.println(this.getID());
			//System.out.println("increased : " + Integer.toString(this.humanCounter));
		}
		return this.humanCounter;
	}

	public double getArea(){
		if (this.area == 0.0) { // the area is not set et
			int tileNum = this.hashTile.size() + 1;
			this.area = tileNum * Simulation.GRIDDIMENSION * Simulation.GRIDDIMENSION / 10000;
		}
		return this.area;
	}

	public double getDensity(){
		area = this.getArea();
		if (area != 0){
			this.density = this.humanCounter/area;
			return this.density;
		}
		else {
			return 0.0;
		}
	}
	
	public boolean isElevatorLobby(){
		return this.isElevatorLobby;
	}
	
	public void setElevatorLobby(boolean isElevatorLobby){
		this.isElevatorLobby = isElevatorLobby;
	}
	
	public String getID(){
		return this.regionID;
	}
	
	public String getName(){
		return this.regionName;
	}
	
	public int getIndex(){
		return this.regionIndex;
	}
	
	public int getCounter(){
		return this.regionCounter;
	}
	
	public void setType(int type){
		this.type = type;
	}
	public int getType(){
		return this.type;
	}
	public void setAccessibility(int accessibility){
		this.accessibility = accessibility;
	}
	public int getAccessibility(){
		return this.accessibility;
	}
	
	public int getLevel(){
		return this.regionLevel;
	}
	
	public Collection<Tile> getTiles(){
		return this.hashTile.values();
	}
	
	public Collection<Tile> getBorderTiles(){
		return this.regionBorderTileList;
	}
	
	public Collection<RegionNode> getNodes(){
		return this.regionNode.values();
	}
	
	public RegionNode getNode(String ID){
		return this.regionNode.get(ID);
	}
	
	public Network getTilesNetwork(){
		return this.tilesNetwork;
	}
	/*public int getRegionGridElevation(){
		return regionGridElevation;
	}*/
	
	public int getElevation(){
		return this.regionElevation;
	}
	
	public Floor getFloor(){
		return this.floor;
	}
	
	public void addTile(Int3D gridLocation, int index){
		if(!this.hashTile.containsKey(gridLocation)){
			gridList3D.add(gridLocation);
			hashTile.put(gridLocation, new Tile(index, this, gridLocation));
			Tile tile = hashTile.get(gridLocation);
			//put tile to environmentGrid
			Simulation.environmentGrid3D.setObjectLocation(tile, gridLocation);
			//put tile as node in tiles network
			tilesNetwork.addNode(tile);
		}	
	}
	
	public void setBoundingBox(int minX, int maxX, int minY, int maxY){
		this.boundingBox.setMinX(minX);
		this.boundingBox.setMaxX(maxX);
		this.boundingBox.setMinY(minY);
		this.boundingBox.setMaxY(maxY);
		this.boundingBox.calculatePosition();
	}
	
	public BoundingBox getBoundingBox(){
		return this.boundingBox;
	}
	
	public void calculateCenter(){
		int sumX = 0;
		int sumY = 0;
		//int z = this.getRegionGridElevation();
		int z = this.getElevation();
		for (Tile tile : this.regionBorderTileHash.values()){
			sumX += tile.getX();
			sumY += tile.getY();
		}
		int n = this.regionBorderTileHash.size();
		double averageX = sumX / n;
		double averageY = sumY / n;
		int centerX = (int)Math.round(averageX);
		int centerY = (int)Math.round(averageY);
		this.center = new Int3D(centerX, centerY, z);
	}
	
	
	public Int3D getCenter(){
		return this.center;
	}
	
	public Tile getCenterTile(){
		return this.getTile(this.getCenter());
	}
	
	public void processGridNetwork(){
		//put all tiles to node;
		HashMap<Int2D, Tile> tempTileHash = new HashMap<>();
		for (Tile regionTile : this.hashTile.values()){
			int x = regionTile.getX();
			int y = regionTile.getY();
			this.tilesNetwork.addNode(regionTile);
			tempTileHash.put(new Int2D(x, y), regionTile);
		}
		for (RegionNode node : this.regionNode.values()){
			for (Tile tile : node.getTiles()){
				int x = tile.getX();
				int y = tile.getY();
				this.tilesNetwork.addNode(tile);
				tempTileHash.put(new Int2D(x, y), tile);
			}
		}
		insertEdges(tempTileHash);
	}
	
	
	private void insertEdges(HashMap<Int2D, Tile> tempTileHash){
		int minX = this.boundingBox.getMinX() - 2;
		int minY = this.boundingBox.getMinY() - 2;
		int maxX = this.boundingBox.getMaxX() + 2;
		int maxY = this.boundingBox.getMaxY() + 2;
		for (int x = minX; x <= maxX; x++){
			for (int y = minY; y <= maxY; y++){		
				Int2D ploc1 = new Int2D(x, (y-1));
				Int2D ploc2 = new Int2D((x-1), y);
				Int2D ploc3 = new Int2D(x, (y+1));
				Int2D loc= new Int2D(x, y);
				createEdge(ploc1, loc, Simulation.TILESIZE, tempTileHash);//vertical edge
				createEdge(ploc2, loc, Simulation.TILESIZE, tempTileHash);//horizontal edge
				Tile tileLoc1 = tempTileHash.get(ploc1);
				Tile tileLoc2 = tempTileHash.get(ploc2);
				Tile tileLoc3 = tempTileHash.get(ploc3);
				if (tileLoc1 != null && tileLoc2 != null){
					Int2D ploc4 = new Int2D((x-1), (y-1));
					createEdge(ploc4, loc, Simulation.DIAGONALTILESIZE, tempTileHash);//upright diagonal edge
				}
				if (tileLoc1 != null && tileLoc3 != null){
					Int2D ploc5 = new Int2D((x-1), (y+1));
					createEdge(ploc5, loc, Simulation.DIAGONALTILESIZE, tempTileHash);//downright diagonal edge
				}
				
			}
		}
	}
	
	
	private void createEdge(Int2D pLoc, Int2D loc, double weight, HashMap<Int2D, Tile> tempTileHash){
		Tile pTile = tempTileHash.get(pLoc);
		Tile tile = tempTileHash.get(loc);
		if(pTile != null && tile != null){
			this.tilesNetwork.addEdge(pTile, tile, weight);
		}
	}

	boolean isTile(Object obj){
		boolean isTile = false;
		if (obj.getClass().equals(Tile.class)){
			isTile = true;
		}
		return isTile;
	}
	
	public boolean isTile(int x, int y){
		//Int3D loc = new Int3D(x, y, this.regionGridElevation);
		Int3D loc = new Int3D(x, y, this.regionElevation);
		return this.hashTile.containsKey(loc);
		/*Tile tile = this.hashTile.get(loc);
		if (tile != null){
			return true;
		} else {
			return false;
		}*/
	}
	
	public void detectBorder(){
		// first tile detection
		int[] firstTile = this.getFirstTile();
		int currentX = firstTile[0];
		int currentY = firstTile[1];
		int pX = firstTile[2];
		int pY = firstTile[3];
		//int z = this.regionGridElevation;
		int z = this.regionElevation;
		Int3D borderMap = new Int3D(currentX, currentY, z);
		Tile borderTile = this.hashTile.get(borderMap);
		this.addBorderTile(borderMap,  borderTile);
		//begin detecting another tiles
		int index = MooreNeighbor.getNeighborIndex(currentX, currentY, pX, pY);
		int firstTileX = currentX;
		int firstTileY = currentY;
		boolean isFinish = false;
		while(isFinish == false){
			int[] nextTile = findTile(currentX, currentY, index);
			currentX = nextTile[0];
			currentY = nextTile[1];
			index = nextTile[2];
			if (currentX == firstTileX && currentY == firstTileY){			
				isFinish = true;
			}
			Int3D mapKey = new Int3D(currentX,currentY, z);
			Tile tile = this.hashTile.get(mapKey);
			if(!this.regionBorderTileHash.containsKey(mapKey)){
				this.addBorderTile(mapKey,  tile);	
			}			
		}
	}
	
	int[] getFirstTile(){
		int minX = this.boundingBox.getMinX() - 1;
		int maxX = this.boundingBox.getMaxX() + 1;
		int minY = this.boundingBox.getMinY() - 1;
		int maxY = this.boundingBox.getMaxY() + 1;
		//int z = this.getRegionGridElevation();	
		int z = this.getElevation();
		int pX = 0;
		int pY = 0;
		int currentX = 0;
		int currentY = 0;		
		endGetXY:
		for (int x = minX; x < maxX; x++){
			for(int y = minY; y < maxY; y++){
				Int3D loc = new Int3D(x, y, z);
				Tile tile = this.hashTile.get(loc);
				if(tile != null){
					currentX = x;
					currentY = y;
					pX = currentX;
					pY = currentY - 1;
					break endGetXY;			
				}
			}
		}
		int[] result = new int[] {currentX, currentY, pX, pY};
		return result;
	}
	
	int[] findTile(int x, int y, int pIndex){
		int index = pIndex;
		int [] result = new int [3];
		found:
		for (int i = 0; i < 8; i++){
			index++;
			if (index == 8){
				index = 0;
			}
			int[] nextTile = MooreNeighbor.neighBorList[index];
			int findX = x + nextTile[0];
			int findY = y + nextTile[1];
			boolean isFound = isTile(findX, findY);
			if (isFound == true){ 
				int nextPreviousIndex = entranceMap(index);
				result[0] = findX;
				result[1] = findY;
				result[2] = nextPreviousIndex;
				break found;
			}
		}
		return result;
	}
	
	private void addBorderTile(Int3D key, Tile values){
		this.regionBorderTileHash.put(key,  values);
		this.regionBorderTileList.add(values);
	}
	
	int entranceMap(int index){
		int result;
		switch(index){
		case 0: case 1:
			result = 6;
			break;
		case 2: case 3:
			result = 0;
			break;
		case 4: case 5:
			result = 2;
			break;
		case 6: case 7:
			result =  4;
			break;
		default:
			result =  9;
			break;
		}
		return result;
	}
	
	public void detectNode(){
		//iterate around the border
		for (Tile tile : this.regionBorderTileList){
			int x = tile.getX();
			int y = tile.getY();
			int z = tile.getZ();
			for (int i = 0; i < 4; i++){
				int[] neighbor = VonNeumannNeighbor.neighBorList[i];
				int checkX = x + neighbor[0];
				int checkY = y + neighbor[1];
				Int3D loc = new Int3D(checkX, checkY, z);
				Tile tileNeighbor = getTileID(loc);
				if (tileNeighbor != null){ // found a tile
					String id = tileNeighbor.getID();
					if (!this.regionNode.containsKey(id)){// the found tile belong to unassigned region
						this.regionNode.put(id,  new RegionNode(id, this.getID()));
						RegionNode node = this.regionNode.get(id);
						node.addTile(new Int3D(checkX, checkY, z), tileNeighbor);
					} else { //if the found tile already has a node to put
						RegionNode node = this.regionNode.get(id);
						if(node.isEmpty(loc)){
							node.addTile(loc, tileNeighbor);
						}
					}
				}			
			}
		}
		processNode();
	}
	
	public Tile getTileID(Int3D loc){
		Tile result = null;
			//check if this is on its own region or not
			boolean isHere = this.hashTile.containsKey(loc);
			if (isHere == false){
				Bag bag = Simulation.environmentGrid3D.getObjectsAtLocation(loc);
				if(bag != null && !bag.isEmpty()){
					Tile tile = (Tile)bag.get(0);
					//String id = tile.getID();
					if (tile != null){
						result = tile;
					}
				}
			}
		return result;
	}
	
	public Tile getTile(Int3D loc){
		Tile tile = this.hashTile.get(loc);
		return tile;
	}
	
	void processNode(){
		// iterate all node
		for(RegionNode node : regionNode.values()){
			node.setCenter();
			node.setWeightFromCenter(this.center);
		}
	}
	
	public void clear(){
		
	}
	
	//overides equals
	public boolean equals(Object obj){
		Region region = (Region)obj;
		if (region.getID().equals(this.getID())){
			return true;
		} else {
			return false;
		}
	}


	public void setID(String regionID) {
		this.regionID = regionID;
	}
}