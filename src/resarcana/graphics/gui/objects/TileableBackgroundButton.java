package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;

import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * @author e.wagner
 * 
 */
public class TileableBackgroundButton extends InterfaceObject {

	private static final Vector OFFSET = new Vector(38, -5);
	private static final Rectangle SINGLE_HITBOX = new Rectangle(Vector.ZERO, 64, 64);
	private static final float TEXT_SCALE = 1.8f;

	private String text;
	private final float scale;
	private final int overwriteSegments;
	private final boolean leftEnd, rightEnd;

	private int numberMiddleSegments = 0;

	private Font font;

	private static String ImageLeft, ImageMiddle, ImageRight;

	/**
	 * Erzeugt ein neues InterfaceLabel
	 * 
	 * @param function Die InterfaceFunction des Buttons
	 * @param text     Der Text des Labels
	 * @param scale    Die Größe des Textes
	 * 
	 */
	public TileableBackgroundButton(InterfaceFunction function, String text, float scale, int numberMiddleSegments,
			boolean leftEnd, boolean rightEnd) {
		super(function);
		this.scale = scale;
		this.overwriteSegments = numberMiddleSegments;
		this.leftEnd = leftEnd;
		this.rightEnd = rightEnd;
		this.font = FontManager.getInstance()
				.getFont((int) (this.scale * TEXT_SCALE * Parameter.GUI_STANDARD_FONT_SIZE));
		this.setText(text);
		if (ImageLeft == null) {
			ImageLeft = "interface-icons/TextBackground_left_2_short.png";
			ImageMiddle = "interface-icons/TextBackground_middle_2.png";
			ImageRight = "interface-icons/TextBackground_right_2_short.png";
		}
	}

	public TileableBackgroundButton(InterfaceFunction function, String text, float scale, int numberMiddleSegments) {
		this(function, text, scale, numberMiddleSegments, true, true);
	}

	public TileableBackgroundButton(InterfaceFunction function, String text, float scale, boolean leftEnd,
			boolean rightEnd) {
		this(function, text, scale, -1, leftEnd, rightEnd);
	}

	public TileableBackgroundButton(InterfaceFunction function, String text, float scale) {
		this(function, text, scale, -1);
	}

	public TileableBackgroundButton(String text, float scale) {
		this(InterfaceFunctions.INTERFACE_LABEL, text, scale);
	}

	private int endSegments() {
		if (this.leftEnd) {
			if (this.rightEnd) {
				return 2;
			}
			return 1;
		}
		if (this.rightEnd) {
			return 1;
		}
		return 0;
	}

	private void recalculateHitbox() {
		if (this.overwriteSegments < 0) {
			this.numberMiddleSegments = Math.max(
					(int) Math.ceil((FontManager.getInstance().getWidth(this.font, text) + OFFSET.x * 2 * this.scale)
							/ (SINGLE_HITBOX.width * this.scale)) - this.endSegments(),
					0);
		} else {
			this.numberMiddleSegments = this.overwriteSegments;
		}
		this.setHitbox(new Rectangle(Vector.ZERO,
				SINGLE_HITBOX.width * (this.numberMiddleSegments + this.endSegments()) * this.scale,
				SINGLE_HITBOX.height * this.scale));
	}

	@Override
	public void draw(Graphics g) {
		g.setFont(this.font);
		Vector pos = this.getPosition();
		float x = pos.x + SINGLE_HITBOX.width * this.scale / 2;
		float y = pos.y + SINGLE_HITBOX.height * this.scale / 2;
		if (this.leftEnd) {
			GraphicUtils.drawImage(g, SINGLE_HITBOX.modifyCenter(x, y).scale(this.scale),
					ResourceManager.getInstance().getImage(ImageLeft));
		}
		x += SINGLE_HITBOX.width * this.scale;
		for (int i = 0; i < this.numberMiddleSegments; i++) {
			GraphicUtils.drawImage(g, SINGLE_HITBOX.modifyCenter(x, y).scale(this.scale),
					i % 2 == 0 ? ResourceManager.getInstance().getImage(ImageMiddle)
							: ResourceManager.getInstance().getRevertedImage(ImageMiddle));
			x += SINGLE_HITBOX.width * this.scale;
		}
		if (this.rightEnd) {
			GraphicUtils.drawImage(g, SINGLE_HITBOX.modifyCenter(x, y).scale(this.scale),
					this.numberMiddleSegments % 2 == 1 ? ResourceManager.getInstance().getImage(ImageRight)
							: ResourceManager.getInstance().getRevertedImage(ImageLeft));
			x += SINGLE_HITBOX.width * this.scale / 2;
		}

		Color c = g.getColor();
		g.setColor(Color.black);
		if (this.getFunction() != InterfaceFunctions.INTERFACE_LABEL) {
			switch (this.getStatus()) {
			case STATUS_MOUSE_OVER:
				g.setColor(Color.yellow);
				break;
			case STATUS_LEFT_DOWN:
			case STATUS_LEFT_PRESSED:
				g.setColor(Color.cyan);
				break;
			case STATUS_NOTHING:
			default:
				break;
			}
		}
		GraphicUtils.drawStringCentered(g, new Vector((pos.x + x) / 2, y + OFFSET.y * this.scale), this.text);
		g.setColor(c);
	}

	/**
	 * Setzt den Text des Labels neu
	 * 
	 * @param text Der neue Text
	 */
	public void setText(String text) {
		this.text = text;
		this.recalculateHitbox();
	}
}
