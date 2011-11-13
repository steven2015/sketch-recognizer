package steven.project.csit5110;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

import javax.imageio.ImageIO;

public class SampleGenerator{
	public static final String id = "smile";
	public static final String filePath = "E:\\Java\\CSIT5110\\src\\steven\\project\\csit5110\\sample\\smile.jpg";

	public static void main(final String[] args) throws Exception{
		final BufferedImage bi = ImageIO.read(new File(filePath));
		final int width = bi.getWidth();
		final int height = bi.getHeight();
		final int[] pixels = bi.getRGB(0,0,width,height,null,0,width);
		double[][] tmps = loadImage(pixels,width,height);
		tmps = filter(tmps,width,height,0);
		tmps = blur(tmps,width,height,1);
		tmps = diff(tmps,width,height,0.1);
		tmps = removeHoles(tmps,width,height);
		tmps = removeOuterPoints(tmps,width,height);
		saveImage(tmps,width,height,"sample");
	}
	public static double[][] loadImage(final int[] pixels, final int width, final int height) throws Exception{
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
			for(int j = 0; j < height; j++){
				final int pixel = pixels[j * width + i];
				final int red = ((pixel & 0xff0000) >> 16);
				final int green = ((pixel & 0xff00) >> 8);
				final int blue = ((pixel & 0xff));
				tmps[i][j] = (red + green + blue) / 255.0 / 3.0;
			}
		}
		return boost(tmps,width,height);
	}
	public static double[][] filter(final double[][] intensitys, final int width, final int height, final double cutoffThreshold){
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
			for(int j = 0; j < height; j++){
				final double intensity = intensitys[i][j];
				if(intensity < cutoffThreshold){
					tmps[i][j] = cutoffThreshold;
				}else{
					tmps[i][j] = intensity;
				}
			}
		}
		return boost(tmps,width,height);
	}
	public static double[][] binary(final double[][] intensitys, final int width, final int height, final double cutoffThreshold){
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
			for(int j = 0; j < height; j++){
				final double intensity = intensitys[i][j];
				if(intensity < cutoffThreshold){
					tmps[i][j] = 0;
				}else{
					tmps[i][j] = 1;
				}
			}
		}
		return tmps;
	}
	public static double[][] diff(final double[][] intensitys, final int width, final int height, final double diffThreshold){
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
			for(int j = 0; j < height; j++){
				if(i != 0 && j != 0 && i != width - 1 && j != height - 1){
					final double intensity = intensitys[i][j];
					if(Math.abs(intensitys[i - 1][j - 1] - intensity) > diffThreshold || Math.abs(intensitys[i][j - 1] - intensity) > diffThreshold
							|| Math.abs(intensitys[i + 1][j - 1] - intensity) > diffThreshold || Math.abs(intensitys[i - 1][j] - intensity) > diffThreshold
							|| Math.abs(intensitys[i + 1][j] - intensity) > diffThreshold || Math.abs(intensitys[i - 1][j + 1] - intensity) > diffThreshold
							|| Math.abs(intensitys[i][j + 1] - intensity) > diffThreshold || Math.abs(intensitys[i + 1][j + 1] - intensity) > diffThreshold){
						tmps[i][j] = 1;
					}else{
						tmps[i][j] = 0;
					}
				}else{
					tmps[i][j] = 0;
				}
			}
		}
		return tmps;
	}
	public static double[][] blur(final double[][] intensitys, final int width, final int height, final double stdDev){
		final double[] blur = new double[(int)(stdDev * 3)];
		final int midBlurLength = blur.length / 2;
		// Gaussian blur
		for(int i = 0; i < blur.length; i++){
			blur[i] = Math.exp(-(i - midBlurLength) * (i - midBlurLength) / 2.0 / stdDev / stdDev) / Math.sqrt(2.0 * Math.PI) / stdDev;
		}
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
		}
		// horizontal
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				final int startx = Math.max(i - midBlurLength,0);
				final int endx = Math.min(i + midBlurLength,width - 1);
				double tmp = 0;
				for(int k = startx; k <= endx; k++){
					tmp += blur[k - i + midBlurLength] * intensitys[k][j];
				}
				tmps[i][j] = tmp * blur.length / (endx - startx + 1);
			}
		}
		final double[][] tmps2 = new double[width][];
		for(int i = 0; i < width; i++){
			tmps2[i] = new double[height];
		}
		// vertical
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				final int starty = Math.max(j - midBlurLength,0);
				final int endy = Math.min(j + midBlurLength,height - 1);
				double tmp = 0;
				for(int k = starty; k <= endy; k++){
					tmp += blur[k - j + midBlurLength] * tmps[i][k];
				}
				tmps2[i][j] = tmp * blur.length / (endy - starty + 1);
			}
		}
		return boost(tmps2,width,height);
	}
	public static double[][] removeHoles(final double[][] intensitys, final int width, final int height){
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
		}
		final double cutoff = 0.5;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(i != 0 && j != 0 && i != width - 1 && j != height - 1){
					if(tmps[i][j] >= 0){
						final double intensity = intensitys[i][j];
						if(intensity <= cutoff){
							if(intensitys[i - 1][j] > cutoff && intensitys[i][j - 1] > cutoff && intensitys[i + 1][j] > cutoff && intensitys[i][j + 1] > cutoff){
								tmps[i][j] = -1;
								tmps[i - 1][j - 1] = -1;
								tmps[i - 1][j] = -1;
								tmps[i - 1][j + 1] = -1;
								tmps[i][j - 1] = -1;
								tmps[i][j + 1] = -1;
								tmps[i + 1][j - 1] = -1;
								tmps[i + 1][j] = -1;
								tmps[i + 1][j + 1] = -1;
							}else{
								tmps[i][j] = 0;
							}
						}else{
							tmps[i][j] = 1;
						}
					}
				}else{
					tmps[i][j] = 0;
				}
			}
		}
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(tmps[i][j] < 0){
					tmps[i][j] = 0;
				}
			}
		}
		return tmps;
	}
	public static double[][] removeOuterPoints(final double[][] intensitys, final int width, final int height){
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
		}
		final double cutoff = 0.5;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(i != 0 && j != 0 && i != width - 1 && j != height - 1){
					final double intensity = intensitys[i][j];
					if(intensity > cutoff){
						final int count = (intensitys[i - 1][j - 1] > cutoff ? 1 : 0) + (intensitys[i - 1][j] > cutoff ? 1 : 0) + (intensitys[i - 1][j + 1] > cutoff ? 1 : 0)
								+ (intensitys[i][j - 1] > cutoff ? 1 : 0) + (intensitys[i][j + 1] > cutoff ? 1 : 0) + (intensitys[i + 1][j - 1] > cutoff ? 1 : 0)
								+ (intensitys[i + 1][j] > cutoff ? 1 : 0) + (intensitys[i + 1][j + 1] > cutoff ? 1 : 0);
						if(count <= 1){
							tmps[i][j] = -1;
							tmps[i - 1][j - 1] = -1;
							tmps[i - 1][j] = -1;
							tmps[i - 1][j + 1] = -1;
							tmps[i][j - 1] = -1;
							tmps[i][j + 1] = -1;
							tmps[i + 1][j - 1] = -1;
							tmps[i + 1][j] = -1;
							tmps[i + 1][j + 1] = -1;
						}else{
							tmps[i][j] = 1;
						}
					}else{
						tmps[i][j] = 0;
					}
				}else{
					tmps[i][j] = 0;
				}
			}
		}
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(tmps[i][j] < 0){
					tmps[i][j] = 0;
				}
			}
		}
		return tmps;
	}
	public static void saveImage(final double[][] intensitys, final int width, final int height, final String fileSuffix) throws Exception{
		final int[] dots = new int[width * height];
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				final int component = (int)((1 - intensitys[i][j]) * 255);
				dots[j * width + i] = 0xff000000 | (component << 16) | (component << 8) | component;
			}
		}
		final BufferedImage tmp = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		tmp.setRGB(0,0,width,height,dots,0,width);
		final BufferedImage bi = new BufferedImage(Sample.squareLength,Sample.squareLength,BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = bi.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(tmp,0,0,Sample.squareLength,Sample.squareLength,null);
		g.dispose();
		final int[] pixels = bi.getRGB(0,0,Sample.squareLength,Sample.squareLength,null,0,Sample.squareLength);
		for(int i = 0; i < pixels.length; i++){
			if((pixels[i] & 0x00ffffff) == 0x00ffffff){
				pixels[i] = 0xffffffff;
			}else{
				pixels[i] = 0xff000000;
			}
		}
		bi.setRGB(0,0,Sample.squareLength,Sample.squareLength,pixels,0,Sample.squareLength);
		ImageIO.write(bi,"png",new File(filePath + fileSuffix + ".png"));
		final String filename = new File(filePath).getName();
		final FileWriter fw = new FileWriter(filePath + ".dat");
		fw.append(id).append(",").append(filename).append(",").append(filename).append(fileSuffix).append(".png").append(",");
		for(final int pixel : pixels){
			if(pixel == 0xffffffff){
				fw.append("0").append(",");
			}else{
				fw.append("1").append(",");
			}
		}
		fw.close();
	}
	public static double[][] boost(final double[][] intensitys, final int width, final int height){
		final double[][] tmps = new double[width][];
		for(int i = 0; i < width; i++){
			tmps[i] = new double[height];
		}
		double min = intensitys[0][0];
		double max = intensitys[0][0];
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				final double tmp = intensitys[i][j];
				if(tmp < min){
					min = tmp;
				}
				if(tmp > max){
					max = tmp;
				}
			}
		}
		final double d = max - min;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				tmps[i][j] = (intensitys[i][j] - min) / d;
			}
		}
		return tmps;
	}
}
