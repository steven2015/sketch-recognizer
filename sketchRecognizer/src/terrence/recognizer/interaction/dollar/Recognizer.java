package terrence.recognizer.interaction.dollar;

import java.util.Vector;

public class Recognizer
{
	//
	// Recognizer class constants
	//
	int NumTemplates = 16;
	public static int NumPoints = 64;
	public static double SquareSize = 250.0;
	double HalfDiagonal = 0.5 * Math.sqrt(250.0 * 250.0 + 250.0 * 250.0);
	double AngleRange = 45.0;
	double AnglePrecision = 2.0;
	public static double Phi = 0.5 * (-1.0 + Math.sqrt(5.0)); // Golden Ratio
	
	public Point centroid = new Point(0, 0);
	public Rectangle boundingBox = new Rectangle(0, 0, 0, 0);
	double bounds[] = { 0.0, 0.0, 0.0, 0.0 };
	
	Vector Templates = new Vector(NumTemplates);

	public static final int GESTURES_DEFAULT = 1;
	public static final int GESTURES_SIMPLE = 2;
	public static final int GESTURES_CIRCLES = 3;	
	
	public Recognizer()
	{
		this(GESTURES_SIMPLE);
	}

	public Recognizer(int gestureSet)
	{
		switch(gestureSet)
		{
			case GESTURES_DEFAULT:
				loadTemplatesDefault(); break;

			case GESTURES_SIMPLE:
				loadTemplatesSimple();	break;

			case GESTURES_CIRCLES:
				loadTemplatesCircles();	break;
		}
	}
	
	void loadTemplatesDefault()
	{
		Templates.addElement(loadTemplate("triangle", TemplateData.trianglePoints));
		Templates.addElement(loadTemplate("x", TemplateData.xPoints));
		Templates.addElement(loadTemplate("rectangle CCW", TemplateData.rectanglePointsCCW));
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("check", TemplateData.checkPoints));
		Templates.addElement(loadTemplate("caret CW", TemplateData.caretPointsCW));
		Templates.addElement(loadTemplate("question", TemplateData.questionPoints));
		Templates.addElement(loadTemplate("arrow", TemplateData.arrowPoints));
		Templates.addElement(loadTemplate("leftSquareBracket", TemplateData.leftSquareBracketPoints));
		Templates.addElement(loadTemplate("rightSquareBracket", TemplateData.rightSquareBracketPoints));
		Templates.addElement(loadTemplate("v", TemplateData.vPoints));
		Templates.addElement(loadTemplate("delete", TemplateData.deletePoints));	
		Templates.addElement(loadTemplate("leftCurlyBrace", TemplateData.leftCurlyBracePoints));
		Templates.addElement(loadTemplate("rightCurlyBrace", TemplateData.rightCurlyBracePoints));
		Templates.addElement(loadTemplate("star", TemplateData.starPoints));
		Templates.addElement(loadTemplate("pigTail", TemplateData.pigTailPoints));
	}
	
	void loadTemplatesSimple()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
		Templates.addElement(loadTemplate("rectangle CCW", TemplateData.rectanglePointsCCW));
		Templates.addElement(loadTemplate("rectangle CW", TemplateData.rectanglePointsCW));
		Templates.addElement(loadTemplate("caret CCW", TemplateData.caretPointsCCW));
		Templates.addElement(loadTemplate("caret CW", TemplateData.caretPointsCW));
	}
	

	void loadTemplatesCircles()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
	}
	
	Template loadTemplate(String name, int[] array)
	{
		return new Template(name, loadArray(array));
	}
	
	Vector loadArray(int[] array)
	{
		Vector v = new Vector(array.length/2);
		for (int i = 0; i < array.length; i+= 2)
		{
			Point p = new Point(array[i], array[i+1]);
			v.addElement(p);
		}
		
	//	System.out.println(v.size() + " " + array.length);
	
		return v;
	}
	
	public Result Recognize(Vector points)
	{
		points = Utils.Resample(points, NumPoints);		
		points = Utils.RotateToZero(points, centroid, boundingBox);
		points = Utils.ScaleToSquare(points, SquareSize);
		points = Utils.TranslateToOrigin(points);
	
		bounds[0] = boundingBox.X;
		bounds[1] = boundingBox.Y;
		bounds[2] = boundingBox.X + boundingBox.Width;
		bounds[3] = boundingBox.Y + boundingBox.Height;
		
		int t = 0;
		
		// steven 20111120 best angle start
		double bestAngle = 0;
		// steven 20111120 best angle end
		double b = Double.MAX_VALUE;
		for (int i = 0; i < Templates.size(); i++)
		{
			double d = Utils.DistanceAtBestAngle(points, (Template)Templates.elementAt(i), -AngleRange, AngleRange, AnglePrecision);
			if (d < b)
			{
				b = d;
				t = i;
				// steven 20111120 best angle start
				bestAngle = Utils.bestAngle;
				// steven 20111120 best angle end
			}
		}
		double score = 1.0 - (b / HalfDiagonal);
		// steven 20111120 bounding box start
		return new Result(((Template)Templates.elementAt(t)).Name, score, t, bounds, bestAngle);
		// steven 20111120 bounding box end
	};

	int AddTemplate(String name, Vector points)
	{
		Templates.addElement(new Template(name, points));
		return Templates.size();
	}
	
	int DeleteUserTemplates()
	{
		for (int i = Templates.size()-NumTemplates; i > 0; i--)
		{
			Templates.removeElementAt(Templates.size()-1);
		}
		
		return Templates.size();
	}

}
