package main;

import helpers.ImageHelper;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class ScreenReader {
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
		screenRectangle = new Rectangle(upperLeft.x, upperLeft.y, 600, 600);
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
				processNewImage(screen);
				// Adjusting the output image...
				ImageHelper.applyFunction(screen, (Integer i) -> ImageHelper.colorDist(i, 0xFF000000) < 100 ? 0xFF000000 : 0xFFFFFFFF);
				// screen = ImageHelper.getClosed(screen, 1);
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

	public static void main(String[] args) {
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
