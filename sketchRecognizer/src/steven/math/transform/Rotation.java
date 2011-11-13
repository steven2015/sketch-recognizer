package steven.math.transform;

import java.awt.Graphics2D;

import steven.project.csit5110.Point2D;

public class Rotation implements ITransform{
	private final double radian;

	public Rotation(final double radian){
		this.radian = radian;
	}
	@Override
	public Point2D apply(final Point2D pt){
		final double x = Math.cos(radian) * pt.x - Math.sin(radian) * pt.y;
		final double y = Math.sin(radian) * pt.x + Math.cos(radian) * pt.y;
		return new Point2D(x,y);
	}
	@Override
	public Graphics2D inverse(final Graphics2D g){
		g.rotate(2 * Math.PI - radian);
		return g;
	}
	@Override
	public Point2D inverse(final Point2D pt){
		return inverse().apply(pt);
	}
	@Override
	public ITransform inverse(){
		return new Rotation(2 * Math.PI - radian);
	}
}
