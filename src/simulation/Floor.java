package simulation;
//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;


import java.util.List;




import ec.util.MersenneTwisterFast;
//import sim.engine.*;
//import sim.util.*;
//import sim.field.grid.*;
import sim.field.network.*;
import sim.util.Bag;

public class Floor {
	private int elevation;
	//private int gridElevation;
	private int level;
	private HashMap<String, Region> regionHash = new HashMap<>();
	//HashMap<String, Region> doorHash = new HashMap<>();
	private Network regionsNetworkAll;
	private Network regionsNetworkGeneral;
	private Network regionsNetworkPatient;
	private HashMap<String, List<ElevatorRegionGroup>> elevatorRegionGroupHash;
	//private List<ElevatorRegionGroup> elevatorRegionGroups;
	
	Floor(int level, int elevation){
		this.elevation = elevation;
		this.level = level;
		//this.gridElevation = elevation/Simulation.GRIDDIMENSION;
	}
	
	public void processRegion(){
		//process overy region on the floor
		for (Object object : regionHash.values()){
			Region singleRegion = (Region) object;
			singleRegion.detectBorder();
			singleRegion.calculateCenter();
			singleRegion.detectNode();
		}
		processNetwork();
		processRegionNetwork();
		processElevatorRegionGroup();
		processElevatorNetwork();
	}
	
	public int getElevation(){
		return this.elevation;
	}
	
	/*public int getGridElevation(){
		return this.gridElevation;
	}*/
	
	public int getLevel(){
		return this.level;
	}
	
	public void addRegion(Region region ){
		String id = region.getID();
		this.regionHash.put(id, region);
	}
	
	public Region getRegion(String id){
		return (Region)this.regionHash.get(id);
	}
	
	public Collection<Region> getRegions(){
		return this.regionHash.values();
	}
	
	public int getRegionNum(){
		return this.regionHash.size();
	}
	
	
	void processNetwork(){
		this.regionsNetworkAll = new Network(false);
		for (Region region : this.regionHash.values()){
			regionsNetworkAll.addNode(region);
		}
		for (Region regionA : this.regionHash.values()){
			for (RegionNode regionNodeAtoB : regionA.getNodes()){
				String id = regionNodeAtoB.getID();
				Region regionB = this.regionHash.get(id);
				RegionNode regionNodeBtoA = regionB.getNode(regionA.getID());
				//check edge;all
				Edge edge = regionsNetworkAll.getEdge(regionA, regionB);
				if (edge == null){
					int weight = regionNodeAtoB.getWeight() + regionNodeBtoA.getWeight();
					this.regionsNetworkAll.addEdge(regionA, regionB, new Integer(weight));
				}
			}
		}
		this.regionsNetworkPatient = new Network(this.regionsNetworkAll);
		for (Region region : this.regionHash.values()){
			if (region.getAccessibility() == Region.STAFFONLY){
				this.regionsNetworkPatient.removeNode(region);
			}
		}
		this.regionsNetworkGeneral = new Network(this.regionsNetworkPatient);
		for (Region region : this.regionHash.values()){
			if (region.getAccessibility() == Region.STAFFANDPATIENT){
				this.regionsNetworkGeneral.removeNode(region);
			}
		}
	}
	
	void processRegionNetwork(){
		for (Region region : this.regionHash.values()){
			region.processGridNetwork();
		}
	}
	
	public Network getNetworkAll(){
		return this.regionsNetworkAll;
	}
	
	public Network getNetworkGeneral(){
		return this.regionsNetworkGeneral;
	}
	
	public Network getNetworkPatient(){
		return this.regionsNetworkPatient;
	}
	
