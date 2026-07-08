package org.ginsim.service.tool.avatar.utils;

import java.util.Collection;
import java.util.List;

/** 
 * Facilities associated with simplistic mathematical functions
 * @author Rui Henriques
 * @version 1.0
 */
public class AvaMath {

	/**
	 * Returns the minimum of an integer list
	 * @param vector the list of integers
	 * @return the minimum value
	 */
	public static int min(List<Integer> vector) {
		int min=1000;
		for(Integer i : vector) min=Math.min(min,i);
		return min;
	}

	/**
	 * Returns the maximum of an integer list
	 * @param vector the list of integers
	 * @return the maximum value
	 */
	public static int max(List<Integer> vector) {
		int max=-1000;
		for(Integer i : vector) max=Math.max(max,i);
		return max;
	}

	/**
	 * Returns the sum of an integer list
	 * @param vector the list of integers
	 * @return the sum of values
	 */
	public static int sum(List<Integer> vector) {
		int sum=0;
		for(Integer i : vector) sum+=i;
		return sum;
	}
	
	/**
	 * Returns the sum of an integer collection
	 * @param vector the collection of integers
	 * @return the sum of values
	 */
	public static int sumCollection(Collection<Integer> vector) {
		int sum=0;
		for(Integer i : vector) sum+=i;
		return sum;
	}
	
	/**
	 * Returns the sum of a double collection
	 * @param values the collection of doubles
	 * @return the sum of values
	 */
	public static double sum(Collection<Double> values) {
		double res=0;
		for(Double v : values) res+=v;
		return res;
	}
	
	/**
	 * Returns the sum of a double array
	 * @param vector the array of doubles
	 * @return the sum of values
	 */
	public static double sum(double[] vector) {
		double sum=0;
		for(double i : vector) sum+=i;
		return sum;
	}

	/**
	 * Returns the mean of an integer list
	 * @param vector the list of integers
	 * @return the mean of values
	 */
	public static double mean(List<Integer> vector) {
		if(vector.size()==0) return Double.NaN;
		return ((double)sum(vector))/(double)vector.size();
	}

	/**
	 * Returns the standard deviation of an integer list
	 * @param vector the list of integers
	 * @return the standard deviation of values
	 */
	public static double std(List<Integer> vector) {
		if(vector.size()==0) return Double.NaN;
		double sum = 0, avg = mean(vector);
		for(Integer val : vector) sum += Math.pow(val-avg,2);
		return Math.sqrt(sum/(double)vector.size());	
	}

	/**
	 * Normalizes the columns of a matrix
	 * @param dataset  the matrix whose columns are to be normalized
	 * @return the matrix with the values normalized per column
	 */
	public static double[][] normalizeColumns(double[][] dataset) {
		for(int j=0, l2=dataset[0].length; j<l2; j++){
			double sum = 0;
			for(int i=0, l1=dataset.length; i<l1; i++) sum+=dataset[i][j];
			for(int i=0, l1=dataset.length; i<l1; i++) dataset[i][j]/=sum;
		}
		return dataset;
	}
}
