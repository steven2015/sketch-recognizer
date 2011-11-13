package steven.math.transform;

import java.awt.Graphics2D;

import steven.project.csit5110.Point2D;

public class Scale implements ITransform{
	private final double sx;
	private final double sy;

	public Scale(final double sx, final double sy){
		this.sx = sx;
		this.sy = sy;
	}
	@Override
	public Point2D apply(final Point2D pt){
		return new Point2D(pt.x * sx,pt.y * sy);
	}
	@Override
	public Graphics2D inverse(final Graphics2D g){
		g.scale(1 / sx,1 / sy);
		return g;
	}
	@Override
	public Point2D inverse(final Point2D pt){
		return inverse().apply(pt);
	}
	@Override
	public ITransform inverse(){
		return new Scale(1 / sx,1 / sy);
	}
}
