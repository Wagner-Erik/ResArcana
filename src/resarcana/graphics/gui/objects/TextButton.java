package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.ThemesGUI;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * Ein Button, der aus Text besteht
 * 
 * @author Erik Wagner
 * 
 */
public class TextButton extends InterfaceObject {

	private static final float BUTTON_PADDING_WIDTH = 10;
	private static final float BUTTON_PADDING_HEIGHT = 10;
	/**
	 * size stellt die Texthöhe aller Textbuttons dar.
	 * 
	 */
	private float scale = 1;
	private String buttonText;
	private Font font;

	/**
	 * Erzeugt einen TextButton für das Interface
	 * 
	 * @param function   Die {@link InterfaceFunction}
	 * @param buttonText Der Text des Button
	 */
	public TextButton(InterfaceFunction function, String buttonText) {
		this(function, Input.KEY_ENTER, buttonText);
	}

	public TextButton(InterfaceFunction function, String buttonText, float scale) {
		this(function, Input.KEY_ENTER, buttonText, scale);
	}

	public TextButton(InterfaceFunction function, int key, String buttonText) {
		this(function, key, buttonText, 1);
	}

	public TextButton(InterfaceFunction function, int key, String buttonText, float scale) {
		super(function, key);
		this.scale = scale;
		this.font = FontManager.getInstance().getFont((int) (this.scale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.setText(buttonText);
	}

	public String getText() {
		return this.buttonText;
	}

	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		switch (this.getStatus()) {
		case STATUS_MOUSE_OVER:
			g.setColor(ThemesGUI.getDefaultTheme().colorOver);
			break;
		case STATUS_LEFT_DOWN:
		case STATUS_LEFT_PRESSED:
			g.setColor(ThemesGUI.getDefaultTheme().colorDown);
			break;
		case STATUS_NOTHING:
		default:
			g.setColor(ThemesGUI.getDefaultTheme().colorText);
			break;
		}
		g.setFont(this.font);
		GraphicUtils.drawStringCentered(g, this.getCenter(), this.buttonText);
		g.setColor(c);
	}

	public void setText(String text) {
		this.buttonText = text;
		this.setHitbox(new Rectangle(Vector.ZERO,
				FontManager.getInstance().getWidth(this.font, this.buttonText) + BUTTON_PADDING_WIDTH,
				FontManager.getInstance().getHeight(this.font, this.buttonText) + BUTTON_PADDING_HEIGHT));
	}
}
