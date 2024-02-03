package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;

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
public class Label extends InterfaceObject {

	private String text;

	private float scale;

	/**
	 * Die verwendete Schriftart
	 */
	private final Font font;

	/**
	 * The textcolor of the label
	 */
	private final Color color;

	/**
	 * Erzeugt ein neues InterfaceLabel
	 * 
	 * @param text  Der Text des Labels
	 * @param scale Die Größe des Textes
	 * 
	 */
	public Label(String text, float scale, Color color) {
		super(InterfaceFunctions.INTERFACE_LABEL);
		this.scale = scale;
		this.color = color;
		this.font = FontManager.getInstance().getFont((int) (this.scale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.setText(text);
	}

	/**
	 * Setzt den Text des Labels neu
	 * 
	 * @param text Der neue Text
	 */
	public void setText(String text) {
		this.text = text;
		this.setHitbox(new Rectangle(Vector.ZERO, FontManager.getInstance().getWidth(this.font, text),
				FontManager.getInstance().getHeight(this.font, this.text)));
	}

	@Override
	public void draw(Graphics g) {
		g.setFont(this.font);
		g.setColor(this.color);
		GraphicUtils.drawString(g, this.getPosition(), this.text);
	}
}
