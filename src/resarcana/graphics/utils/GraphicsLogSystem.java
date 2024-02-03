package resarcana.graphics.utils;

import java.io.PrintStream;
import java.util.Date;

import org.newdawn.slick.util.DefaultLogSystem;
import org.newdawn.slick.util.LogSystem;

import resarcana.game.utils.LogBox;

/**
 * An enhanced copy of the {@link DefaultLogSystem} to be able to display
 * information in game additional to on console/fileoutput
 * 
 */
public class GraphicsLogSystem implements LogSystem {
	/** The output stream for dumping the log out on */
	public static PrintStream out = System.out;

	public static LogBox errorLogBox = null;

	private void logInBox(String message) {
		if (errorLogBox != null) {
			errorLogBox.addLog(message);
			errorLogBox.show();
		}
	}

	/**
	 * Log an error
	 * 
	 * @param message The message describing the error
	 * @param e       The exception causing the error
	 */
	@Override
	public void error(String message, Throwable e) {
		error(message);
		error(e);
	}

	/**
	 * Log an error
	 * 
	 * @param e The exception causing the error
	 */
	@Override
	public void error(Throwable e) {
		error(e.getMessage());
		e.printStackTrace(out);
	}

	/**
	 * Log an error
	 * 
	 * @param message The message describing the error
	 */
	@Override
	public void error(String message) {
		out.println(new Date() + " ERROR:" + message);
		logInBox("ERROR:" + message);
	}

	/**
	 * Log a warning
	 * 
	 * @param message The message describing the warning
	 */
	@Override
	public void warn(String message) {
		out.println(new Date() + " WARN:" + message);
		logInBox("WARN:" + message);
	}

	/**
	 * Log an information message
	 * 
	 * @param message The message describing the infomation
	 */
	@Override
	public void info(String message) {
		out.println(new Date() + " INFO:" + message);
	}

	/**
	 * Log a debug message
	 * 
	 * @param message The message describing the debug
	 */
	@Override
	public void debug(String message) {
		out.println(new Date() + " DEBUG:" + message);
	}

	/**
	 * Log a warning with an exception that caused it
	 * 
	 * @param message The message describing the warning
	 * @param e       The cause of the warning
	 */
	@Override
	public void warn(String message, Throwable e) {
		warn(message);
		e.printStackTrace(out);
	}
}
