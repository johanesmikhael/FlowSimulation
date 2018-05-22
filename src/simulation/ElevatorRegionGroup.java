package simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import sim.field.network.Network;

public class ElevatorRegionGroup {
	private Floor floor;
	private Region lobbyRegion;
	private HashMap<String, Elevator> elvMap;
	private HashMap<Integer, Floor> floorToService;
	private int accessibility;
	private int type;
	
	public ElevatorRegionGroup(Floor floor, Region lobbyRegion, int type, int accessibility){
		this.floor = floor;
		this.lobbyRegion = lobbyRegion;
		this.elvMap = new HashMap<>();
		this.floorToService = new HashMap<>();
		this.accessibility = accessibility;
		this.type = type;
	}
	
	public boolean isExist(String id, int type, int accessibility){
		if(lobbyRegion.getID().equals(id) && type == this.type && accessibility == this.accessibility){
			return true;
		} else {
			return false;
		}
	}
	
	public void addElevator(Elevator elevator){
		this.elvMap.put(elevator.getID(), elevator);
		List<Floor> floors = elevator.getFloors();
		for (Floor floor : floors){
			int level = floor.getLevel();
			if (!this.floorToService.containsKey(level)){
				this.floorToService.put(new Integer(level), floor);
			}
		}
	}
	
	public int getLevel(){
		return this.floor.getLevel();
	}
	
	public int getType(){
		return this.type;
	}
	
	public int getAccessibility(){
		return this.accessibility;
	}
	
	public String getRegionID(){
		return lobbyRegion.getID();
	}
	
	public Region getLobbyRegion(){
		return this.lobbyRegion;
	}
	
	public Collection<Elevator> getElevators(){
		return this.elvMap.values();
	}
	
	public boolean isLevelExist(int level){
		return this.floorToService.containsKey(level);
	}
	
	public void processRegionToService(){
		Network network = Simulation.elevatorNetworks.get(this.accessibility);
		
	}
	
	public Elevator getFeasibleElevator(int startLevel, int destLevel){
		int maxFeasibility = -99999999;
		Elevator selectedElv = null;
		for (Elevator elevator : this.getElevators()){
			int feasibility = elevator.getFeasibility(startLevel, destLevel);
			if (feasibility > maxFeasibility){
				maxFeasibility = feasibility;
				selectedElv = elevator;
			}
		}
		return selectedElv;
	}
}
