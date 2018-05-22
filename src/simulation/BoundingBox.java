package simulation;

public class BoundingBox {
	private double width;
	private double height;
	private double orientation;
	private double [] position;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	
	public BoundingBox(){
		orientation = 0;
	}
	
	public BoundingBox(int minX, int maxX, int minY, int maxY){
		orientation = 0;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		width = this.maxX - this.minX;
		height = this.maxY - this. minY;
		calculatePosition();
	}
	
	void calculatePosition(){
		position = new double[2];
		double x = (minX + maxX) / 2;
		double y = (minY + maxY) / 2;
		position[0] = x;
		position[1] = y;
	}
	public void setMinX(int x){
		minX = x;
	}
	public void setMaxX(int x){
		maxX = x;
	}
	public void setMinY(int y){
		minY = y;
	}
	public void setMaxY(int y){
		maxY = y;
	}
	public int getMinX(){
		return minX;		
	}
	public int getMaxX(){
		return maxX;
	}
	public int getMinY(){
		return minY;
	}
	public int getMaxY(){
		return maxY;
	}
	
	public double getWidth(){
		return width;
	}
	
	public double getHeight(){
		return height;
	}
	
	public double getOrientation(){
		return orientation;
	}
}

