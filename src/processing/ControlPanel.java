package processing;
import processing.FloorTile.InteractiveTile;
import processing.core.*;
import sim.util.Int3D;
import simulation.Floor;
import simulation.Itenerary;
import simulation.Region;
import simulation.Simulation;
import simulation.Tile;
import controlP5.*;

public class ControlPanel {
	public P5 p5Control;
	EnvironmentModel model;
	PApplet applet;
	Tile activeTile;
	boolean isSetPath;

	ControlPanel(PApplet applet, EnvironmentModel model){
		p5Control = new P5(applet, model);
		this.model = model;
		this.applet = applet;
		activeTile = null;
		isSetPath = false;
	}
	
	
	void draw(){
		p5Control.draw();
	}
	
	public void setTileInfo(InteractiveTile interactiveTile){
		Tile tile = interactiveTile.getTile();
		activeTile = tile;
		Region region = tile.getRegion();
		Int3D key = interactiveTile.getKey();
		ControlP5 cp5 = p5Control.getControlP5();
		String id = region.getID();
		String name = region.getName();
		String level = String.valueOf(region.getLevel());
		String accessibility = String.valueOf(region.getAccessibility());
		String type = String.valueOf(region.getType());
		String location = key.x + ", " + key.y + ", " + key.z;
		cp5.getController("namevalue").setValueLabel(name);
		cp5.getController("idvalue").setValueLabel(id);
		cp5.getController("levelvalue").setValueLabel(level);
		cp5.getController("locationvalue").setValueLabel(location);
		cp5.getController("accessibilityvalue").setValueLabel(accessibility);
		cp5.getController("typevalue").setValueLabel(type);
		this.p5Control.setIteneraryTile(tile);
	}
	
	public void resetTileInfoPanel(){
		activeTile = null;
		ControlP5 cp5 = p5Control.getControlP5();
		cp5.getController("namevalue").setValueLabel("<area name>");
		cp5.getController("idvalue").setValueLabel("idvalue");
		cp5.getController("levelvalue").setValueLabel("<level>");
		cp5.getController("locationvalue").setValueLabel("<location>");
		cp5.getController("accessibilityvalue").setValueLabel("<accessibility>");
		cp5.getController("typevalue").setValueLabel("<type>");
	}
	
	public Tile getActiveTile(){
		return this.activeTile;
	}
	
	public void setActiveTile(Tile tile){
		this.activeTile = tile;
	}
	
	public void controlEvent(ControlEvent theEvent){
		p5Control.controlEvent(theEvent);
	}
	
	public boolean isSetPath(){
		return this.isSetPath;
	}
	
	public void setPath(boolean isSetPath){
		this.isSetPath = isSetPath;
	}
	
}
