package resarcana.graphics.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.opengl.InternalTextureLoader;
import org.newdawn.slick.util.Log;

import resarcana.game.Launcher;
import resarcana.graphics.AdvancedImage;
import resarcana.graphics.ResourceError;
import resarcana.utils.JarHandler;

/**
 * Der ResourceManager lädt Ressourcen wie Bilder oder Sounds vom Dateisystem.
 * Wenn mehrmals nach einer Ressource gefragt wird, kann sie jederzeit aus dem
 * Cache geholt werden.
 * 
 */
public class ResourceManager {

	/**
	 * @return Der von der gesamten Anwendung geteilte ResourceManager.
	 */
	public static ResourceManager getInstance() {
		if (instance == null) {
			instance = new ResourceManager();
		}
		return instance;
	}

	private static ResourceManager instance;

	private Map<String, AdvancedImage> images = Collections.synchronizedMap(new HashMap<String, AdvancedImage>());

	private Map<String, Sound> sounds = Collections.synchronizedMap(new HashMap<String, Sound>());

	private final AdvancedImage imageNotFound;
	private final Sound soundNotFound;

	private ResourceManager() {
		AdvancedImage image = null;
		Sound sound = null;
		try {
			image = new AdvancedImage(new Image("image_not_found.png"));
		} catch (SlickException e) {
			Log.error("Could not load: \"image_not_found.png\" as default image", e);
		}
		this.imageNotFound = image;
		try {
			sound = new Sound("sound_not_found.ogg");
		} catch (SlickException e) {
			Log.error("Could not load: \"sound_not_found.ogg\" as default sound", e);
		}
		this.soundNotFound = sound;
	}

	public String normalizeIdentifier(String identifier) {
		if (JarHandler.existJar()) {
			if (!identifier.startsWith(Launcher.getResourceDirectory())) {
				return Launcher.getResourceDirectory() + identifier;
			} else {
				return identifier;
			}
		} else {
			if (!identifier.startsWith("resources/")) {
				return "resources/" + identifier;
			} else {
				Log.warn("Resource identifiers should not start with \"resources/\" (" + identifier + ")");
				return identifier;
			}
		}
	}

	private void load(String identifier) {
		// Detection when resources are used but not scheduled, only active in non-jar execution
		if (!JarHandler.existJar() && !Scheduler.getInstance().hasItemsScheduled()) {
			Log.debug("Loading unscheduled resource: " + identifier);
			new ResourceError("Loading unscheduled resource: " + identifier).printStackTrace();
		}
		if (identifier.endsWith(".png")) { // Bild laden
			try {
				this.images.put(identifier, new AdvancedImage(new Image(identifier)));
			} catch (Throwable e) {
				Log.error("Could not load image: " + identifier, e);
				this.images.put(identifier, this.imageNotFound);
			}
		} else if (identifier.endsWith(".ogg")) { // Sound laden
			try {
				this.sounds.put(identifier, new Sound(identifier));
			} catch (Throwable e) {
				Log.error("Could not load sound: " + identifier, e);
				this.sounds.put(identifier, this.soundNotFound);
			}
		} else { // Ressourcentyp unbekannt
			Log.error("Error by \"" + identifier + "\"");
			throw new ResourceError("Unkown ressource type: " + identifier);
		}
	}

	/**
	 * Läd die gewünschte Bildressource vom Dateisystem oder aus dem Cache.
	 * 
	 * @param id Pfad zur Bildressource.
	 * @return Das Bild.
	 */
	public AdvancedImage getImage(String id) {
		id = normalizeIdentifier(id);

		AdvancedImage image = this.images.get(id);
		if (image == null) {
			load(id);
			return this.images.get(id);
		} else {
			return image;
		}
	}

	/**
	 * Erzeugt ein Bild das in X-Richtung umgedreht wurde oder holt es aus dem
	 * Cache.
	 * 
	 * @param id Pfad zur Bildressource.
	 * @return Das umgedrehte Bild.
	 */
	public AdvancedImage getRevertedImage(String id) {
		id = normalizeIdentifier(id);

		AdvancedImage image = this.images.get(id + "?reverse");
		if (image == null) {
			if (JarHandler.existJar()) {
				image = new AdvancedImage(this.getImage(id).getFlippedCopy(true, false));
			} else {
				image = new AdvancedImage(this.getImage(id.substring(id.indexOf('/') + 1)).getFlippedCopy(true, false));
			}
			this.images.put(id + "?reverse", image);
		}
		return image;
	}

	/**
	 * Erzeugt ein Bild das um ANGLE im Uhrzeigersinn gedreht wurde
	 * 
	 * @param id    Pfad zur Bildressource.
	 * @param angle Der Winkel in Grad um den das Bild gedreht werden soll
	 * @return Das gedrehte Bild
	 */
	public AdvancedImage getRotatedImage(String id, float angle) {
		id = normalizeIdentifier(id);

		AdvancedImage image = this.images.get(id + "?r" + angle);
		if (image == null) {
			if (JarHandler.existJar()) {
				image = new AdvancedImage(this.getImage(id));
			} else {
				image = new AdvancedImage(this.getImage(id.substring(id.indexOf('/') + 1)));
			}
			image.setRotation(angle);
			this.images.put(id + "?r" + angle, image);
			Log.info("Rotated image " + id + " at " + angle);
		}
		return image;
	}

	/**
	 * Läd eine Sounddatei vom Dateisystem oder aus dem Cache.
	 * 
	 * @param id Pfad zur Soundresource.
	 * @return Die Sounddatei.
	 */
	public Sound getSound(String id) {
		id = normalizeIdentifier(id);

		Sound sound = this.sounds.get(id);
		if (sound == null) {
			load(id);
			return this.sounds.get(id);
		} else {
			return sound;
		}
	}

	public void loadScheduled(String identifier) {
		this.load(this.normalizeIdentifier(identifier));
	}

	public void reloadImages() {
		Log.info("Reloading images");
		ArrayList<String> resources = new ArrayList<String>(this.images.keySet());
		int i = 0;
		while (i < resources.size()) {
			// Don't reschedule reversed and rotated images
			if (resources.get(i).contains("?reverse") || resources.get(i).contains("?r")) {
				resources.remove(i);
			} else {
				i++;
			}
		}
		Scheduler.getInstance().addMarker("Reloading images");
		Scheduler.getInstance().scheduleAllResources(resources);
		Scheduler.getInstance().resetScheduleCounter();
		this.images.clear();
		InternalTextureLoader.get().clear();
	}

	public Image getDefaultImage() {
		return this.images.get("image_not_found.png");
	}
}
