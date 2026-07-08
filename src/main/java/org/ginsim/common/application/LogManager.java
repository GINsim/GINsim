package org.ginsim.common.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A simple helper class to log things in System.out or System.err
 * 
 * The log schema is :
 *    lineNumber:className#methodName() msg\n
 *    
 * eg. log("a fancy message") will print the following line on System.out : 
 *    43:LogManager#getLineNumber() a fancy message\n
 *
 * @author Duncan Berenguier
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 */
public class LogManager {
	private static PrintStream debugOut = System.err;
	private static int debugmask = 0;
	
	// Logging verboseLevel : 0 = log; 1= info; 2 = trace
	private static int verboseLevel = 1;
	
	// Activate/unactivate the output of 'error', 'info' and 'trace' on the standard outputs (sys.err and sys.out)
	private static boolean debugMode = false;
	
	// PrintWriter of each log level
	private static PrintWriter[] logout = null;
	
	// Path to the log files
	private static Path logDirPath = null;
	private static Path[] logPath = null;

	static {
		logPath = new Path[3];
		logout = new PrintWriter[ logPath.length];

		try {
			logDirPath = Files.createTempDirectory("GINsim-logs");

			logPath[0] = logDirPath.resolve("error.txt");
			logPath[1] = logDirPath.resolve("info.txt");
			logPath[2] = logDirPath.resolve("trace.txt");

			for (int i = 0; i < logPath.length; i++) {
				logout[i] = new PrintWriter(logPath[i].toFile());
			}
		} catch (IOException e) {
			System.err.println("Could not create log files");
		}
	}

	/**
	 * Set the verbose level of the logs
	 * 0 = log; 1= info; 2 = trace
	 * 
	 * @param verbose
	 */
	public static void setVerbose( int verbose){
		
		if( verbose >=0 && verbose <= 2){
			
			verboseLevel = verbose;
		}
		else{
			debug( "Incorrect value for verbose level : " + verboseLevel);
		}
	}
	
	/**
	 * Returns the verbose level actually set
	 * 0 = log; 1= info; 2 = trace
	 * 
	 * @return the verbose level actually set
	 */
	public static int getVerboseLevel() {
		
		return verboseLevel;
	}
	
	/**
	 * Log an error. The message is logged to the error log file according the verbose level
	 * 
	 * @param msg the message to log
	 */
	public static void error( Object msg){
		
		if( logout == null){
			return;
		}
		
		PrintWriter errorOut = logout[0];
		
		if( msg instanceof Throwable){
			errorOut.write( getPosition()+" Exception is :" + "\n");
			((Throwable) msg).printStackTrace( errorOut);
		}
		else{
			errorOut.write( getPosition()+msg.toString() + "\n");
		}
		errorOut.flush();
		
		if( debugMode){
			if( msg instanceof Throwable){
				((Throwable) msg).printStackTrace();
			}
			else{
				System.err.println( getPosition()+msg.toString());
			}
		}
	}
	
	/**
	 * Log an info. The message is logged to the info file according the verbose level
	 * 
	 * @param msg the message to log
	 */
	public static void info( Object msg){

		
		if( logout == null){
			return;
		}
		
		PrintWriter infoOut = logout[1];
		
		if( verboseLevel >= 1){
			infoOut.write( getPosition()+msg.toString() + "\n");
			infoOut.flush();
			if( debugMode){
				System.out.println( getPosition()+msg.toString());
			}
		}
	}
	
	/**
	 * Log a trace. The message is logged to the trace file according the verbose level
	 * 
	 * @param msg the message to log
	 */
	public static void trace( Object msg){
		
		LogManager.trace( msg, true);
	}
	
	/**
	 * Log a trace. The message is logged to the trace file only according the verbose level.
	 * If lineInfo is false, the information on the class name, method name and line number that issued the log
	 * are not traced to the file.
	 * 
	 * @param msg the message to log
	 * @param line_info indicates if the information on the class name, method name and line number must be traced
	 */
	public static void trace( Object msg, boolean line_info){

		if( logout == null){
			return;
		}
		
		PrintWriter traceOut = logout[2];
		
		if( verboseLevel >= 2){
			if( !line_info){
				traceOut.write( msg.toString());
				traceOut.flush();
				if( debugMode){
					System.out.print( msg);
				}
			}
			else{
				traceOut.write( getPosition() + msg + "\n");
				traceOut.flush();
				if( debugMode){
					System.out.println( getPosition() + msg.toString());
				}
			}

		}
		
	}
	
	/**
	 * Produce a zip file containing the log files and returns the path to the file
	 * 
	 * @return the path to the logs zip file
	 */
	public static Path deliverLogs(){
		
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		try {
		    // Create the ZIP file
		    Path zipPath = logDirPath.resolve("logs.zip");
		    ZipOutputStream out = new ZipOutputStream( new FileOutputStream(zipPath.toFile()));

		    // Compress the files
		    for( int i = 0; i < logPath.length; i++) {
		    	try{
			        FileInputStream in = new FileInputStream( logPath[i].toFile());
	
			        // Add ZIP entry to output stream.
			        out.putNextEntry( new ZipEntry( logPath[i].getFileName().toString() ));
	
			        // Transfer bytes from the file to the ZIP file
			        int len;
			        while ((len = in.read(buf)) > 0) {
			            out.write(buf, 0, len);
			        }
	
			        // Complete the entry
			        out.closeEntry();
			        in.close();
		    	}
		    	catch( FileNotFoundException fnfe){
		    		// One of the log file is not found: it will not be added to the zip file
		    	}
		    }

		    // Complete the ZIP file
		    out.close();
		    
			return zipPath;
			
		} catch (IOException e) {
			LogManager.error( "Unable to provide log zip file");
			LogManager.error( e);
			return null;
		}
		
	}
	
