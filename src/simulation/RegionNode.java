package simulation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import sim.util.*;

public class RegionNode {
	
	private HashMap<Int3D, Tile> tileHash;
	private List<Int3D> tileList;
	private Int3D nodeCenter;
	private int centerIndex;
	private String nodeID;
	private String nodeParentID;
	private int weight;
	
	public RegionNode(String nodeID, String nodeParentID){
		tileHash = new HashMap<>();
		tileList = new ArrayList<Int3D>();
		this.nodeID = nodeID;
		this.nodeParentID = nodeParentID;
	}
	
	public String getID(){
		return this.nodeID;
	}
	
	public String getParentID(){
		return this.nodeParentID;
	}
	
	public int getWeight(){
		return this.weight;
	}
	
	public void addTile(Int3D key, Tile tile){
		tileHash.put(key,  tile);
		tileList.add(key);
	}
	
	public Collection<Tile> getTiles(){
		return this.tileHash.values();
	}
	
	public boolean isEmpty(Int3D loc){
		boolean isContains = this.tileHash.containsKey(loc);
		return !isContains;
	}
	
	public void setCenter(){
		int n = this.tileList.size();
		float center = Math.round(n/2);
		int centerInt = (int)center;
		this.centerIndex = centerInt;
		this.nodeCenter= this.tileList.get(centerInt);
	}
	
	public Tile getCenter(){
		return this.tileHash.get(this.nodeCenter);
	}
	
	public int getCenterIndex(){
		return this.centerIndex;
	}
	
	public Tile getTile(int index){
		if (index < 0 || index >= this.tileList.size()){
			return null;
		} else {
			Int3D tileLoc = this.tileList.get(index);
			Tile tile = this.tileHash.get(tileLoc);
			return tile;
		}
	}
	
	public void setWeightFromCenter(Int3D center){
		double distance = center.distance(this.nodeCenter);
		int distInt = (int)Math.round(distance);
		this.weight = distInt;
	}
}
