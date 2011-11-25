package terrence.recognizer.interaction.dollar;

public class Result
{
	public String Name;
	public double Score;
	public int Index;
	// steven 20111120 bounding box start
	public double[] bounds = new double[4];
	public double bestAngleRadian;
	// steven 20111120 bounding box end

	// steven 20111120 bounding box start
	@Deprecated
	// steven 20111120 bounding box end
	public Result(String name, double score, int index)
	{
		this.Name = name;
		this.Score = score;
		this.Index = index;
	}
	// steven 20111120 bounding box start
	public Result(final String name, final double score, final int index, final double[] bounds, double bestAngle){
		Name=name;
		Score=score;
		Index=index;
		this.bounds[0] = bounds[0];
		this.bounds[1] = bounds[1];
		this.bounds[2] = bounds[2];
		this.bounds[3] = bounds[3];
		this.bestAngleRadian = bestAngle / 360;
	}
	// steven 20111120 bounding box end
}
