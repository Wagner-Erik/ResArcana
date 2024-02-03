package resarcana.communication;

import java.io.PrintStream;
import java.util.Date;

/**
 * Copied from org.newdawn.slick.util.DefaultLogSystem
 * 
 * @author Erik
 *
 */
public class ServerLog {
	/** The output stream for dumping the log out on */
	public static PrintStream out = System.out;

	public static String prefix = "";

	/**
	 * Log an error
	 * 
	 * @param message The message describing the error
	 * @param e       The exception causing the error
	 */
	public static void error(String message, Throwable e) {
		error(message);
		error(e);
	}

	/**
	 * Log an error
	 * 
	 * @param e The exception causing the error
	 */
	public static void error(Throwable e) {
		out.println(new Date() + " ERROR:" + prefix + e.getMessage());
		e.printStackTrace(out);
	}

	/**
	 * Log an error
	 * 
	 * @param message The message describing the error
	 */
	public static void error(String message) {
		out.println(new Date() + " ERROR:" + prefix + message);
	}

	/**
	 * Log a warning
	 * 
	 * @param message The message describing the warning
	 */
	public static void warn(String message) {
		out.println(new Date() + " WARN:" + prefix + message);
	}

	/**
	 * Log an information message
	 * 
	 * @param message The message describing the infomation
	 */
	public static void info(String message) {
		out.println(new Date() + " INFO:" + prefix + message);
	}

	/**
	 * Log a debug message
	 * 
	 * @param message The message describing the debug
	 */
	public static void debug(String message) {
		out.println(new Date() + " DEBUG:" + prefix + message);
	}

	/**
	 * Log a warning with an exception that caused it
	 * 
	 * @param message The message describing the warning
	 * @param e       The cause of the warning
	 */
	public static void warn(String message, Throwable e) {
		warn(message);
		e.printStackTrace(out);
	}
}
