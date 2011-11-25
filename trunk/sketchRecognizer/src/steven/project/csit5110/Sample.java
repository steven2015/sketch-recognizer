package steven.project.csit5110;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import steven.math.transform.ITransform;
import steven.math.transform.Rotation;
import steven.math.transform.Scale;
import steven.math.transform.Translation;
import steven.utility.StringUtility;

public class Sample{
	public final String id;
	public final double[][] square;
	public final List<ITransform> trans = new ArrayList<ITransform>();
	public final List<Point2D> points = new ArrayList<Point2D>();
	public static final int squareLength = 33;
	public static final int midSquareLength = squareLength / 2;
	public static final double stdDev = 3;
	public static final int blurLength = (int)(stdDev * 3);
	public static final int midBlurLength = blurLength / 2;
	public static final int rotationCount = 100;
	public static final List<Sample> samples = new ArrayList<Sample>();
	public final Image image;

	public Sample(final String id){
		this.id = id;
		square = new double[squareLength][];
		for(int i = 0; i < square.length; i++){
			square[i] = new double[square.length];
			for(int j = 0; j < square.length; j++){
				square[i][j] = 0;
			}
		}
		image = null;
	}
	public Sample(final Sample sample, final List<ITransform> trans){
		id = sample.id;
		square = sample.square;
		this.trans.addAll(trans);
		this.points.addAll(sample.points);
		image = null;
	}
	public Sample(final String id, final double[][] square, final Image image){
		this.id = id;
		this.square = square;
		this.image = image;
	}
	protected static Sample createSample(final String id, final List<Point2D> points, final double radian){
		final Sample sample = new Sample(id);
		sample.points.addAll(points);
		// draw lines
		Point2D lastPt = null;
		for(final Point2D pt : rotateAndTransformPoints(points,radian,sample.trans)){
			if(pt != null && lastPt != null){
				final int ptx = (int)(pt.x + midSquareLength);
				final int lastx = (int)(lastPt.x + midSquareLength);
				final int pty = (int)(pt.y + midSquareLength);
				final int lasty = (int)(lastPt.y + midSquareLength);
				final int dx = Math.abs(ptx - lastx);
				final int dy = Math.abs(pty - lasty);
				if(dx > dy){
					if(pty == lasty){
						if(ptx < lastx){
							for(int i = ptx; i <= lastx; i++){
								sample.square[i][pty] = 1;
							}
						}else{
							for(int i = lastx; i <= ptx; i++){
								sample.square[i][pty] = 1;
							}
						}
					}else{
						if(ptx < lastx){
							final int ty = lasty - pty;
							for(int i = ptx; i <= lastx; i++){
								sample.square[i][ty * (i - ptx) / dx + pty] = 1;
							}
						}else{
							final int ty = pty - lasty;
							for(int i = lastx; i <= ptx; i++){
								sample.square[i][ty * (i - lastx) / dx + lasty] = 1;
							}
						}
					}
				}else{
					if(ptx == lastx){
						if(pty < lasty){
							for(int i = pty; i <= lasty; i++){
								sample.square[ptx][i] = 1;
							}
						}else{
							for(int i = lasty; i <= pty; i++){
								sample.square[ptx][i] = 1;
							}
						}
					}else{
						if(pty < lasty){
							final int tx = lastx - ptx;
							for(int i = pty; i <= lasty; i++){
								sample.square[tx * (i - pty) / dy + ptx][i] = 1;
							}
						}else{
							final int tx = ptx - lastx;
							for(int i = lasty; i <= pty; i++){
								sample.square[tx * (i - lasty) / dy + lastx][i] = 1;
							}
						}
					}
				}
			}
			lastPt = pt;
		}
		return blur(sample);
	}
	private static Sample blur(final Sample sample){
		final double[] blur = new double[blurLength];
		// Gaussian blur
		for(int i = 0; i < blur.length; i++){
			blur[i] = Math.exp(-(i - midBlurLength) * (i - midBlurLength) / 2.0 / stdDev / stdDev) / Math.sqrt(2.0 * Math.PI) / stdDev;
		}
		final double[][] tmpSquare = new double[squareLength][];
		for(int i = 0; i < squareLength; i++){
			tmpSquare[i] = new double[squareLength];
		}
		// horizontal
		for(int i = 0; i < squareLength; i++){
			for(int j = 0; j < squareLength; j++){
				final int startx = Math.max(i - midBlurLength,0);
				final int endx = Math.min(i + midBlurLength,squareLength - 1);
				double tmp = 0;
				for(int k = startx; k <= endx; k++){
					tmp += blur[k - i + midBlurLength] * sample.square[k][j];
				}
				tmpSquare[i][j] = tmp;
			}
		}
		// vertical
		for(int i = 0; i < squareLength; i++){
			for(int j = 0; j < squareLength; j++){
				final int starty = Math.max(j - midBlurLength,0);
				final int endy = Math.min(j + midBlurLength,squareLength - 1);
				double tmp = 0;
				for(int k = starty; k <= endy; k++){
					tmp += blur[k - j + midBlurLength] * tmpSquare[i][k];
				}
				sample.square[i][j] = tmp;
			}
		}
		// boost
		double min = sample.square[0][0];
		double max = sample.square[0][0];
		for(int i = 0; i < squareLength; i++){
			for(int j = 0; j < squareLength; j++){
				final double tmp = sample.square[i][j];
				if(tmp < min){
					min = tmp;
				}
				if(tmp > max){
					max = tmp;
				}
			}
		}
		final double d = max - min;
		for(int i = 0; i < squareLength; i++){
			for(int j = 0; j < squareLength; j++){
				final double tmp = sample.square[i][j];
				sample.square[i][j] = (tmp - min) / d;
			}
		}
		return sample;
	}
	private static List<Point2D> transformPoints(final List<Point2D> points, final List<ITransform> trans){
		// find min, max
		double minX = -1;
		double maxX = -1;
		double minY = -1;
		double maxY = -1;
		for(final Point2D pt : points){
			if(pt != null){
				minX = pt.x;
				maxX = pt.x;
				minY = pt.y;
				maxY = pt.y;
				break;
			}
		}
		for(final Point2D pt : points){
			if(pt != null){
				if(pt.x < minX){
					minX = pt.x;
				}
				if(pt.x > maxX){
					maxX = pt.x;
				}
				if(pt.y < minY){
					minY = pt.y;
				}
				if(pt.y > maxY){
					maxY = pt.y;
				}
			}
		}
		// transform
		final double denominatorX = (maxX - minX) + 1;
		final double denominatorY = (maxY - minY) + 1;
		final ITransform tran1 = new Translation(-minX,-minY);
		final ITransform tran2 = new Scale((squareLength - blurLength) / denominatorX,(squareLength - blurLength) / denominatorY);
		final ITransform tran3 = new Translation(-(midSquareLength - midBlurLength),-(midSquareLength - midBlurLength));
		final List<Point2D> tmps = new ArrayList<Point2D>();
		for(final Point2D pt : points){
			if(pt != null){
				tmps.add(tran3.apply(tran2.apply(tran1.apply(pt))));
			}else{
				tmps.add(null);
			}
		}
		trans.add(tran1);
		trans.add(tran2);
		trans.add(tran3);
		return tmps;
	}
	private static List<Point2D> rotateAndTransformPoints(final List<Point2D> points, final double radian, final List<ITransform> trans){
		final List<Point2D> tmps = new ArrayList<Point2D>();
		final ITransform tran = new Rotation(radian);
		for(final Point2D pt : transformPoints(points,trans)){
			if(pt != null){
				tmps.add(tran.apply(pt));
			}else{
				tmps.add(null);
			}
		}
		trans.add(tran);
		return transformPoints(tmps,trans);
	}
	public static synchronized Sample match(final List<Point2D> points){
		final long t = System.nanoTime();
		final Sample[] sketchs = new Sample[rotationCount];
		for(int i = 0; i < rotationCount; i++){
			final double radian = i * 2 * Math.PI / rotationCount;
			sketchs[i] = createSample("Sketch-" + radian,points,radian);
		}
		int sampleIndex = -1;
		int rotationIndex = -1;
		double minError = -1;
		for(int i = 0; i < samples.size(); i++){
			final Sample sample = samples.get(i);
			for(int j = 0; j < rotationCount; j++){
				final double error = sample.match(sketchs[j]);
				System.out.println(sample.id + " " + error);
				if(minError < 0 || error < minError){
					minError = error;
					sampleIndex = i;
					rotationIndex = j;
				}
			}
		}
		System.out.println("Best match: " + samples.get(sampleIndex).id + ", error: " + minError + ", radian: " + rotationIndex * 2 * Math.PI / rotationCount + ", spent: " + (System.nanoTime() - t)
				/ 1000000 + " ms");
		final Sample match = samples.get(sampleIndex);
		final List<ITransform> trans = sketchs[rotationIndex].trans;
		for(int i = match.trans.size() - 1; i >= 0; i--){
			trans.add(match.trans.get(i).inverse());
		}
		return new Sample(match,trans);
	}
	public static synchronized Sample addSample(final String id, final List<Point2D> points){
		final Sample sample = createSample(id,points,0);
		samples.add(sample);
		return sample;
	}
	public static synchronized Sample addSample(final String id, final double[][] intensitys, final Image image){
		final Sample sample = blur(createSample(id,intensitys,image));
		samples.add(sample);
		return sample;
	}
	public static void generateSample(){
		final List<Point2D> points = new ArrayList<Point2D>();
		final int length = squareLength * 8;
		// rectangle
		points.clear();
		points.add(new Point2D(0,0));
		points.add(new Point2D(length - 1,0));
		points.add(new Point2D(length - 1,length - 1));
		points.add(new Point2D(0,length - 1));
		points.add(new Point2D(0,0));
		addSample("Rectangle",points);
		// circle
		points.clear();
		points.add(new Point2D(0,length));
		for(int i = 0; i < rotationCount; i++){
			final double radian = i * 2 * Math.PI / rotationCount;
			final int x = (int)(-length * Math.sin(radian));
			final int y = (int)(length * Math.cos(radian));
			points.add(new Point2D(x,y));
		}
		points.add(new Point2D(0,length));
		addSample("Oval",points);
		// triangle
		points.clear();
		points.add(new Point2D(length / 2,0));
		points.add(new Point2D(length - 1,length - 1));
		points.add(new Point2D(0,length - 1));
		points.add(new Point2D(length / 2,0));
		addSample("Triangle",points);
		// right triangle
		points.clear();
		points.add(new Point2D(length - 1,0));
		points.add(new Point2D(length - 1,length - 1));
		points.add(new Point2D(0,length - 1));
		points.add(new Point2D(length - 1,0));
		addSample("Right Triangle",points);
	}
	public static void loadSample(){
		final String folderPath = Sample.class.getClassLoader().getResource(".").toExternalForm().substring(5) + "/steven/project/csit5110/sample/";
		final File folder = new File(folderPath);
		for(final File file : folder.listFiles()){
			if(file.isFile() && file.getName().endsWith(".dat")){
				final char[] buffer = new char[(int)file.length()];
				int count = 0;
				try{
					final FileReader fr = new FileReader(file);
					count = fr.read(buffer);
					fr.close();
				}catch(final Exception e){
					e.printStackTrace();
				}
				final String[] segments = StringUtility.split(new String(buffer,0,count),",");
				final double[][] tmps = new double[squareLength][];
				int k = 0;
				for(int i = 0; i < squareLength; i++){
					tmps[i] = new double[squareLength];
					for(int j = 0; j < squareLength; j++){
						tmps[i][j] = Double.parseDouble(segments[k + 3]);
						k++;
					}
				}
				try{
					addSample(segments[0],tmps,ImageIO.read(new File(folderPath + segments[1])));
				}catch(final Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	public void draw(final Graphics2D g){
		final AffineTransform backup = g.getTransform();
		g.setTransform(g.getDeviceConfiguration().getDefaultTransform());
		for(int i = trans.size() - 1; i >= 0; i--){
			trans.get(i).inverse(g);
		}
		g.setTransform(backup);
	}
	public List<Point2D> getInversePoints(){
		final List<Point2D> tmps = new ArrayList<Point2D>();
		for(final Point2D pt : points){
			if(pt != null){
				Point2D tmp = new Point2D(pt.x,pt.y);
				for(int i = trans.size() - 1; i >= 0; i--){
					tmp = trans.get(i).inverse(tmp);
				}
				tmps.add(tmp);
			}else{
				tmps.add(null);
			}
		}
		return tmps;
	}
	private double match(final Sample sketch){
		double error = 0;
		for(int i = 0; i < squareLength; i++){
			for(int j = 0; j < squareLength; j++){
				final double dist = square[i][j] - sketch.square[i][j];
				error += dist * dist;
			}
		}
		return error;
	}
	protected static Sample createSample(final String id, final double[][] intensitys, final Image image){
		return new Sample(id,intensitys,image);
	}
}
