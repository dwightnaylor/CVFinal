package helpers;

/**
 * A class made to help with all sorts of work with arrays.
 * 
 * @author Dwight Naylor
 * @since 8/19/12
 */
public class ArrayHelper {

	/**
	 * Converts the given integer array into a boolean array, returning a
	 * boolean array with the same length.<br>
	 * <br>
	 * Each value in the boolean array is whether or not the corresponding value
	 * in the given array is greater than zero.
	 */
	public static boolean[] convertToBooleanArray(int... values) {
		boolean[] ret = new boolean[values.length];
		for (int i = 0; i < values.length; i++) {
			ret[i] = values[i] > 0;
		}
		return ret;
	}

	public static boolean[] reverse(boolean... arg) {
		boolean[] ret = new boolean[arg.length];
		for (int i = 0; i < arg.length; i++) {
			ret[arg.length - 1 - i] = arg[i];
		}
		return ret;
	}

	public static int[] array(int value, int num) {
		int[] ret = new int[num];
		for (int i = 0; i < num; i++) {
			ret[i] = value;
		}
		return ret;
	}

	public static boolean[] array(boolean value, int num) {
		boolean[] ret = new boolean[num];
		for (int i = 0; i < num; i++) {
			ret[i] = value;
		}
		return ret;
	}

	/**
	 * Averages the given array of arrays together, such that at each index of
	 * the returned array, the value therein is equal to the sum of the values
	 * of each array at that same index divided by the number of arrays.
	 * 
	 * @param arrays
	 *            The arrays to add together.
	 * @return The added list of arrays.
	 */
	public static double[] averageArrays(double[][] arrays) {
		int maxLength = 0;
		for (int i = 0; i < arrays.length; i++) {
			if (arrays[i].length > maxLength) {
				maxLength = arrays[i].length;
			}
		}
		double[] ret = new double[maxLength];
		for (int i = 0; i < maxLength; i++) {
			double cur = 0;
			for (int a = 0; a < arrays.length; a++) {
				if (i < arrays[a].length) {
					cur += arrays[a][i];
				}
			}
			ret[i] = cur / arrays.length;
		}
		return ret;
	}

	/**
	 * Adds the given array of arrays together, such that at each index of the
	 * returned array, the value therein is equal to the sum of the values of
	 * each array at that same index.
	 * 
	 * @param arrays
	 *            The arrays to add together.
	 * @return The added list of arrays.
	 */
	public static double[] addArrays(double[][] arrays) {
		int maxLength = 0;
		for (int i = 0; i < arrays.length; i++) {
			if (arrays[i].length > maxLength) {
				maxLength = arrays[i].length;
			}
		}
		double[] ret = new double[maxLength];
		for (int i = 0; i < maxLength; i++) {
			double cur = 0;
			for (int a = 0; a < arrays.length; a++) {
				if (i < arrays[a].length) {
					cur += arrays[a][i];
				}
			}
			ret[i] = cur;
		}
		return ret;
	}

	/**
	 * Returns an array containing the nth value of each input array.
	 */
	public static double[] getVerticalValues(double[][] d, int index) {
		double[] ret = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			ret[i] = d[i][index];
		}
		return ret;
	}

	/**
	 * Returns a negated copy of the current array, such that for all indexes
	 * the value in the returned array is the negative of the value in the given
	 * array.
	 * 
	 * @param d
	 *            The array to make a negated copy of.
	 * @return A negated copy of the array.
	 */
	public static double[] getNegatedArray(double[] d) {
		double[] ret = new double[d.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = -d[i];
		}
		return ret;
	}

	/**
	 * Returns a multiplied copy of the current array, such that for all indexes
	 * the value in the returned array is the multiple of the value in the given
	 * array.
	 * 
	 * @param d
	 *            The array to make a multiplied copy of.
	 * @return A multiplied copy of the array.
	 */
	public static double[] getMultipliedArray(double[] d, double mult) {
		double[] ret = new double[d.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = d[i] * mult;
		}
		return ret;
	}

	/**
	 * Returns an array of integers containing all of the numbers rounded to
	 * integers with the Math.round() function.
	 * 
	 * @param values
	 *            The array of values to round.
	 * @return The list of rounded integers.
	 */
	public static int[] round(double[] values) {
		int[] ret = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			ret[i] = (int) Math.round(values[i]);
		}
		return ret;
	}

	public static String toString(boolean[] b) {
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			if (b[i]) {
				ret.append(1);
			} else {
				ret.append(0);
			}
		}
		return ret.toString();
	}

	public static String toString(int[] b) {
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < b.length - 1; i++) {
			ret.append(b[i] + ",");
		}
		if (b.length > 0) {
			ret.append(b[b.length - 1]);
		}
		return ret.toString();
	}

	public static String toString(double[] b) {
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < b.length - 1; i++) {
			ret.append(b[i] + ",");
		}
		if (b.length > 0) {
			ret.append(b[b.length - 1]);
		}
		return ret.toString();
	}
}
