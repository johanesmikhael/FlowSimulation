package simulation;
//import java.util.UUID;
//import sim.engine.*;
import sim.util.*;
//import sim.field.grid.*;
//import sim.field.network.*;

public class Tile {
	private int index;
	private String regionID;
	private Int3D location3D;
	private Region region;
	//private int priority;
	
	public Tile(int index, Region region, Int3D tileCoordinate3D){
		this.index = index;
		this.location3D = tileCoordinate3D;
		this.region = region;
		this.regionID = region.getID();
		//this.priority = 0;
	}
	
	/*public int getPriority(){
		return this.priority;
	}
	
	public void setPriority(int priority){
		this.priority = priority;
	}*/
	
	public int getX(){
		return this.location3D.x;
	}
	public int getY(){
		return this.location3D.y;
	}
	public int getZ(){
		return this.location3D.z;
	}
	
	public Int3D getLocation(){
		return this.location3D;
	}
	
	public String getID(){
		return regionID;
	}
	
	public Region getRegion(){
		return this.region;
	}
	
	public int getLevel(){
		return this.region.getLevel();
	}
	
	public boolean isID(String id){
		if(this.regionID.equals(id)){
			return true;
		} else {
			return false;
		}
	}

	public int getIndex(){
		return this.index;
	}
	
	//overides equals
	public boolean equalsRegion(Object obj){
		Tile tile = (Tile)obj;
		if (tile.regionID.equals(this.regionID)){
			return true;
		} else {
			return false;
		}
	}
}
