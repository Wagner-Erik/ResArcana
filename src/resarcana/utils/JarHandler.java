package resarcana.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import org.newdawn.slick.util.Log;

/**
 * Eine Klasse, die dazu dient, zu erkennen, ob das Programm aus einem
 * Jar-Archiv gestartet wurde
 * <p>
 * Der Name des Archivs muss manuell eingegeben werden
 * 
 * @author Erik Wagner
 * 
 */
public class JarHandler {

	private static String jarName = null;

	private static boolean exist, jChecked;
	private static JarFile jFile = null;

	/**
	 * @return <code>true</code>, wenn das Programm aus einem Jar-Archiv gestartet
	 *         wurde
	 */
	public static boolean existJar() {
		createJar();
		return exist;
	}

	private static void createJar() {
		if (!jChecked) {
			try {
				// Name des potentiellen Jar-Archivs ermitteln
				createJarName();
				jFile = new JarFile(jarName);
				exist = true;
				Log.info("Programm aus Jar-Archiv (" + jarName + ") gestartet");
			} catch (Exception e) {
				// Wird aufgerufen, wenn das Jar-Archiv nicht existiert
				// (Exception beim Erstellen von "JarFile")
				Log.info("Programm aus keinem Jar-Archiv gestartet");
				exist = false;
			}
			jChecked = true;
		}
	}

	private static void createJarName() {
		try {
			jarName = (new File(JarHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI()))
					.getName();
		} catch (URISyntaxException e) {
			Log.error("URISyntaxException in JarHandler");
		}
	}

	public static String getJarName() {
		if (jarName == null) {
			createJarName();
		}
		return jarName;
	}

	/**
	 * @return <code>null</code>, wenn das Jar-Archiv nicht existiert
	 */
	public static JarFile getJarFile() {
		createJar();
		return jFile;
	}
}
