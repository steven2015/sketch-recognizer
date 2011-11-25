package steven.project.csit5110;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Algorithm{
	private final List<Point2D> points = new ArrayList<Point2D>();
	private final List<Sample> matches = new ArrayList<Sample>();

	public Algorithm(){
		clearSketch();
	}
	public synchronized void sketch(final int x, final int y, final boolean stopped){
		points.add(new Point2D(x,y));
		if(stopped){
			points.add(null);
		}
	}
	public synchronized void clearSketch(){
		points.clear();
	}
	public synchronized void draw(final Graphics2D g, final int width, final int height){
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width,height);
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(5));
		Point2D lastPt = null;
		for(final Point2D pt : points){
			if(pt != null && lastPt != null){
				g.drawLine((int)lastPt.x,(int)lastPt.y,(int)pt.x,(int)pt.y);
			}
			lastPt = pt;
		}
		for(final Sample match : matches){
			g.setColor(Color.GREEN);
			lastPt = null;
			for(final Point2D pt : match.getInversePoints()){
				if(pt != null && lastPt != null){
					g.drawLine((int)lastPt.x,(int)lastPt.y,(int)pt.x,(int)pt.y);
				}
				lastPt = pt;
			}
		}
	}
	public synchronized void clearScreen(){
		points.clear();
		matches.clear();
	}
	public synchronized void match(){
		matches.add(Sample.match(points));
		points.clear();
	}
	public List<Sample> getMatches(){
		return matches;
	}
}
