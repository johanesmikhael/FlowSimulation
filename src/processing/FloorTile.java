package processing;
//import processing.ProcessingDisplay.TileSurface;
import processing.core.*;
import java.util.HashMap;





//import javafx.geometry.BoundingBox;
import remixlab.bias.core.BogusEvent;
import remixlab.dandelion.core.AbstractScene;
import remixlab.dandelion.core.Constants.ClickAction;
import remixlab.dandelion.core.Constants.DandelionAction;
import remixlab.dandelion.core.Constants.Target;
import remixlab.dandelion.core.InteractiveFrame;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.*;
import sim.util.Int3D;
import simulation.Elevator;
import simulation.Floor;
import simulation.Region;
import simulation.Simulation;
import simulation.Tile;



public class FloorTile {
	private HashMap<Int3D, InteractiveTile> tiles;
	PApplet applet;
	Scene scene;
	EnvironmentModel model;
	InteractiveFloor floor;
	final int divisor = 7;
	
	FloorTile(PApplet p, Scene scene, EnvironmentModel model, InteractiveFloor floor){
		this.applet = p;
		this.scene = scene;
		tiles = new HashMap<>();
		this.model = model;
		this.floor = floor;
	}
	
	public void setTiles(Floor floor){
		System.out.println(floor.getLevel());
		for(Object regionObject : floor.getRegions()){
			Region region = (Region) regionObject;
			String id = region.getID();
			simulation.BoundingBox bound = region.getBoundingBox();
			int divMinX = bound.getMinX()/divisor;
			int divMinY = bound.getMinY()/divisor;
			int divMaxX = bound.getMaxX()/divisor + 1;
			int divMaxY = bound.getMaxY()/divisor + 1;
			for (int i = divMinX; i <= divMaxX; i++){
				for(int j = divMinY; j <= divMaxY; j++){
					boolean isValid = true;
					//System.out.println("NEW BIGTILE :" + isValid);
					validity:
					for (int x = 0; x <= divisor -1 ; x++){
						for (int y = 0; y <= divisor - 1; y++){
							int cX = (i * divisor) + x;
							int cY = (j * divisor) + y;
							boolean isTile = region.isTile(cX, cY);
							//System.out.println(isTile);
							if(isTile == false){
								isValid = false;
								break validity;
							}
						}
					}
					if(isValid == true){
						//System.out.println("dfafasdf");
						float midX = i * divisor + (float)(divisor - 1) / 2;
						float midY = j * divisor + (float)(divisor - 1) / 2;
						//int z = region.getRegionGridElevation();
						int z = region.getElevation();
						Int3D key = new Int3D(i * divisor + divisor/2, j * divisor + divisor/2, z);
						Tile tile = region.getTile(key);
						float zFloat = (float) z + 1;
						tiles.put(key, new InteractiveTile(tile, midX,midY, zFloat + 1, id, key, scene));
					}
				}
			}
		}
    	/*int p = 0;
		for (int elevation : Simulation.floorElevationList){
			System.out.println(p);
			Floor floor = Simulation.floorHash.get(elevation);
			if (true){
				for(Object regionObject : floor.getRegions()){
					Region region = (Region) regionObject;
					String id = region.getID();
					simulation.BoundingBox bound = region.getBoundingBox();
					int divMinX = bound.getMinX()/divisor;
					int divMinY = bound.getMinY()/divisor;
					int divMaxX = bound.getMaxX()/divisor + 1;
					int divMaxY = bound.getMaxY()/divisor + 1;
					for (int i = divMinX; i <= divMaxX; i++){
						for(int j = divMinY; j <= divMaxY; j++){
							boolean isValid = true;
							//System.out.println("NEW BIGTILE :" + isValid);
							validity:
							for (int x = 0; x <= divisor -1 ; x++){
								for (int y = 0; y <= divisor - 1; y++){
									int cX = (i * divisor) + x;
									int cY = (j * divisor) + y;
									boolean isTile = region.isTile(cX, cY);
									//System.out.println(isTile);
									if(isTile == false){
										isValid = false;
										break validity;
									}
								}
							}
							if(isValid == true){
								//System.out.println("dfafasdf");
								float midX = i * divisor + (float)(divisor - 1) / 2;
								float midY = j * divisor + (float)(divisor - 1) / 2;
								int z = region.getRegionGridElevation();
								Int3D key = new Int3D(i * divisor + 4, j * divisor + 4, z);
								float zFloat = (float)z;
								tiles.put(key, new InteractiveTile(midX,midY, zFloat + 1, id, scene));
							}
							
						}
					}
				}
			}	
			p++;
		}*/
    }
	
