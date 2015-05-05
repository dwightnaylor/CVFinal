package main;

import helpers.DelayHelper;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class Clicker {
	private static final int minWaitTime = 50;
	private static long lastClick = 0;
	private static boolean paused = false;
	private static Point lastPoint;
	private static Robot robotDontUse;

	public static void clickAt(int x, int y) {
		mouseMove(x, y);
		if (System.currentTimeMillis() - lastClick < minWaitTime) {
			return;
		}
		lastClick = System.currentTimeMillis();
		r().mousePress(InputEvent.BUTTON1_MASK);
		DelayHelper.delay(10);
		r().mouseRelease(InputEvent.BUTTON1_MASK);
	}

	public static void mouseMove(int x, int y) {
		checkForKill();
		lastPoint = new Point(x, y);
		r().mouseMove(x, y);
	}

	private static void checkForKill() {
		while (paused) {
			DelayHelper.delay(10);
		}
		if (lastPoint != null && !MouseInfo.getPointerInfo().getLocation().equals(lastPoint) && !paused && System.currentTimeMillis() - lastClick < 5000) {
			lastPoint = null;
			paused = true;
			new Thread() {
				@Override
				public void run() {
					System.out.println("Pausing mouse mover for ten seconds.");
					DelayHelper.delay(10000);
					paused = false;
					System.out.println("Done pausing mouse mover.");
				}
			}.start();
		}
	}

	private static Robot r() {
		if (robotDontUse == null) {
			try {
				robotDontUse = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		return robotDontUse;
	}
}
