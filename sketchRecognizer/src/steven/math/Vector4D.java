package steven.math;

public class Vector4D{
	public double x;
	public double y;
	public double z;
	public double w;

	@Override
	public String toString(){
		return "Vector4D [" + x + "," + y + "," + z + "," + w + "]";
	}
	public double distTo(final Vector4D v){
		final double dx = x - v.x;
		final double dy = y - v.y;
		final double dz = z - v.z;
		final double dw = w - v.w;
		return Math.sqrt(dx * dx + dy * dy + dz * dz + dw * dw);
	}
}
