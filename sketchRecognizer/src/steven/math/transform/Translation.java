package steven.math.transform;

import java.awt.Graphics2D;

import steven.project.csit5110.Point2D;

public class Translation implements ITransform{
	private final double x;
	private final double y;

	public Translation(final double x, final double y){
		this.x = x;
		this.y = y;
	}
	@Override
	public Point2D apply(final Point2D pt){
		return new Point2D(pt.x + x,pt.y + y);
	}
	@Override
	public Graphics2D inverse(final Graphics2D g){
		g.translate(-x,-y);
		return g;
	}
	@Override
	public Point2D inverse(final Point2D pt){
		return inverse().apply(pt);
	}
	@Override
	public ITransform inverse(){
		return new Translation(-x,-y);
	}
}
