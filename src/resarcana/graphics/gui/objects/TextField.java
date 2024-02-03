package resarcana.graphics.gui.objects;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.ContentableObject;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.SoundManager;
import resarcana.graphics.utils.Timer;
import resarcana.math.PointLine;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * Ein Textfeld für das Interface
 * 
 * @author Erik Wagner
 * 
 */
public class TextField extends ContentableObject {

	private static Color COLOR_BACKGROUND = Color.blue;
	private static Color COLOR_TEXT = Color.white;
	private static Color COLOR_BORDER = Color.white;

	private static final float PADDING_ENDS = 5.f;
	private static final float PADDING_CURSOR = 4.f;
	private static final float PADDING_HEIGHT = 3.f;

	private static final float DELAY_LENGTH = Parameter.GUI_TEXTFIELD_DELAY;
	private static final float BLINK_TIME = 0.5f;

	private final Font font;
	private final float scale;

	private Timer input_timer = new Timer(DELAY_LENGTH);
	private String content1 = "", content2 = "";
	private boolean writeable;

	private float blinkTime = BLINK_TIME;

	private String minimumText = "MMMMMMMMMM";

	public TextField(InterfaceFunction function, int key, boolean writeable, float scale) {
		super(function, key);
		this.writeable = writeable;
		this.scale = scale;
		this.font = FontManager.getInstance().getFont((int) (this.scale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.updateHitbox();
	}

	public TextField(InterfaceFunction function, int key, boolean writeable) {
		this(function, key, writeable, 1);
	}

	public TextField(InterfaceFunction function, int key) {
		this(function, key, true);
	}

	public TextField(InterfaceFunction function, boolean writeable) {
		this(function, 0, writeable);
	}

	public TextField(InterfaceFunction function) {
		this(function, true);
	}

	public void setWriteable(boolean status) {
		this.writeable = status;
	}

	private void updateHitbox() {
		this.setHitbox(new Rectangle(Vector.ZERO,
				Math.max(FontManager.getInstance().getWidth(this.font, this.getContent() + "|"),
						FontManager.getInstance().getWidth(this.font, this.minimumText) + 10) + 2 * PADDING_ENDS
						+ PADDING_CURSOR,
				FontManager.getInstance().getLineHeight(this.font) + 2 * PADDING_HEIGHT));
	}

	@Override
	public void draw(Graphics g) {
		g.setFont(this.font);

		Vector pos = this.getPosition();
		Vector center = this.getCenter();

		// Background
		GraphicUtils.fill(g, this.getHitbox().modifyCenter(center), COLOR_BACKGROUND);

		// First part of text
		GraphicUtils.drawString(g, pos.add(PADDING_ENDS, PADDING_HEIGHT), this.content1, COLOR_TEXT);

		// Cursor
		float xModifier = FontManager.getInstance().getWidth(this.font, this.content1);
		// Draw cursor if writeable and selected
		if (this.writeable && this.isSelected() && this.blinkTime < BLINK_TIME) {
			GraphicUtils.draw(g,
					new PointLine(pos.add(xModifier + PADDING_ENDS + PADDING_CURSOR / 2, PADDING_HEIGHT),
							pos.add(xModifier + PADDING_ENDS + PADDING_CURSOR / 2,
									Parameter.GUI_STANDARD_FONT_SIZE * this.scale + PADDING_HEIGHT)),
					COLOR_TEXT);
		}

		// Second part of text
		GraphicUtils.drawString(g, pos.add(xModifier + PADDING_ENDS + PADDING_CURSOR, PADDING_HEIGHT), this.content2,
				COLOR_TEXT);

		// Border
		g.setLineWidth(2);
		GraphicUtils.draw(g, this.getHitbox().modifyCenter(center), COLOR_BORDER);
	}

	@Override
	public String getContent() {
		return this.content1 + this.content2;
	}

	@Override
	public void setContent(String newContent) {
		if (newContent.length() > this.content1.length()) {
			this.content1 = newContent.substring(0, this.content1.length());
			this.content2 = newContent.substring(this.content1.length());
		} else {
			this.content1 = newContent;
			this.content2 = "";
		}
		this.updateHitbox();
	}

	@Override
	public void poll(Input input, float secounds) {
		super.poll(input, secounds);
		this.blinkTime += secounds;
		if (this.blinkTime > 2 * BLINK_TIME) {
			this.blinkTime = 0;
		}
		this.input_timer.poll(input, secounds);
		if (this.isSelected() && this.writeable) {
			if (input.isKeyDown(Input.KEY_BACK)) {
				if (!this.input_timer.isRunning()) {
					this.input_timer.start(DELAY_LENGTH);
					if (this.content1.length() >= 1) {
						this.changeContent(this.content1.substring(0, this.content1.length() - 1), this.content2);
					}
				}
			} else if (input.isKeyDown(Input.KEY_DELETE)) {
				if (!this.input_timer.isRunning()) {
					this.input_timer.start(DELAY_LENGTH);
					if (this.content2.length() >= 1) {
						this.changeContent(this.content1, this.content2.substring(1));
					}
				}
			} else if (input.isKeyDown(Input.KEY_LEFT)) {
				if (!this.input_timer.isRunning()) {
					this.input_timer.start(DELAY_LENGTH);
					if (this.content1.length() >= 1) {
						this.changeContent(this.content1.substring(0, this.content1.length() - 1),
								this.content1.charAt(this.content1.length() - 1) + this.content2);
					}
				}
			} else if (input.isKeyDown(Input.KEY_RIGHT)) {
				if (!this.input_timer.isRunning()) {
					this.input_timer.start(DELAY_LENGTH);
					if (this.content2.length() >= 1) {
						this.changeContent(this.content1 + this.content2.charAt(0), this.content2.substring(1));
					}
				}
			} else if (input.isKeyDown(Input.KEY_LCONTROL) || input.isKeyDown(Input.KEY_RCONTROL)) {
				if (input.isKeyPressed(Input.KEY_V)) {
					this.input_timer.start(DELAY_LENGTH);
					this.insertFromClipboard();
				}
			} else {
				for (int i = 0; i < 255; i++) {
					if (input.isKeyPressed(i)) {
						String keyName = Input.getKeyName(i).toLowerCase();
						if (keyName.length() == 1) {
							if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
								keyName = keyName.toUpperCase();
							}
							this.changeContent(this.content1 += keyName, this.content2);
						} else {
							if (keyName.equals("period")) {
								this.changeContent(this.content1 += ".", this.content2);
							}
							if (keyName.equals("minus")) {
								this.changeContent(this.content1 += "-", this.content2);
							}
							if (keyName.equals("comma")) {
								this.changeContent(this.content1 += ",", this.content2);
							}
							if (keyName.equals("space")) {
								this.changeContent(this.content1 += " ", this.content2);
							}
						}
					}
				}
			}
		}
	}

	private void insertFromClipboard() {
		Clipboard systemClipboard;
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferData = systemClipboard.getContents(null);
		for (DataFlavor dataFlavor : transferData.getTransferDataFlavors()) {
			Object content;
			try {
				content = transferData.getTransferData(dataFlavor);
				if (content instanceof String) {
					Log.info("Pasting string: " + content);
					this.changeContent((String) content, "");
					break;
				}
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void changeContent(String c1, String c2) {
		this.content1 = c1;
		this.content2 = c2;
		this.informContentListeners();
		this.updateHitbox();
		SoundManager.getInstance().playTypingSound();
	}

	public void setMinimumText(String string) {
		this.minimumText = string;
	}
}