	// *********************************************************************************
	// **************************   DEBUGGING METHODS **********************************
	// *********************************************************************************
	
	
	/**
	 * print the log in {@link System}.out
	 */
	public static void debug() {
		
		debugOut.println(getPosition());
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(Object msg) {
		debugOut.println(getPosition()+msg);
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line if  mask match the debug mask
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(int mask, Object msg) {
		if ((debugmask & mask) != 0) debug(msg);	
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(int msg) {
		debugOut.println(getPosition()+msg);
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line if  mask match the debug mask
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(int mask, int msg) {
		if ((debugmask & mask) != 0) debug(msg);	
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(boolean msg) {
		debugOut.println(getPosition()+msg);
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line if  mask match the debug mask
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(int mask, boolean msg) {
		if ((debugmask & mask) != 0) debug(msg);	
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(double msg) {
		debugOut.println(getPosition()+msg);
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line if  mask match the debug mask
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(int mask, double msg) {
		if ((debugmask & mask) != 0) debug(msg);	
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(long msg) {
		debugOut.println(getPosition()+msg);
	}
	/**
	 * print the log in {@link System}.out and append msg to the end of the line if  mask match the debug mask
	 * @param msg the message to append at the end of the line
	 */
	public static void debug(int mask, long msg) {
		if ((debugmask & mask) != 0) debug(msg);	
	}

	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(Collection parents) {
		if (parents.size() == 0) {
			debugOut.println(getPosition()+"[]");
			return;
		}
		StringBuffer s = new StringBuffer("[");
		for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
			s.append(iterator.next()+", ");
		}
		debugOut.println(getPosition()+s.substring(0, s.length()-2)+"]");
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(int mask, Collection parents) {
		if ((debugmask & mask) != 0) debug_collection(parents);	
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(Object[] parents) {
		StringBuffer s = new StringBuffer("[");
		for (int i = 0; i < parents.length-1; i++) {
			s.append(parents[i]+", ");
		}
		s.append(parents[parents.length-1]+"]");
		debugOut.println(getPosition()+s);
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(int mask, Object[] parents) {
		if ((debugmask & mask) != 0) debug_collection(parents);	
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(int[] parents) {
		StringBuffer s = new StringBuffer("[");
		for (int i = 0; i < parents.length-1; i++) {
			s.append(parents[i]+",");
		}
		s.append(parents[parents.length-1]+"]");
		debugOut.println(getPosition()+s);
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(int mask, int[] parents) {
		if ((debugmask & mask) != 0) debug_collection(parents);	
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(double[] parents) {
		StringBuffer s = new StringBuffer("[");
		for (int i = 0; i < parents.length-1; i++) {
			s.append(parents[i]+",");
		}
		s.append(parents[parents.length-1]+"]");
		debugOut.println(getPosition()+s);
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(int mask, double[] parents) {
		if ((debugmask & mask) != 0) debug_collection(parents);	
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(boolean[] parents) {
		StringBuffer s = new StringBuffer("[");
		for (int i = 0; i < parents.length-1; i++) {
			s.append(parents[i]+", ");
		}
		s.append(parents[parents.length-1]+"]");
		debugOut.println(getPosition()+s);
	}
	/**
	 * print the log in {@link System}.out and append the content of the collection to the end of the line if mask match the debug mask
	 */
	public static void debug_collection(int mask, boolean[] parents) {
		if ((debugmask & mask) != 0) debug_collection(parents);	
	}
	
	/**
	 * print the last "levels" stack trace lines
	 */
	public static void printStackTrace() {
		printStackTrace(Thread.currentThread().getStackTrace().length);
	}
	
	public static void printStackTrace(int levels) {
		StringBuffer s = new StringBuffer("");
		for (int i = 3; i < levels; i++) {
			StackTraceElement st = Thread.currentThread().getStackTrace()[i];
			s.append(st.toString());
//		    s.append(st.getLineNumber());
//		    s.append(':');
//			String clname = st.getClassName();
//		    s.append(clname.substring(clname.lastIndexOf('.')+1));
//		    s.append('#');
//		    s.append(st.getMethodName());
		    s.append("()\n");
		}
		debugOut.println(s+"\n");
	}

	private static String getPosition() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace == null || trace.length < 4) {
			return "[UNKNOWN POSITION]:: ";
		}
		
		StackTraceElement pos = trace[3];
		
		return pos.getLineNumber() +": "+pos.getClassName() +"."+pos.getMethodName()+"():: ";
	}
	/**
	 * Give the mask of the log you want to display
	 * @param debug the debug to set
	 */
	public static void setDebug(int debug) {
		LogManager.debugmask = debug;
	}

	/**
	 * Define the outputStream. (<code>System.err</code> by default)
	 * @param out the outputStream to set
	 */
	public static void setOut(PrintStream out) {
		LogManager.debugOut = out;
	}
 }
