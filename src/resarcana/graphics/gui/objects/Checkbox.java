package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.ContentableObject;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.ThemesGUI;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

public class Checkbox extends ContentableObject implements Informable {

	public static final int MODE_DEFAULT = 0;
	public static final int MODE_YES_NO = 1;

	private static final float SIZE = Parameter.GUI_CHECKBOX_SIZE;

	private final int mode;
	private final Font font;

	private boolean value = false;
	private boolean writeable = true;

	private String yesText = "Yes", noText = "No";
	
	public Checkbox(InterfaceFunction function, boolean startValue) {
		this(function, Input.KEY_ENTER, startValue);
	}

	public Checkbox(InterfaceFunction function, int key, boolean startValue) {
		this(function, key, startValue, 1);
	}

	public Checkbox(InterfaceFunction function, int key, boolean startValue, float scale) {
		this(function, key, startValue, scale, MODE_DEFAULT);
	}

	public Checkbox(InterfaceFunction function, int key, boolean startValue, float scale, int mode) {
		super(function, key);
		this.value = startValue;
		this.font = FontManager.getInstance().getFont((int) (scale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.mode = mode;
		switch (this.mode) {
		case MODE_YES_NO:
			this.setHitbox(FontManager.getInstance().getWidth(this.font, "Yes"),
					FontManager.getInstance().getLineHeight(this.font));
			break;
		case MODE_DEFAULT:
		default:
			this.setHitbox(SIZE * scale, SIZE * scale);
			break;
		}
		this.addInformable(this);
	}

	public void setYesNoText(String yes, String no) {
		if (yes == null) {
			yes = "Yes";
		}
		if (no == null) {
			no = "No";
		}
		if (yes.isEmpty()) {
			yes = "Yes";
		}
		if (no.isEmpty()) {
			no = "No";
		}
		this.yesText = yes;
		this.noText = no;
		this.setHitbox(
				Math.max(FontManager.getInstance().getWidth(this.font, this.yesText),
						FontManager.getInstance().getWidth(this.font, this.noText)),
				Math.max(FontManager.getInstance().getHeight(this.font, this.yesText),
						FontManager.getInstance().getHeight(this.font, this.noText)));
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		GraphicUtils.translate(g, this.getCenter());
		switch (this.mode) {
		case MODE_YES_NO:
			g.setFont(this.font);
			Color c = ThemesGUI.getDefaultTheme().colorText;
			if (this.writeable) {
				switch (this.getStatus()) {
				case STATUS_MOUSE_OVER:
					c = ThemesGUI.getDefaultTheme().colorOver;
					break;
				case STATUS_LEFT_DOWN:
				case STATUS_LEFT_PRESSED:
					c = ThemesGUI.getDefaultTheme().colorDown;
					break;
				case STATUS_NOTHING:
				default:
					break;
				}
			}
			GraphicUtils.drawStringCentered(g, Vector.ZERO, this.value ? this.yesText : this.noText, c);
			break;
		case MODE_DEFAULT:
		default:
			if (this.value) {
				GraphicUtils.fill(g, this.getHitbox()
						.scale(this.getStatus() == Mousestatus.STATUS_LEFT_DOWN && this.writeable ? 0.9f : 1.0f));
			} else {
				GraphicUtils.draw(g, this.getHitbox()
						.scale(this.getStatus() == Mousestatus.STATUS_LEFT_DOWN && this.writeable ? 0.9f : 1.0f));
			}
			break;
		}
		g.popTransform();
	}

	@Override
	public String getContent() {
		return "" + this.value;
	}

	@Override
	public void setContent(String newContent) {
		this.value = Boolean.parseBoolean(newContent);
	}

	public boolean isChecked() {
		return this.value;
	}

	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object == this) {
			if (this.getStatus() == Mousestatus.STATUS_LEFT_RELEASED && this.writeable) {
				this.value = !this.value;
				this.informContentListeners();
			}
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}
}
