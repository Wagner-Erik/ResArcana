package resarcana.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import resarcana.communication.ServerLog;
import resarcana.game.utils.factory.ArtifactFactory;
import resarcana.game.utils.factory.MageFactory;
import resarcana.game.utils.factory.MagicItemFactory;
import resarcana.game.utils.factory.MonumentFactory;
import resarcana.game.utils.factory.PowerPlaceFactory;
import resarcana.game.utils.factory.ScrollFactory;
import resarcana.graphics.Engine;
import resarcana.graphics.SlickEngine;
import resarcana.graphics.utils.GraphicsLogSystem;
import resarcana.utils.ForkingPrintStream;
import resarcana.utils.JarHandler;
import resarcana.utils.Parameter;
import resarcana.utils.UtilFunctions;

public class Launcher {

	private static int Width = 1600;
	private static int Height = 900;
	private static boolean Fullscreen = false, Verbose = true;
	private static int targetFPS = Parameter.GAME_FPS_TARGET;

	public static boolean preloadFonts = false, preloadImages = true;
	public static String defaultName = "", defaultServer = "heidegaming.de";

	private static HashMap<String, String> config = new HashMap<String, String>();

	private static void putIntoConfig(String identifier, String value) {
		config.put(identifier.trim().toLowerCase(), value.trim());
	}

	private static String retrieveFromConfig(String identifier) {
		return config.get(identifier.trim().toLowerCase());
	}

