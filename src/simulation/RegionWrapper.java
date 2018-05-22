package simulation;

public class RegionWrapper{
	private int priority;
	private Region region;
	
	public RegionWrapper(Region region, int priority){
		this.region = region;
		this.priority = priority;
	}
	
	public int getPriority(){
		return this.priority;
	}
	
	public Region getRegion(){
		return this.region;
	}
	
	public String getID(){
		return this.region.getID();
	}
}