	public void draw(){
		if (model.isFloorActive() == false){
			return;
		} else {
			if(this.floor.isActive() == true){
				for (InteractiveTile tile : this.tiles.values()){
					tile.draw();
				}
			}
		}
		
	}
	
	
	
	class InteractiveTile extends InteractiveFrame{
		private float x;
		private float y;
		private float z;
		private final float SIZE = divisor;
		String id;
		Int3D key;
		int level;
		boolean isTileGrab;
		Tile tile;
				
		InteractiveTile(Tile tile, Int3D location, String id, Scene scene){
			super(scene);
			this.level = level;
			setLocation(location);
			this.id = id;
			this.tile = tile;
		}
		
		InteractiveTile(Tile tile, float x, float y, float z, String id, Int3D key, Scene scene){
			super(scene);
			setLocation(x, y ,z);
			this.id = id;
			this.key = key;
			this.tile = tile;
		}
		
		private void setLocation(float x, float y, float z){
			this.x = x - 400;
			this.y = -y + 400;
			this.z = z;
		}
		
		private void setLocation(Int3D location){
			this.x = (float)(location.getX() - 400);
			this.y = (float)(-location.getY() + 400); // inverted y coordinate system from standard to java
			this.z = (float)(location.getZ()) + 1;
		}
		
		public Int3D getKey(){
			return this.key;
		}
		
		public Tile getTile(){
			return this.tile;
		}
		
		public String getID(){
			return this.id;
		}
		
		void draw(){
			applet.pushMatrix();
			applet.pushStyle();
			// code here
			this.setPosition(0,0,0);
			this.setRotation(0,0,0,0);
			this.setScaling(1);
			this.applyTransformation();
			applet.noFill();
			//applet.fill(255);
			applet.noStroke();
			if (grabsInput(((Scene)scene).motionAgent())) {
			      applet.fill(250, 250, 0);
			}
			
			applet.beginShape();
			applet.vertex(x - SIZE/2,y - SIZE/2,z);
			applet.vertex(x - SIZE/2, y + SIZE/2, z);
			applet.vertex(x + SIZE/2, y + SIZE/2, z);
			applet.vertex(x + SIZE/2, y - SIZE/2, z);
			applet.endShape(PConstants.CLOSE);
			applet.popStyle();
			applet.popMatrix();
			
		}
		
		public boolean checkIfGrabsInput(BogusEvent event) {
			if(floor.isActive() == false){
				isTileGrab = false;
				return false;
			} else {
				isTileGrab = pointInsideQuad(scene);
				return isTileGrab;
			}
			//return pointInsideQuad(scene);
		}
		 
		private boolean pointInsideQuad(AbstractScene scene) {
			Vec v1 = scene.projectedCoordinatesOf(new Vec(x - SIZE/2,y - SIZE/2,z));
			Vec v2 = scene.projectedCoordinatesOf(new Vec(x - SIZE/2, y + SIZE/2, z));
			Vec v3 = scene.projectedCoordinatesOf(new Vec(x + SIZE/2, y + SIZE/2, z));
			Vec v4 = scene.projectedCoordinatesOf(new Vec(x + SIZE/2, y - SIZE/2, z));

			return
			computePointPosition(applet.mouseX, applet.mouseY, v1.x(), v1.y(), v2.x(), v2.y()) < 0 &&
			computePointPosition(applet.mouseX, applet.mouseY, v2.x(), v2.y(), v3.x(), v3.y()) < 0 &&
			computePointPosition(applet.mouseX, applet.mouseY, v3.x(), v3.y(), v4.x(), v4.y()) < 0 &&
			computePointPosition(applet.mouseX, applet.mouseY, v4.x(), v4.y(), v1.x(), v1.y()) < 0;
		}

		private float computePointPosition(float x, float y, float x0, float y0, float x1, float y1) {
		// see "solution 3" at http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
			return (y - y0) * (x1 - x0) - (x - x0) * (y1 - y0);
		}
		
		public void execAction3D(DandelionAction a) {
		    switch(a) {
		    	case CUSTOM:
		    		//board.movePatch(this);
		    		//model.identifyLocation(key, id);
		    		ProcessingDisplay display = (ProcessingDisplay) applet;
		    		ControlPanel controlPanel = display.getControlPanel();
		    		controlPanel.setTileInfo(this);
		    		break;
		    	default:
		    		super.execAction3D(a);
		    		break;
		    }
		}
	}
}
