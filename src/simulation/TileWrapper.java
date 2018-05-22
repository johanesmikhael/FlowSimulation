package simulation;

import sim.util.Int3D;

public class TileWrapper {
	
	Tile tile;
	double priority;
	
	public TileWrapper(Tile tile, double priority){
		this.tile = tile;
		this.priority = priority;
	}
	
	public double getPriority(){
		return this.priority;
	}
	
	public Tile getTile(){
		return this.tile;
	}
	
	public Int3D getLocation(){
		return this.tile.getLocation();
	}
	
	public String getID(){
		return this.tile.getID();
	}	
}
