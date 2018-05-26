package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Int3D;

public class Path extends Route {
	
	boolean isSameRegion;
	HashMap<String, RegionPath> regionPathHash;
	HashMap<String, String> goToRegion;
	
	public Path (Tile tileStart, Tile tileFinish, Network network){
		this.uuid = UUID.randomUUID();
		this.pathID = uuid.toString();
		this.network = network;
		this.isSameRegion = false;
		this.regionPathHash = new HashMap<>();
		this.goToRegion = new HashMap<>();
		setPath(tileStart, tileFinish);
	}
	
	boolean setPath(Tile tileStart, Tile tileFinish){
		setRoute(tileStart, tileFinish);
		boolean isSameRegion = setRegionPath();
		return isSameRegion;
	}
	
	boolean setRegionPath(){
		//System.out.println(this.regionList.size());
		if (this.regionList.size() > 1){
			Tile nextTileStart = null;
			for (int i = 0; i < regionList.size(); i++){
				if (i == 0){ //the start of path
					Region currentRegion = this.regionList.get(i);
					//System.out.println("test01");
					//System.out.println(currentRegion.getID());
					Tile tileStart = this.tileStart;
					//System.out.println(tileStart.getID());
					String nextRegionID = this.regionList.get(i+1).getID();
					RegionNode nextRegionNode = currentRegion.getNode(nextRegionID);
					Tile tileFinish = nextRegionNode.getCenter();
					//System.out.println(tileFinish.getID());
					RegionPath regionPath = new RegionPath(currentRegion, tileStart, tileFinish);
					nextTileStart = tileFinish;
					this.goToRegion.put(currentRegion.getID(), nextRegionID);
					this.regionPathHash.put(currentRegion.getID(), regionPath);
				} else if (i != this.regionList.size() - 1) { //the middle of path
					Region currentRegion = this.regionList.get(i);
					//System.out.println("test02");
					//System.out.println(currentRegion.getID());
					Tile tileStart = nextTileStart;
					//System.out.println(tileStart.getID());
					String nextRegionID = this.regionList.get(i+1).getID();
					RegionNode nextRegionNode = currentRegion.getNode(nextRegionID);
					Tile tileFinish = nextRegionNode.getCenter();
					//System.out.println(tileFinish.getID());
					RegionPath regionPath = new RegionPath(currentRegion, tileStart, tileFinish);
					nextTileStart = tileFinish;
					this.goToRegion.put(currentRegion.getID(), nextRegionID);
					this.regionPathHash.put(currentRegion.getID(), regionPath);					
				} else if (i == this.regionList.size() -1){ //the end of the path;
					Region currentRegion = this.regionList.get(i);
					//System.out.println("test03");
					//System.out.println(currentRegion.getID());
					Tile tileStart = nextTileStart;
					//System.out.println(tileStart.getID());
					Tile tileFinish = this.tileFinish;
					//System.out.println(tileFinish.getID());
					RegionPath regionPath = new RegionPath(currentRegion, tileStart, tileFinish);
					nextTileStart = null;
					this.goToRegion.put(currentRegion.getID(), null);
					this.regionPathHash.put(currentRegion.getID(), regionPath);
				}			
			}
			return false;
		} else {
			if (this.regionList.size() == 1){// you only have 1 region
				Region currentRegion = this.regionList.get(0);
				Tile tileStart = this.tileStart;
				Tile tileFinish = this.tileFinish;
				RegionPath regionPath = new RegionPath(currentRegion, tileStart, tileFinish);
				this.regionPathHash.put(currentRegion.getID(), regionPath);
			}
			return true;
		}
	}
	
	public RegionPath getNextRegionPath(String regionID){
		if (regionID != null){
			String nextRegionID = this.goToRegion.get(regionID);
			if (nextRegionID != null){
				return this.regionPathHash.get(nextRegionID);
			} else {
				return null;
			}
		} else { // regionID is null, indicates it is the start of path.
			if (!this.regionList.isEmpty()){
				String nextRegionID = this.regionList.get(0).getID();
				return this.regionPathHash.get(nextRegionID);
			} else { // regionList is empty
				return null;
			}		
		}
	}
	
