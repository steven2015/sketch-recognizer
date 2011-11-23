package steven.project.csit5110;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Algorithm{
	private final List<Point2D> points = new ArrayList<Point2D>();
	private final List<Sample> matches = new ArrayList<Sample>();
	@Deprecated
	private Sample sketch;

	public Algorithm(){
		clearSketch();
	}
	public synchronized void sketch(final int x, final int y, final boolean stopped){
		points.add(new Point2D(x,y));
		if(stopped){
			points.add(null);
		}
		sketch = Sample.createSample("Sketch",points,0);
	}
	public synchronized void clearSketch(){
		points.clear();
		sketch = null;
	}
	public synchronized void draw(final Graphics g, final int width, final int height){
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width,height);
		g.setColor(Color.RED);
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
		if(sketch != null){
			for(int i = 0; i < Sample.squareLength; i++){
				for(int j = 0; j < Sample.squareLength; j++){
					final double tmp = sketch.square[i][j];
					if(tmp > 0){
						g.setColor(new Color(0,0,(int)(tmp * 255)));
						g.drawLine(20 + i,40 + j,20 + i,40 + j);
					}
				}
			}
		}
		g.setColor(Color.BLACK);
		g.drawRect(20,40,Sample.squareLength,Sample.squareLength);
	}
	public synchronized void match(){
		points.clear();
		matches.add(Sample.match(points));
	}
	public List<Sample> getMatches(){
		return matches;
	}
}
