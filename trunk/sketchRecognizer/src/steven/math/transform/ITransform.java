package steven.math.transform;

import java.awt.Graphics2D;

import steven.project.csit5110.Point2D;

public interface ITransform{
	public Point2D apply(Point2D pt);
	public Graphics2D inverse(Graphics2D g);
	public Point2D inverse(Point2D pt);
	public ITransform inverse();
}
