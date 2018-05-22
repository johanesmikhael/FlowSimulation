package simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

public class Itenerary {
	Tile tileStart;
	Tile tileFinish;
	//List<Tile> tileList;
	//List<Region> regionList;
	List<Tile> itenerary;
	
	//List<Region> regionListFloorA;
	//List<Region> regionListElevator;
	//List<Region> regionlistFloorB;
	boolean isSingleFloor;
	UUID uuid;
	String iteneraryID;
	
	public Itenerary(){
		uuid = UUID.randomUUID();
		iteneraryID = uuid.toString();
		tileStart = null;
		tileFinish = null;
		//tileList = new ArrayList<Tile>();
		itenerary = new ArrayList<Tile>();
		isSingleFloor = false;
	}
	
	public void setStart(Tile tile){
		this.tileStart = tile;
	}
	
	public Tile getStart(){
		return this.tileStart;
	}
	
	public void setFinish(Tile tile){
		this.tileFinish = tile;
	}
	
	public Tile getFinish(){
		return this.tileFinish;
	}
	
	public boolean isStartSet(){
		if (tileStart == null){
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isFinishSet(){
		if (tileFinish == null){
			return false;
		} else {
			return true;
		}
	}
	
	public void resetItenerary(){
		tileStart = null;
		tileFinish = null;
	}
	
	public boolean calculateItenerary(){
		boolean isPathExist = false;
		this.reset();
		if (tileStart.getRegion().equals(tileFinish.getRegion())){
			this.itenerary.add(tileStart);
			this.itenerary.add(tileFinish);
			isPathExist = true;
			return isPathExist;
		}
		if (tileStart.getZ() == tileFinish.getZ()){// if its on the same floor
			this.isSingleFloor = true;
			Floor floor = tileStart.getRegion().getFloor();
			Network network = floor.getNetworkAll();
			Route path = Route.calculateRoute(tileStart, tileFinish, network);
			if(path != null){
				this.itenerary.add(this.tileStart);
				this.itenerary.add(this.tileFinish);
				isPathExist = true;
			}
		} else {
			this.isSingleFloor = false;
			List<Route> paths = findTroughElevatorPath();
			if (paths != null){
				this.itenerary.add(this.tileStart);
				this.itenerary.add(paths.get(0).getFinish());
				this.itenerary.add(paths.get(1).getStart());
				this.itenerary.add(this.tileFinish);
				isPathExist = true;
			}
		}
		//System.out.println("returnIsPathExist :" + isPathExist);
		return isPathExist;
	}
	
	void reset(){
		itenerary.clear();	
	}
	
	public Tile getItenerary(int index){
		if (index >= this.itenerary.size()){
			return null;
		}
		Tile tile = this.itenerary.get(index);
		if (tile == null){
			return null;
		} else {
			return tile;
		}
	}
	
	List<Route> findTroughElevatorPath(){
		Region regionStart = this.tileStart.getRegion();
		Region regionFinish = this.tileFinish.getRegion();
		if (regionStart == null || regionFinish == null){
			return null;
		}
		Floor floorA = regionStart.getFloor();
		Network networkA = floorA.getNetworkAll();
		int minCost= 999999999;
		//ElevatorRegionGroup selectedElvGroup = null;
		Route selectedPathA = null;
		Route selectedPathB = null;
		for (List<ElevatorRegionGroup> elvGroupList : floorA.getElevatorRegionGroupHashValues()){
			for (ElevatorRegionGroup elvGroup : elvGroupList){
				Region regionElvGroup = elvGroup.getLobbyRegion();
				Route pathA = Route.calculateRoute(this.tileStart, regionElvGroup.getCenterTile(), networkA);
				if(pathA !=null){
					int cost = pathA.getRegionCost();
					if (cost < minCost){
						Route pathB = createRouteToFinish(elvGroup, tileFinish);
						if (pathB != null){
							minCost = cost;
							selectedPathA = pathA;
							selectedPathB = pathB;
							//selectedElvGroup = elvGroup;
						}
					}
					
				}
			}
		}
		if (selectedPathA == null && selectedPathB == null){
			return null;
		}
		List<Route> paths = new ArrayList<Route>();
		paths.add(selectedPathA);
		paths.add(selectedPathB);
		return paths;
	}
	
	Route createRouteToFinish(ElevatorRegionGroup elvGroupA, Tile tileFinish){
		Region regionFinish = tileFinish.getRegion();
		Floor floor = regionFinish.getFloor();
		boolean isLevelExist = elvGroupA.isLevelExist(floor.getLevel());
		if (isLevelExist == false){
			return null;
		}		
		Network network = floor.getNetworkAll();
		ElevatorRegionGroup elvGroupB = null;
		check:
		for (Elevator elevator : elvGroupA.getElevators()){
			ElevatorRegionGroup elvGroup = elevator.getElevatorRegionGroup(regionFinish.getLevel());
			if (elvGroup != null){
				elvGroupB = elvGroup;
				break check;
			}
		}
		Region regionB = elvGroupB.getLobbyRegion();
		Route path = Route.calculateRoute(regionB.getCenterTile(), tileFinish, network);
		return path;
	}
}
