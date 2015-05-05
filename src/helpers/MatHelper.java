package helpers;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MatHelper {

	public static Mat invertColors(Mat image) {
		Mat invertcolormatrix = new Mat(image.rows(), image.cols(), image.type(), new Scalar(255, 255, 255));

		Mat ret = new Mat();
		Core.subtract(invertcolormatrix, image, ret);
		return ret;
	}

}
