package main;

import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;
import helpers.ImageHelper;
import helpers.MatHelper;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class ScreenReader {
	final Mat CLOSING_KERNEL = Mat.ones(9, 9, CV_8UC1);

	Robot r;
	Rectangle screenRectangle;
	BufferedImage upperLeftImage = ImageHelper.load("windowUpperLeft.png");

	/**
	 * The change in screen location since the last frame
	 */
	Point change = new Point(0, 0);
	BufferedImage oldImage;

	public ScreenReader() {
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public BufferedImage getWholeScreen() {
		return r.createScreenCapture(new Rectangle(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height));
	}

	public void isolateGameScreen() {
		System.out.println("Finding screen...");
		BufferedImage screen = getWholeScreen();
		Point upperLeftRough = ImageHelper.find(screen, upperLeftImage);
		if (upperLeftRough == null) {
			System.err.println("Unable to find game window.");
			System.exit(0);
		}
		Point upperLeft = new Point(upperLeftRough.x - 22, upperLeftRough.y + 20);
		screenRectangle = new Rectangle(upperLeft.x, upperLeft.y, 590, 590);
		// ImageHelper.showImage(r.createScreenCapture(screenRectangle));
	}

	public void reisolateIfNecessary() {
		BufferedImage upperLeft = r.createScreenCapture(new Rectangle(screenRectangle.x + 22, screenRectangle.y - 20, upperLeftImage.getWidth(), upperLeftImage.getHeight()));
		if (!ImageHelper.equal(upperLeft, upperLeftImage, 90)) {
			isolateGameScreen();
		}
	}

	private void processNewImage(BufferedImage newImage) {
		if (oldImage != null) {
			// ImageHelper.saveImage(oldImage, "im1.png");
			// ImageHelper.saveImage(newImage, "im2.png");
			// System.exit(0);
		}
		oldImage = newImage;
	}

	public void showDisplay() {
		JFrame frame = new JFrame();
		frame.setContentPane(new JRootPane() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				reisolateIfNecessary();
				BufferedImage screen = r.createScreenCapture(screenRectangle);
				// processNewImage(ImageHelper.deepCopy(screen));
				processNewImage(screen);
				ImageHelper.applyFunction(screen, (Integer i) -> ImageHelper.colorDist(i, 0xFF000000) < 100 ? 0xFF000000 : 0xFFFFFFFF);
				// Adjusting the output image...
				Mat screenMat = createScreenMatrix(screen);

				dealWithContours(screenMat, screen);

				// g.drawImage(ImageHelper.mat2Img(screenMat), 0, 0, null);
				g.drawImage(screen, 0, 0, null);
			}
		});
		frame.getContentPane().setPreferredSize(new Dimension(screenRectangle.width, screenRectangle.height));
		frame.pack();
		frame.setVisible(true);
		while (true) {
			frame.repaint();
		}
	}

	private Mat createScreenMatrix(BufferedImage screen) {
		Mat screenMat = new Mat(590, 590, CV_32S);
		screenMat.put(0, 0, ((DataBufferInt) screen.getRaster().getDataBuffer()).getData());
		screenMat.convertTo(screenMat, CV_8U);
		morphologyEx(screenMat, screenMat, MORPH_OPEN, CLOSING_KERNEL);
		return screenMat;
	}

	ArrayList<MatOfPoint> lastContours;

	private void processNewContours(ArrayList<MatOfPoint> contours) {
		if (lastContours != null) {
			Point delta = estimateDelta(contours);
			for (int i = 0; i < lastContours.size(); i++) {
				Rect pastRect = Imgproc.boundingRect(lastContours.get(i));
				int dist = Integer.MAX_VALUE;
				int best = -1;
				for (int j = 0; j < contours.size(); j++) {
					Rect rect = Imgproc.boundingRect(contours.get(j));
					if (contours.get(j).size().equals(lastContours.get(i).size())) {
						int curDist = Math.abs(pastRect.x + delta.x - rect.x) + Math.abs(pastRect.y + delta.y - rect.y);
						if (best == -1 || dist > curDist) {
							dist = curDist;
							best = j;
						}
					}
				}
				if (best != -1) {
					if (dist != 0) {
						int x = pastRect.x + pastRect.width / 2 + delta.x;
						int y = pastRect.y + pastRect.height / 2 + delta.y;
						if (x > 30 && y > 30 && x < screenRectangle.width - 30 && y < screenRectangle.height - 30)
							Clicker.clickAt(screenRectangle.x + x, screenRectangle.y + y);
					}
				}
			}
		}
		lastContours = contours;
	}

	private Point estimateDelta(ArrayList<MatOfPoint> contours) {
		Point maxDelta = null;
		Hashtable<Point, Integer> deltas = new Hashtable<Point, Integer>();
		// Estimate the delta for now
		for (int i = 0; i < lastContours.size(); i++) {
			Rect pastRect = Imgproc.boundingRect(lastContours.get(i));
			int dist = Integer.MAX_VALUE;
			int best = -1;
			for (int j = 0; j < contours.size(); j++) {
				Rect rect = Imgproc.boundingRect(contours.get(j));
				if (contours.get(j).size().equals(lastContours.get(i).size())) {
					int curDist = Math.abs(pastRect.x - rect.x) + Math.abs(pastRect.y - rect.y);
					if (best == -1 || dist > curDist) {
						dist = curDist;
						best = j;
					}
				}
			}
			if (best != -1) {
				Rect toUse = Imgproc.boundingRect(contours.get(best));
				Point shift = new Point(toUse.x - pastRect.x, toUse.y - pastRect.y);
				if (!deltas.containsKey(shift)) {
					deltas.put(shift, 0);
				}
				deltas.put(shift, deltas.get(shift) + 1);
				if (maxDelta == null || deltas.get(maxDelta) < deltas.get(shift)) {
					maxDelta = shift;
				}
			}
		}
		if (maxDelta == null) {
			maxDelta = new Point(0, 0);
		}
		return maxDelta;
	}

	private void dealWithContours(Mat screenMat, BufferedImage screen) {
		Graphics g = screen.getGraphics();
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		findContours(MatHelper.invertColors(screenMat), contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
		System.out.println(contours.size() + "--------------------------------------");
		contours.removeIf(p -> {
			Rect rect = Imgproc.boundingRect(p);
			if (rect.width <= 20 || rect.height <= 20)
				return true;
			if (20 > Math.max(Math.abs(rect.x + rect.width / 2 - screenRectangle.getWidth() / 2), Math.abs(rect.y + rect.height / 2 - screenRectangle.getHeight() / 2)))
				return true;
			return false;
		});
		processNewContours(contours);
		// Pretty drawing here
		for (MatOfPoint mp : contours) {
			Rect rect = Imgproc.boundingRect(mp);
			// if (20 > Math.max(Math.abs(rect.x - screenRectangle.getWidth() / 2), Math.abs(rect.y
			// - screenRectangle.getHeight() / 2)))
			// continue;
			// Core.rectangle(screenMat, new org.opencv.core.Point(rect.x, rect.y), new
			// org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height), new Scalar(128),
			// 3);
			g.setColor(new Color((int) (Math.random() * Integer.MAX_VALUE)));
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ScreenReader s = new ScreenReader();
		s.isolateGameScreen();
		s.showDisplay();
	}
}
