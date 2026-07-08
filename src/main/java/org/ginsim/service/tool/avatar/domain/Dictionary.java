package org.ginsim.service.tool.avatar.domain;

import java.math.BigInteger;

/**
 * Class responsible for the efficient calculus of the hash code of a given state
 * 
 * @author Rui
 * @version 1.0
 */
public final class Dictionary {
	
	private static int ncomps;
	private static long[] factors;
	private static BigInteger[] hugeFactors;
	private static boolean huge = false;
	
	/**
	 * Loads the hash function: sum of the Cartesian product between the given parameter and a given state
	 * @param factors the hash function to compute the key of a given state
	 */
	public static void codingLongStates(BigInteger[] factors){
		hugeFactors = factors;
		ncomps = hugeFactors.length;
		huge = true;
	}
	
	/**
	 * Loads the hash function: sum of the Cartesian product between the given parameter and a given state
	 * @param _factors the hash function to compute the key of a given state
	 */
	public static void codingShortStates(long[] _factors){
		factors = _factors;
		ncomps = factors.length;
		huge = false;
	}
	
	/**
	 * Computes the key of a state based on the values of its components
	 * @param state the state
	 * @return the string identifier of the given state
	 */
	public static String toKey(byte[] state) {
		return huge ? toBigKey(state) : toNumericKey(state)+"";
	}
	
	/**
	 * Computes the key of a state based on the values of its components
	 * @param state the state
	 * @return the numeric identifier of the given state
	 */
	public static long toNumericKey(byte[] state) {
		long key=0;
		for(int i=0; i<ncomps; i++) key+=state[i]*factors[i];
		return key;
	}
	
	/**
	 * Computes the key of a long state based on the values of its components
	 * @param state the long state
	 * @return the string identifier of the given state
	 */
	public static String toBigKey(byte[] state) {
		BigInteger sum = new BigInteger("0");
		for(int i=0; i<ncomps; i++)
			sum = sum.add(hugeFactors[i].multiply(new BigInteger(state[i]+""))); 
		return sum.toString();
	}
}