	public class RegionPath{
		String id;
		Region region;
		Network tileNetwork;
		Tile tileStart;
		Tile tileFinish;
		List<Tile> tilePath;
		HashMap<Int3D, Tile> goToMap;
		
		public RegionPath(Region region, Tile tileStart, Tile tileFinish){
			this.region = region;
			this.tileNetwork = this.region.getTilesNetwork();
			this.id = region.getID();
			this.tileStart = tileStart;
			this.tileFinish = tileFinish;
			this.tilePath = new ArrayList<Tile>();
			this.goToMap = new HashMap<>();
			createTilePath();
		}
		
		public String getID(){
			return this.id;
		}
		
		public Region getRegion(){
			return this.region;
		}
		
		public Tile getNextTile(Int3D pTile){
			if (pTile == null){
				return this.tileStart;
			} else {
				Tile tile = this.goToMap.get(pTile);
					return tile;
			}
		}
		
		public Tile getTile(int index){
			if (index < this.tilePath.size()){
				return tilePath.get(index);
			} else {
				return null;
			}
		}
		
		void createTilePath(){
			boolean isStartExist = this.tileNetwork.nodeExists(this.tileStart);
			boolean isFinishExist = this.tileNetwork.nodeExists(this.tileFinish);
			if (isStartExist == true && isFinishExist == true){
				PriorityQueue<TileWrapper> frontier = new PriorityQueue<TileWrapper>(10, new Comparator<TileWrapper>(){
					public int compare(TileWrapper tileA, TileWrapper tileB){
						if (tileA.getPriority() < tileB.getPriority()){
							return -1;
						}
						if (tileA.getPriority() > tileB.getPriority()){
							return 1;
						}
						return 0;
					}
				});
				TileWrapper tileStartWrap = new TileWrapper(this.tileStart, 0);
				frontier.add(tileStartWrap);
				HashMap<Int3D, TileWrapper> cameFrom = new HashMap<>();
				HashMap<Int3D, Double> costSoFar = new HashMap<>();
				cameFrom.put(this.tileStart.getLocation(), tileStartWrap);
				costSoFar.put(this.tileStart.getLocation(), 0.0);
				while(frontier.size() != 0){
					TileWrapper currentTileWrap = frontier.poll();
					if (currentTileWrap.getLocation().equals(tileFinish.getLocation())){
						break;
					}
					Bag edges = null;
					edges = this.tileNetwork.getEdges(currentTileWrap.getTile(), edges);
					Bag tiles = new Bag();
					for (Object obj : edges){
						Edge edge = (Edge) obj;
						Tile neighbor = (Tile) edge.getOtherNode(currentTileWrap.getTile());
						double newCost = costSoFar.get(currentTileWrap.getLocation()) + (double) edge.info;
						if (!costSoFar.containsKey(neighbor.getLocation()) || newCost < costSoFar.get(neighbor.getLocation())){
							costSoFar.put(neighbor.getLocation(), newCost);
							double priority = newCost + neighbor.getLocation().distance(this.tileFinish.getLocation());
							TileWrapper neighborWrap = new TileWrapper(neighbor, priority);
							frontier.add(neighborWrap);
							cameFrom.put(neighbor.getLocation(), currentTileWrap);
						}
					}
				}
				Int3D startLocation = this.tileStart.getLocation();
				Tile nextTile = null;
				if (cameFrom.containsKey(this.tileFinish.getLocation()) == true){
					TileWrapper currentTileWrap = cameFrom.get(this.tileFinish.getLocation());
					this.tilePath.add(this.tileFinish);
					nextTile = this.tileFinish;
					if (currentTileWrap != null){
						while (!currentTileWrap.getLocation().equals(this.tileStart.getLocation())){
							Tile currentTile = currentTileWrap.getTile();
							this.goToMap.put(currentTile.getLocation(), nextTile);
							this.tilePath.add(currentTile);
							nextTile = currentTile;
							currentTileWrap = cameFrom.get(currentTile.getLocation());
						}
					}
				}
				this.goToMap.put(this.tileStart.getLocation(), nextTile);
				this.tilePath.add(this.tileStart);
				Collections.reverse(this.tilePath);
				//System.out.println(this.tilePath.size());
			}
		}
	}
}
