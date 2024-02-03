package resarcana.graphics.gui.objects;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * @author e.wagner
 * 
 */
public class TextArea extends InterfaceObject {

	private ArrayList<String> text;

	private float scale;

	/**
	 * Die verwendete Schriftart
	 */
	private final Font font;

	/**
	 * The textcolor of the label
	 */
	private final Color color;

	private final int lines;
	private final float width;

	private int offset;

	/**
	 * Erzeugt ein neues InterfaceLabel
	 * 
	 * @param text  Der Text des Labels
	 * @param scale Die Größe des Textes
	 * 
	 */
	public TextArea(String width, int lines, float scale, Color color) {
		super(InterfaceFunctions.INTERFACE_LABEL);
		this.lines = lines;
		this.offset = 0;
		this.text = new ArrayList<String>();
		this.scale = scale;
		this.color = color;
		this.font = FontManager.getInstance().getFont((int) (this.scale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.width = FontManager.getInstance().getWidth(this.font, width + "...");
		this.calculateHitbox();
	}

	private void calculateHitbox() {
		this.setHitbox(new Rectangle(Vector.ZERO, this.width,
				FontManager.getInstance().getLineHeight(this.font) * this.lines));
	}

	/**
	 * Setzt den Text des Labels neu
	 * 
	 * @param text Der neue Text
	 */
	public void addLine(String line) {
		String insert = line;
		while (FontManager.getInstance().getWidth(this.font, insert) > this.width) {
			insert = insert.substring(0, insert.length() - 1);
		}
		if (!insert.equals(line)) {
			insert = insert.substring(0, insert.length() - 2) + " ...";
		}
		this.text.add(insert);
		Log.debug("Added: " + line);
		this.calculateHitbox();
	}

	@Override
	public void draw(Graphics g) {
		g.setFont(this.font);
		g.setColor(this.color);
		Vector pos = this.getPosition();
		for (int i = this.offset; i < this.text.size() && i < offset + this.lines; i++) {
			GraphicUtils.drawString(g, pos, this.text.get(i));
			pos = pos.add(0, FontManager.getInstance().getLineHeight(this.font));
		}
	}

	private void setOffset(int newOffset) {
		this.offset = Math.max(0, Math.min(this.text.size() - this.lines, newOffset));
	}

	public void scrollFullDown() {
		this.setOffset(this.text.size() - this.lines);
	}

	public void scrollDown() {
		this.setOffset(this.offset + 1);
	}

	public void scrollUp() {
		this.setOffset(this.offset - 1);
	}
}
