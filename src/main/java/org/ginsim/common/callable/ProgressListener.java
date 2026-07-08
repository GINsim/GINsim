package org.ginsim.common.callable;

/**
 * Define methods to listen for background task: 
 * Implementors must handle progress messages and the end of the task.
 * @param <R>  from R
 * @author Aurelien Naldi
 */
public interface ProgressListener<R> {

    /**
     * Show task status/progress.
     * 
     * @param text string of text
     */
    void setProgress(String text);

    /**
     * Set a numeric progress counter
     * 
     * @param n number int
     */
    void setProgress(int n);

    /**
     * Inform about intermediate step or result.
     * Handling such events is not mandatory but can allow extensions
     * 
     * @param data object data
     */
    void milestone(Object data);
    
    /**
     * The task finished and returns its result.
     * 
     * @param result R result
     */
    void setResult(R result);
}