	public void processElevatorRegionGroup(){
		//this.elevatorRegionGroups = new ArrayList<ElevatorRegionGroup>();
		this.elevatorRegionGroupHash = new HashMap<>();
		for (Elevator elevator : Simulation.elevatorHash.values()){
			Region elvRegion = elevator.getRegion(this.level);
			if (elvRegion != null){
				Bag edges = null;
				edges = this.regionsNetworkAll.getEdges(elvRegion, edges);
				Region adjRegion = null;
				System.out.println("elevator reg : " + elvRegion.getID());
				if (edges.size() != 0){
					adjRegion = (Region) ((Edge)edges.get(0)).getOtherNode(elvRegion);
				}
				System.out.println(adjRegion.getID());
				boolean isExist = isElevatorGroupExist(adjRegion.getID(), elevator.getType(), elevator.getAccessibility());
				if (isExist == false){
					adjRegion.setElevatorLobby(true);
					//System.out.println(adjRegion.getID());
					ElevatorRegionGroup elvGroup = new ElevatorRegionGroup(this, adjRegion, elevator.getType(), elevator.getAccessibility());
					elvGroup.addElevator(elevator);
					if (!this.elevatorRegionGroupHash.containsKey(adjRegion.getID())){
						List <ElevatorRegionGroup> elvgroupList = new ArrayList<ElevatorRegionGroup>();
						elvgroupList.add(elvGroup);
						this.elevatorRegionGroupHash.put(adjRegion.getID(), elvgroupList);
					} else {
						List <ElevatorRegionGroup> elvgroupList = this.elevatorRegionGroupHash.get(adjRegion.getID());
						elvgroupList.add(elvGroup);
					}
				} else {
					ElevatorRegionGroup elvGroup = getElevatorRegionGroup(adjRegion.getID(), elevator.getType(), elevator.getAccessibility());
					if (elvGroup != null){
						elvGroup.addElevator(elevator);
					}
				}
			}
		}
	}
	
	boolean isElevatorGroupExist(String id, int type, int accessibility){
		boolean isExist = false;
		boolean isKeyExist = this.elevatorRegionGroupHash.containsKey(id);
		if (isKeyExist){
			check:
			for (ElevatorRegionGroup elvGroup : this.elevatorRegionGroupHash.get(id)){
				if (elvGroup.isExist(id, type, accessibility) == true){
					isExist = true;
					break check;
				}
			}
		}
		return isExist;
	}
	
	ElevatorRegionGroup getElevatorRegionGroup(String id, int type, int accessibility){
		ElevatorRegionGroup elvGroup = null;
		List<ElevatorRegionGroup> elvGroupList = this.elevatorRegionGroupHash.get(id);
		for (ElevatorRegionGroup elvgrp : elvGroupList){
			if (elvgrp.isExist(id, type, accessibility) == true){
				elvGroup = elvgrp;
			}
		}
		return elvGroup;
	}
	
	ElevatorRegionGroup getAnyElevatorRegionGroup(String id){//get any elevator start form general to staff
		List<ElevatorRegionGroup> elvGroupList = this.elevatorRegionGroupHash.get(id);
		for (ElevatorRegionGroup elvgrp : elvGroupList){
			if (elvgrp.getAccessibility() == Elevator.GENERAL){
				return elvgrp;
			}
		}
		for (ElevatorRegionGroup elvgrp : elvGroupList){
			if (elvgrp.getAccessibility() == Elevator.STAFFANDPATIENT){
				return elvgrp;
			}
		}
		for (ElevatorRegionGroup elvgrp : elvGroupList){
			if (elvgrp.getAccessibility() == Elevator.STAFFONLY){
				return elvgrp;
			}
		}
		return null;
	}
	
	public Collection<List<ElevatorRegionGroup>> getElevatorRegionGroupHashValues(){
		return this.elevatorRegionGroupHash.values();
	}
	
	public void processElevatorNetwork(){
		for (List<ElevatorRegionGroup> elvGroupList : this.elevatorRegionGroupHash.values()){
			for (ElevatorRegionGroup elvRegionGroup : elvGroupList){
				int type = elvRegionGroup.getType();
				int accessibility = elvRegionGroup.getAccessibility();
				processElevatorNetwork(elvRegionGroup, accessibility);
			}
		}
	}
	
	void processElevatorNetwork(ElevatorRegionGroup elvRegionGroup, int accessibility){
		Network network = Simulation.elevatorNetworks.get(accessibility);
		network.addNode(elvRegionGroup);
		for (Elevator elevator : elvRegionGroup.getElevators()){
			elevator.addRegionGroup(elvRegionGroup);
			network.addEdge(elevator, elvRegionGroup, new Integer(1));
		}
		
	}
	
	Tile getRandomCenterTile(MersenneTwisterFast twister){
		//Collection<Region> regions = this.regionHash.values();
		List<Region> regions = new ArrayList<Region>();
		regions.addAll(this.regionHash.values());
		
		//System.out.println(regionIndex);
		Region region = null;
		do{
			double random = twister.nextDouble(true, true);
			//System.out.println(random);
			int regionIndexMax = regions.size() - 1;
			//System.out.println(regionIndexMax);
			double randomIndex = random * (double)regionIndexMax;
			int regionIndex = (int)Math.round(randomIndex);
			region = regions.get(regionIndex);
		} while (region.getType() != Region.FLOORREGION || region.isElevatorLobby() == true);
		
		Tile tile = region.getCenterTile();
		return tile;
	}
}
