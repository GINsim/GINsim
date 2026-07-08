package org.ginsim.common.callable;

/**
 * Simple implementation of ProgressListener, to retrieve the result and extend.
 * 
 * @author Aurelien Naldi
 *
 * @param <T>  from T
 */
public class BasicProgressListener<T> implements ProgressListener<T> {

	/**
	 * public T result
	 */
	public T result = null;
	
	@Override
	public void setProgress(String text) {
	}

	@Override
	public void setProgress(int n) {
	}

	@Override
	public void milestone(Object data) {
	}

	@Override
	public void setResult(T result) {
		this.result = result;
	}

}
