package helpers;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;

/**
 * @author Dwight
 */
public class ImageHelper {
	static Map<String, BufferedImage> imgMap = new HashMap<String, BufferedImage>();

	public static BufferedImage load(String fileName) {
		BufferedImage img = null;
		if (imgMap.containsKey(fileName)) {
			img = imgMap.get(fileName);
		} else {
			try {
				InputStream resource = ImageHelper.class.getClassLoader().getResourceAsStream("img/" + fileName);
				if (resource == null) {
					return null;
				}
				img = ImageIO.read(resource);

				imgMap.put(fileName, img);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return img;
	}

	public static JFrame showImage(BufferedImage img) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(img)));
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	public static void showImage(BufferedImage img, JFrame frame) {
		frame.remove(0);
		frame.getContentPane().add(new JLabel(new ImageIcon(img)));
		frame.pack();
		frame.setVisible(true);
		// ((JLabel) frame.getContentPane().getComponent(0)).setIcon(new ImageIcon(img));
		// ((JLabel) frame.getContentPane().getComponent(0)).repaint();
	}

	public static Point find(BufferedImage image, BufferedImage subImage) {
		return find(image, subImage, 90);
	}

	public static void saveImage(BufferedImage image, String file) {
		try {
			ImageIO.write(image, "png", new File(file));
		} catch (IOException e) {
		}
	}

	/**
	 * stolen from
	 * http://www.codeproject.com/Tips/752511/How-to-Convert-Mat-to-BufferedImage-Vice-Versa
	 */
	public static BufferedImage mat2Img(Mat in) {
		byte[] data = new byte[in.width() * in.height() * (int) in.elemSize()];
		in.get(0, 0, data);

		// BufferedImage out = new BufferedImage(in.width(), in.height(),
		// BufferedImage.TYPE_INT_RGB);
		BufferedImage out = new BufferedImage(in.width(), in.height(), BufferedImage.TYPE_BYTE_GRAY);
		out.getRaster().setDataElements(0, 0, in.width(), in.height(), data);
		return out;
	}

	public static Point find(BufferedImage image, BufferedImage subImage, int colorEpsilon) {
		for (int x = 0; x < image.getWidth() - subImage.getWidth(); x++) {
			for (int y = 0; y < image.getHeight() - subImage.getHeight(); y++) {
				boolean matches = true;
				for (int sx = 0; sx < subImage.getWidth() && matches; sx++) {
					for (int sy = 0; sy < subImage.getHeight() && matches; sy++) {
						if ((subImage.getRGB(sx, sy) & 0xFF000000) != 0 && colorDist(image.getRGB(x + sx, y + sy), subImage.getRGB(sx, sy)) > colorEpsilon) {
							matches = false;
						}
					}
				}
				if (matches) {
					return new Point(x, y);
				}
			}
		}
		return null;
	}

	public static boolean equal(BufferedImage im1, BufferedImage im2, int colorEpsilon) {
		for (int i = 0; i < im1.getWidth(); i++) {
			for (int j = 0; j < im1.getHeight(); j++) {
				if ((im1.getRGB(i, j) & 0xFF000000) != 0 && (im2.getRGB(i, j) & 0xFF000000) != 0 && colorDist(im1.getRGB(i, j), im2.getRGB(i, j)) > colorEpsilon) {
					return false;
				}
			}
		}
		return true;
	}

	public static BufferedImage getClosed(BufferedImage image, int closeRadius) {
		BufferedImage ret = deepCopy(image);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (image.getRGB(x, y) == 0xFFFFFFFF) {
					for (int sx = Math.max(0, x - closeRadius); sx <= Math.min(image.getWidth() - 1, x + closeRadius + 1); sx++) {
						for (int sy = Math.max(0, y - closeRadius); sy <= Math.min(image.getHeight() - 1, y + closeRadius + 1); sy++) {
							if (image.getRGB(sx, sy) == 0xFF000000) {
								ret.setRGB(x, y, 0xFF000000);
							}
						}
					}
				}
			}
		}
		return ret;
	}

	private interface ImageFunction {
		public int getColor(BufferedImage image, Point pixel);
	}

	public static void applyFunction(BufferedImage image, ImageFunction f) {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				image.setRGB(x, y, f.getColor(image, new Point(x, y)));
			}
		}
	}

	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static int colorDist(int cv1, int cv2) {
		Color c1 = new Color(cv1);
		Color c2 = new Color(cv2);
		return Math.abs(c1.getRed() - c2.getRed()) + Math.abs(c1.getGreen() - c2.getGreen()) + Math.abs(c1.getBlue() - c2.getBlue());
	}

	public static void applyFunction(BufferedImage image, Function<Integer, Integer> f) {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				image.setRGB(x, y, f.apply(image.getRGB(x, y)));
			}
		}
	}
}
