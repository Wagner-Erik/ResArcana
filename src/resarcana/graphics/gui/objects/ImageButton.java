package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.ImageDisplay;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.ScalableObject;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

public class ImageButton extends InterfaceObject implements ImageDisplay, ScalableObject {

	private final int key;

	private final String image;
	private String text;
	private final Font font;

	private Rectangle overlayHitbox = null;
	private String overlay = null;

	public ImageButton(InterfaceFunction function, Rectangle box, String image, int key, String text) {
		super(function, key);
		this.key = key;
		this.setHitbox(box);
		this.image = image;
		this.text = text;
		if (text.isEmpty()) {
			this.font = FontManager.getInstance().getDefaultFont();
		} else {
			float scale = box.height * 0.8f
					/ FontManager.getInstance().getHeight(FontManager.getInstance().getDefaultFont(), this.text);
			this.font = FontManager.getInstance().getFont((int) (scale * Parameter.GUI_STANDARD_FONT_SIZE));
		}
	}

	public ImageButton(InterfaceFunction function, Rectangle box, String image, int key) {
		this(function, box, image, key, "");
	}

	public ImageButton(InterfaceFunction function, Rectangle box, String image, String text) {
		this(function, box, image, Input.KEY_ENTER, text);
	}

	public ImageButton(InterfaceFunction function, Rectangle box, String image) {
		this(function, box, image, Input.KEY_ENTER, "");
	}

	public ImageButton(ImageButton button, float scale) {
		this(button.getFunction(), button.getHitbox().scale(scale), button.image, button.key, button.text);
		this.setBorder(button.hasBorder());
		this.overlay = button.overlay;
		if (button.overlayHitbox != null) {
			this.overlayHitbox = button.overlayHitbox.scaleWithCenter(scale);
		}
	}

	/**
	 * Sets a new text for this button
	 * <p>
	 * The font will <b>NOT</b> be updated by this
	 * 
	 * @param text the new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	public void setOverlay(Rectangle hitbox, String overlay) {
		if (hitbox == null || overlay == null) {
			hitbox = null;
			overlay = null;
		} else {
			this.overlayHitbox = hitbox;
			this.overlay = overlay;
		}
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		GraphicUtils.translate(g, this.getCenter());
		GraphicUtils.drawImage(g, this.getHitbox(), ResourceManager.getInstance().getImage(this.image));
		Color c = g.getColor();
		g.setColor(Color.white);
		if (this.overlay != null) {
			GraphicUtils.drawImage(g, this.overlayHitbox, ResourceManager.getInstance().getImage(this.overlay));
		}
		if (!this.text.isEmpty()) {
			g.setFont(this.font);
			GraphicUtils.drawStringCentered(g, Vector.ZERO, this.text);
		}
		this.drawBorder(g);
		g.setColor(c);
		g.popTransform();
	}

	@Override
	public String getImage() {
		return this.image;
	}

	@Override
	public ImageButton scale(float newScale) {
		return new ImageButton(this, newScale);
	}
}
