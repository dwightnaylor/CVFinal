package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;
import org.opencv.video.*;
import org.opencv.features2d.*;


public class Parker {
	
	public static void printMap(Map mp) {
	    Iterator it = mp.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}
	
	public static void surfPair(Mat img1, Mat img2) {
		// Create feature detector
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
		
		// Get keypoints from both images
		MatOfKeyPoint kp1 = new MatOfKeyPoint();
		detector.detect(img1, kp1);
		MatOfKeyPoint kp2 = new MatOfKeyPoint();
		detector.detect(img2, kp2);
		
		// Extract Descriptors
		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		
		Mat desc1 = new Mat();
		extractor.compute(img1, kp1, desc1);
		Mat desc2 = new Mat();
		extractor.compute(img2, kp2, desc2);
		
		// Match Descriptors
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(desc1, desc2, matches);
		
		
		List<DMatch> matchList = matches.toList();
		for (int i=0; i < matchList.size(); i++) {
			System.out.println(matchList.get(i).queryIdx + matchList.get(i).trainIdx);
		}
	}
	
	
	
	
	
	private static final int MAX_CORNERS = 500;

    public static void main(String[] args) {
    	
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Load two images and allocate other structures
    	
    	Mat imgA = Highgui.imread("oldScreen.jpg", Highgui.IMREAD_GRAYSCALE);
    	Mat imgB = Highgui.imread("newScreen.jpg", Highgui.IMREAD_GRAYSCALE);
    	
    	surfPair(imgA,imgB);
    	
        int win_sz = 15;

        Mat imgC = Highgui.imread("newScreen.jpg");
        
        // corners might need to be initialized
        MatOfPoint corners_A = new MatOfPoint();
        corners_A.alloc(MAX_CORNERS);
        Imgproc.goodFeaturesToTrack(imgA, corners_A, MAX_CORNERS, 0.05, 30);
        
        System.out.println(corners_A.size());
        
        Size window_sz = new Size(win_sz,win_sz);
        MatOfPoint2f corners2f_A = new MatOfPoint2f(corners_A.toArray());
        TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, 20, 0.03);
        Imgproc.cornerSubPix(imgA, corners2f_A, window_sz, new Size(-1,-1), criteria);

//        Size pyr_sz = new Size(imgA.width() + 8, imgB.height() / 3);
        
//        Mat pyrA = new Mat(pyr_sz, CvType.CV_32FC1);
//        Mat pyrB = new Mat(pyr_sz, CvType.CV_32FC1);
        
        MatOfPoint2f corners2f_B = new MatOfPoint2f();
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        Video.calcOpticalFlowPyrLK(imgA, imgB, corners2f_A, corners2f_B, status, err, window_sz, 3);
        
        System.out.println(corners2f_A.size());
        System.out.println(corners2f_B.size());
        System.out.println(status.size());
        System.out.println(err.size());
        
        Map<Integer,Integer> dataX = new HashMap<Integer,Integer>();
        Map<Integer,Integer> dataY = new HashMap<Integer,Integer>();
        		
        // Make an image of the results
        for (int i = 0; i < corners2f_A.rows(); i++) {
            if (status.get(i,0)[0] == 0 || status.get(i,0)[0] > 550) {
                System.out.println("Error is " + err.get(i,0)[0] + "/n");
                continue;
            }
            Point p0 = new Point(Math.round(corners2f_A.get(i,0)[0]),
                    Math.round(corners2f_A.get(i,0)[1]));
            Point p1 = new Point(Math.round(corners2f_B.get(i,0)[0]),
                    Math.round(corners2f_B.get(i,0)[1]));
            Core.circle(imgC, p0, 7, new Scalar(0,0,0));
            Core.circle(imgC, p1, 7, new Scalar(0,255,0));
            Core.line(imgC, p0, p1, new Scalar(0,0,0), 2, 8, 0);
            
            // Tally up the measured changes
            Integer xVal = (int)Math.round(Math.abs(p0.x-p1.x));
            Integer yVal = (int)Math.round(Math.abs(p0.y-p1.y));
            // ...for delta X
            if (!dataX.containsKey(xVal)) {
            	dataX.put(xVal, 1);
            }
            else {
            	dataX.put(xVal, dataX.get(xVal)+1);
            }
            // ...for delta Y
            if (!dataY.containsKey(yVal)) {
            	dataY.put(yVal, 1);
            }
            else {
            	dataY.put(yVal, dataY.get(yVal)+1);
            }
            
        }
        
        System.out.println("dataX:");
        printMap(dataX);
        System.out.println("dataY:");
        printMap(dataY);
        
        Highgui.imwrite("image0-1.jpg", imgC);

    }
	
}