	private static void loadConfig() {
		// Set default values
		putIntoConfig("directory", PROGRAMM_DIRECTORY_MAIN);
		putIntoConfig("width", "" + Width);
		putIntoConfig("height", "" + Height);
		putIntoConfig("fullscreen", "" + Fullscreen);
		putIntoConfig("targetFPS", "" + targetFPS);

		putIntoConfig("defaultName", "");
		putIntoConfig("defaultServer", "heidegaming.de");

		putIntoConfig("verbose", "" + Verbose);
		putIntoConfig("preloadFonts", "" + preloadFonts);
		putIntoConfig("preloadImages", "" + preloadImages);

		putIntoConfig("artifacts", "all");
		putIntoConfig("places", "all");
		putIntoConfig("monuments", "all");
		putIntoConfig("scrolls", "all");
		putIntoConfig("mages", "all");
		putIntoConfig("items", "all");

		boolean noException = true;

		Path file = Paths.get("./config.txt");
		Log.info("Loading config from \"./config.txt\"");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file)))) {
			String line = null;
			String[] split;
			while ((line = reader.readLine()) != null) {
				split = line.split("=");
				if (split.length == 2) {
					putIntoConfig(split[0], split[1]);
					Log.info(line);
				}
			}
			reader.close();
		} catch (IOException e) {
			Log.error("Error while reading config-file, proceeding with default values", e);
			noException = false;
		}
		if (noException) {
			if (!("automatic".equalsIgnoreCase(retrieveFromConfig("directory")))) {
				PROGRAMM_DIRECTORY_MAIN = retrieveFromConfig("directory");
			}

			try {
				Width = Integer.parseInt(retrieveFromConfig("width"));
			} catch (Exception e) {
				// Ignore and continue with default value
			}
			try {
				Height = Integer.parseInt(retrieveFromConfig("height"));
			} catch (Exception e) {
				// Ignore and continue with default value
			}
			Fullscreen = Boolean.parseBoolean(retrieveFromConfig("fullscreen"));
			Verbose = Boolean.parseBoolean(retrieveFromConfig("verbose"));
			try {
				targetFPS = Math.max(-1, Integer.parseInt(retrieveFromConfig("targetFPS")));
				if (targetFPS > -1) {
					targetFPS = Math.max(targetFPS, Parameter.GAME_FPS_MINIMUM);
				}
			} catch (Exception e) {
				// Ignore and continue with default value
			}

			preloadFonts = Boolean.parseBoolean(retrieveFromConfig("preloadFonts"));
			preloadImages = Boolean.parseBoolean(retrieveFromConfig("preloadImages"));

			defaultName = retrieveFromConfig("defaultName");
			defaultServer = retrieveFromConfig("defaultServer");

			int[] split;
			if (!("all".equalsIgnoreCase(retrieveFromConfig("artifacts")))) {
				split = UtilFunctions.parseIntAll(retrieveFromConfig("artifacts").split(" "));
				if (split.length == 1) {
					ArtifactFactory.setArtifactsUsed(UtilFunctions.getRange(0, split[0]));
				} else if (split.length == 2) {
					ArtifactFactory.setArtifactsUsed(UtilFunctions.getRange(split[0], split[1]));
				} else {
					ArtifactFactory.setArtifactsUsed(split);
				}
			}
			if (!("all".equalsIgnoreCase(retrieveFromConfig("places")))) {
				split = UtilFunctions.parseIntAll(retrieveFromConfig("places").split(" "));

				if (split.length == 1) {
					PowerPlaceFactory.setPlacesUsed(UtilFunctions.getRange(0, split[0]));
				} else if (split.length == 2) {
					PowerPlaceFactory.setPlacesUsed(UtilFunctions.getRange(split[0], split[1]));
				} else {
					PowerPlaceFactory.setPlacesUsed(split);
				}
			}
			if (!("all".equalsIgnoreCase(retrieveFromConfig("monuments")))) {
				split = UtilFunctions.parseIntAll(retrieveFromConfig("monuments").split(" "));
				if (split.length == 1) {
					MonumentFactory.setMonumentsUsed(UtilFunctions.getRange(0, split[0]));
				} else if (split.length == 2) {
					MonumentFactory.setMonumentsUsed(UtilFunctions.getRange(split[0], split[1]));
				} else {
					MonumentFactory.setMonumentsUsed(split);
				}
			}
			if (!("all".equalsIgnoreCase(retrieveFromConfig("mages")))) {
				split = UtilFunctions.parseIntAll(retrieveFromConfig("mages").split(" "));
				if (split.length == 1) {
					MageFactory.setMagesUsed(UtilFunctions.getRange(0, split[0]));
				} else if (split.length == 2) {
					MageFactory.setMagesUsed(UtilFunctions.getRange(split[0], split[1]));
				} else {
					MageFactory.setMagesUsed(split);
				}
			}
			if (!("all".equalsIgnoreCase(retrieveFromConfig("items")))) {
				split = UtilFunctions.parseIntAll(retrieveFromConfig("items").split(" "));
				if (split.length == 1) {
					MagicItemFactory.setItemsUsed(UtilFunctions.getRange(0, split[0]));
				} else if (split.length == 2) {
					MagicItemFactory.setItemsUsed(UtilFunctions.getRange(split[0], split[1]));
				} else {
					MagicItemFactory.setItemsUsed(split);
				}
			}
			if (!("all".equalsIgnoreCase(retrieveFromConfig("scrolls")))) {
				split = UtilFunctions.parseIntAll(retrieveFromConfig("scrolls").split(" "));
				if (split.length == 1) {
					ScrollFactory.setScrollsUsed(UtilFunctions.getRange(0, split[0]));
				} else if (split.length == 2) {
					ScrollFactory.setScrollsUsed(UtilFunctions.getRange(split[0], split[1]));
				} else {
					ScrollFactory.setScrollsUsed(split);
				}
			}
			Log.info("Config loaded successfully");
		}
	}

	public static void main(String[] args) {

		// Mark server output in logging
		ServerLog.prefix = "SERVER: ";
		// Create Logging system
		Log.setLogSystem(new GraphicsLogSystem());

		if (JarHandler.existJar()) {
			// Redirect Log and ServerLog to a file and keep console output
			try {
				File logs = new File("./logs/");
				logs.mkdirs();
				String logfile = "./logs/" + FILE_IDENTIFIER + ".log";
				PrintStream log = new ForkingPrintStream(new PrintStream(logfile), System.out);
				ServerLog.out = log;
				GraphicsLogSystem.out = log;
				Log.info("Forked log into " + logfile);
			} catch (FileNotFoundException e1) {
				Log.error("Could not open Log-file...defaulting to System.out only", e1);
				ServerLog.out = System.out;
				GraphicsLogSystem.out = System.out;
			}
			loadConfig();
			File stats = new File("./stats/");
			stats.mkdirs();
			File x = new File(getResourceDirectory());
			x.mkdirs();
			Log.info("Benutztes Resourcen-Verzeichnis: " + getResourceDirectory());
			Log.info("Verbose logging: " + Verbose);
			Log.setVerbose(Verbose);
		}
		// The rendering engine
		SlickEngine engine = Engine.getInstance();
		// Settings for the engine
		engine.setTargetFrameRate(targetFPS);
		engine.setAlwaysRender(true);
		engine.setClearEachFrame(false);
		engine.setUpdateOnlyWhenVisible(false);
		engine.setTitle("Res Arcana");
		// Construct the game and start it
		engine.switchState(new GameState());
		try {
			engine.setDisplayMode(Width, Height, Fullscreen);
			engine.start();
		} catch (SlickException e1) {
			Log.error("SlickException in game loop!", e1);
			throw new Error(e1);
		}
	}

	public static final long FILE_IDENTIFIER = System.currentTimeMillis();

	/**
	 * Der gesamte Pfad zu dem Ordner, in dem die resarcana-Dateien gespeichert
	 * werden sollen
	 * <p>
	 * Dieser Ordner befindet sich immer im "user.home"-Verzeichnis
	 * 
	 * @see java.lang.System#getProperty
	 */
	private static String PROGRAMM_DIRECTORY_MAIN = "./resources/";

	public static String getResourceDirectory() {
		return PROGRAMM_DIRECTORY_MAIN;
	}

}
