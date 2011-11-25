package terrence.recognizer.steven;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import terrence.recognizer.interaction.dollar.Result;

public class Rectangle implements FinalObject{
	private final double x;
	private final double y;
	private final double width;
	private final double height;
	private final Color color;
	private final double rotationAngle;

	public Rectangle(final Result result){
		x = result.bounds[0];
		y = result.bounds[1];
		width = result.bounds[2] - x;
		height = result.bounds[3] - y;
		color = new Color((int)(Math.random() * 256),(int)(Math.random() * 256),(int)(Math.random() * 256));
		rotationAngle = 0;
	}
	@Override
	public void render(final Graphics2D g){
		final Color bakColor = g.getColor();
		final AffineTransform bakTransform = g.getTransform();
		g.setColor(color);
		g.translate(x,y);
		g.rotate(rotationAngle);
		g.fillRect(0,0,(int)width,(int)height);
		g.setColor(bakColor);
		g.setTransform(bakTransform);
	}
}
