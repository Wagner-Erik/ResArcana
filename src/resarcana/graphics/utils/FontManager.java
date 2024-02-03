package resarcana.graphics.utils;

import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

import resarcana.math.Vector;

public class FontManager {

	private static FontManager instance;

	public static FontManager getInstance() {
		if (instance == null) {
			instance = new FontManager();
		}
		return instance;
	}

	private Font defaultFont, defaultFontNoAlias;

	private static final float FONT_ANTI_ALIAS_ZOOM = 4.f;

	private static final int MAX_FONT_SIZE_PRELOADED = 128;

	// Fonts
	private java.awt.Font baseFont;
	private UnicodeFont uniFont;

	private HashMap<Integer, UnicodeFont> fonts = new HashMap<Integer, UnicodeFont>();

	private FontManager() {
		// Load custom font
		try {
			this.baseFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream(
					ResourceManager.getInstance().normalizeIdentifier("fonts/Enchanted Land.otf")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void scheduleFonts() {
		Scheduler.getInstance().addMarker("Fonts");
		for (int i = 1; i < MAX_FONT_SIZE_PRELOADED; i++) {
			Scheduler.getInstance().scheduleFont(i);
		}
	}

	public void loadFont(int size) {
		this.getFont(size, true, true);
	}

	public Font getFont(int size) {
		return this.getFont(size, true, false);
	}

	@SuppressWarnings("unchecked")
	private Font getFont(int size, boolean antialias, boolean scheduled) {
		if (antialias) {
			size *= FONT_ANTI_ALIAS_ZOOM;
		}
		if (!this.fonts.containsKey(size)) {
			if (!scheduled) {
				Log.info("Loading font size " + size);
			}
			// Load custom font
			try {
				this.uniFont = new UnicodeFont(this.baseFont.deriveFont(java.awt.Font.PLAIN, size));
				this.uniFont.addAsciiGlyphs();
				this.uniFont.getEffects().add(new ColorEffect(java.awt.Color.white));
				this.uniFont.addAsciiGlyphs();
				this.uniFont.loadGlyphs();
				this.fonts.put(size, this.uniFont);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.fonts.get(size);
	}

	public Font getDefaultFont() {
		return this.defaultFont;
	}

	public Font getDefaultFontNoAlias() {
		return this.defaultFontNoAlias;
	}

	public void setDefaultFont(int size) {
		this.defaultFont = this.getFont(size, true, true);
		this.defaultFontNoAlias = this.getFont(size, false, true);
	}

	/**
	 * Zeichnet einen String
	 * 
	 * @param position Obere linke Ecke des Textes
	 */
	public void drawString(Graphics g, Vector position, String text) {
		position = position.mul(FONT_ANTI_ALIAS_ZOOM);
		g.scale(1 / FontManager.FONT_ANTI_ALIAS_ZOOM, 1 / FONT_ANTI_ALIAS_ZOOM);
		g.drawString(text, position.x, position.y);
		g.scale(FONT_ANTI_ALIAS_ZOOM, FONT_ANTI_ALIAS_ZOOM);
	}

	/**
	 * Zeichnet einen String mit Farbe color
	 * 
	 * @param position Obere linke Ecke des Textes
	 */
	public void drawString(Graphics g, Vector position, String text, Color color) {
		position = position.mul(FONT_ANTI_ALIAS_ZOOM);
		Color c = g.getColor();
		g.setColor(color);
		g.scale(1 / FONT_ANTI_ALIAS_ZOOM, 1 / FONT_ANTI_ALIAS_ZOOM);
		g.drawString(text, position.x, position.y);
		g.scale(FONT_ANTI_ALIAS_ZOOM, FONT_ANTI_ALIAS_ZOOM);
		g.setColor(c);
	}

	/**
	 * Zeichnet einen String zentriert auf der Position
	 * 
	 * @param position Center of String
	 */
	public void drawStringCentered(Graphics g, Vector position, String text) {
		if (!text.isEmpty()) {
			position = position.mul(FONT_ANTI_ALIAS_ZOOM);
			g.scale(1 / FONT_ANTI_ALIAS_ZOOM, 1 / FONT_ANTI_ALIAS_ZOOM);
			g.drawString(text, position.x - g.getFont().getWidth(text) / 2,
					position.y - g.getFont().getHeight(text) / 2);
			g.scale(FONT_ANTI_ALIAS_ZOOM, FONT_ANTI_ALIAS_ZOOM);
		}
	}

	/**
	 * Zeichnet einen String zentriert auf der Position mit Farbe color
	 * 
	 * @param position Center of String
	 */
	public void drawStringCentered(Graphics g, Vector position, String text, Color color) {
		if (!text.isEmpty()) {
			position = position.mul(FONT_ANTI_ALIAS_ZOOM);
			Color c = g.getColor();
			g.setColor(color);
			g.scale(1 / FONT_ANTI_ALIAS_ZOOM, 1 / FONT_ANTI_ALIAS_ZOOM);
			g.drawString(text, position.x - g.getFont().getWidth(text) / 2,
					position.y - g.getFont().getHeight(text) / 2);
			g.scale(FONT_ANTI_ALIAS_ZOOM, FONT_ANTI_ALIAS_ZOOM);
			g.setColor(c);
		}
	}

	public float getWidth(Font font, String text) {
		return font.getWidth(text) / FONT_ANTI_ALIAS_ZOOM;
	}

	public float getHeight(Font font, String text) {
		return font.getHeight(text) / FONT_ANTI_ALIAS_ZOOM;
	}

	public float getLineHeight(Font font) {
		return font.getLineHeight() / FONT_ANTI_ALIAS_ZOOM;
	}
}
