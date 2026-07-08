package org.ginsim.service.tool.avatar.utils;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.ejml.simple.SimpleMatrix;
/*import org.la4j.LinearAlgebra.InverterFactory;
import org.la4j.Matrix;
import org.la4j.inversion.MatrixInverter;
import org.la4j.matrix.dense.Basic2DMatrix;*/

/**
 * Facilities to manipulate matrices (focus on the inversion procedure)
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class AvaMatrix {

	/**
	 * Inverts a matrix using La4J library
	 * 
	 * @param matrix the matrix to be inverted
	 * @return the inverted matrix
	 */
	public static double[][] inverseLa4J(double[][] matrix) {
		/*
		 * Matrix result = new Basic2DMatrix(matrix); MatrixInverter inverter =
		 * result.withInverter(InverterFactory.GAUSS_JORDAN);
		 * //InverterFactory.NO_PIVOT_GAUSS, InverterFactory.SMART Matrix e =
		 * inverter.inverse(); for(int i=0, l1=e.rows(); i<l1; i++) for(int j=0,
		 * l2=e.columns(); j<l2; j++) matrix[i][j]=e.get(i,j);
		 */
		return matrix;
	}

	/**
	 * Inverts a matrix using EJML library
	 * 
	 * @param matrix
	 *            the matrix to be inverted
	 * @return the inverted matrix
	 */
	public static double[][] inverseSimpleEJML(double[][] matrix) {
		SimpleMatrix S = new SimpleMatrix(matrix);
		// DenseMatrix64F S = new DenseMatrix64F(matrix);
		S = S.invert();
		for (int i = 0, l1 = S.numRows(); i < l1; i++)
			for (int j = 0, l2 = S.numCols(); j < l2; j++)
				matrix[i][j] = S.get(i, j);
		return matrix;
	}

	/**
	 * Inverts a matrix using Commons library
	 * 
	 * @param matrix
	 *            the matrix to be inverted
	 * @return the inverted matrix
	 */
	public static double[][] inverseCommons(double[][] matrix) {
		return new LUDecomposition(MatrixUtils.createRealMatrix(matrix)).getSolver().getInverse().getData();
	}
}
