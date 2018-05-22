package simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

public class Route {
	Tile tileStart;
	Tile tileFinish;
	//List<Tile> tileList;
	List<Region> regionList;
	String pathID;
	UUID uuid;
	Network network;
	int regionCost;
	
	public Route(){
		this.regionList = new ArrayList<Region>();
		tileStart = null;
		tileFinish = null;
		this.regionCost = 0;
	}
	
	public Route(Network network){
		this.network = network;
		uuid = UUID.randomUUID();
		pathID = uuid.toString();
		tileStart = null;
		tileFinish = null;
		this.regionList = new ArrayList<Region>();
		//this.tileList = new ArrayList<Tile>();
		this.regionCost = 0;
	}
	
	public static Route calculateRoute(Tile tileStart, Tile tileFinish, Network network){
		Route path = new Route(network);
		boolean isRoute = path.setRoute(tileStart, tileFinish);
		if (isRoute == true){
			return path;
		} else {
			return null;
		}
	}
	
	
	boolean setRoute(Tile tileStart, Tile tileFinish){
		this.tileStart = tileStart;
		this.tileFinish = tileFinish;
		boolean isExist = false;
		Region regionStart = this.tileStart.getRegion();
		Region regionFinish = this.tileFinish.getRegion();
		boolean isStartExist = network.nodeExists(regionStart);
		boolean isFinishExist = network.nodeExists(regionFinish);
		if (regionStart.equals(regionFinish)){
			this.regionList.add(regionStart);
			isExist = true;
		}
		if (isStartExist == true && isFinishExist == true && !regionStart.equals(regionFinish)){
			PriorityQueue<RegionWrapper> frontier = new PriorityQueue<RegionWrapper>(10, new Comparator<RegionWrapper>(){
				public int compare(RegionWrapper regionA, RegionWrapper regionB){
					if (regionA.getPriority() < regionB.getPriority()){
						return -1;
					}
					if (regionA.getPriority() > regionB.getPriority()){
						return 1;
					}
					return 0;
				}
			});
			//regionStart.setPriority(0);
			RegionWrapper regionStartWrap = new RegionWrapper(regionStart, 0);
			frontier.add(regionStartWrap);
			HashMap<String,RegionWrapper> cameFrom = new HashMap<>();
			HashMap<String,Integer> costSoFar = new HashMap<>();
			cameFrom.put(regionStart.getID(), null);
			costSoFar.put(regionStart.getID(), 0);
			while(frontier.size() != 0){
				RegionWrapper currentRegWrap = frontier.poll();	
				if (currentRegWrap.getRegion().equals(regionFinish)){
					this.regionCost = costSoFar.get(regionFinish.getID());
					break;
				}
				Bag edges = null;
				edges = network.getEdges(currentRegWrap.getRegion(), edges);
				Bag regions = new Bag();
				for (Object edgeObj : edges){
					Edge edge = (Edge) edgeObj;
					Region neighbor = (Region) edge.getOtherNode(currentRegWrap.getRegion());
					int newCost = costSoFar.get(currentRegWrap.getID()) + (int) edge.info;
					if (!costSoFar.containsKey(neighbor.getID()) || newCost < costSoFar.get(neighbor.getID())){
						costSoFar.put(neighbor.getID(), newCost);
						int priority = newCost + heuristicDistance(regionFinish, neighbor);
						RegionWrapper neighborWrap = new RegionWrapper(neighbor, priority);
						//neighbor.setPriority(priority);
						frontier.add(neighborWrap);
						cameFrom.put(neighbor.getID(), currentRegWrap);
					}
				}
			}
			
			String startID = regionStart.getID();
			if (cameFrom.containsKey(regionFinish.getID()) == true){
				RegionWrapper currentRegWrap = cameFrom.get(regionFinish.getID());
				//Region currentReg = cameFrom.get(regionFinish.getID());
				this.regionList.add(regionFinish);
				if (currentRegWrap != null){
					while(!currentRegWrap.getID().equals(startID)){
						Region currentReg = currentRegWrap.getRegion();
						//System.out.println(currentReg.getID());
						this.regionList.add(currentReg);
						currentRegWrap = cameFrom.get(currentReg.getID());
					}
				}
				this.regionList.add(regionStart);
				Collections.reverse(this.regionList);
				for (Region region : this.regionList){
					//System.out.println(region.getID());
				}
				isExist = true;
			} else {
				isExist = false;
			}
		}
		return isExist;
	}
	
	
	public int heuristicDistance(Region regionA, Region regionB){
		double distance =  regionA.getCenter().distance(regionB.getCenter());
		int distInt = (int)Math.round(distance);
		return distInt;
	}
	
	public int getRegionCost(){
		return this.regionCost;
	}
	
	public Tile getStart(){
		return this.tileStart;
	}
	
	public Tile getFinish(){
		return this.tileFinish;
	}
}
