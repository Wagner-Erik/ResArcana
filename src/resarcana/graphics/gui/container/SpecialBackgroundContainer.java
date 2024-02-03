package resarcana.graphics.gui.container;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.ThemesGUI;
import resarcana.graphics.gui.ThemesGUI.Position;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class SpecialBackgroundContainer extends InterfaceContainer {

	private static final Rectangle PADDING = new Rectangle(0, 0, 32, 32);
	private static final float OVERLAP = 2.f;

	private final boolean top, bottom, left, right;
	private final float paddingWidth, paddingHeight;
	private final Rectangle padding;
	private final ThemesGUI theme;

	private InterfaceContainer contents;

	public SpecialBackgroundContainer(InterfaceContainer contents, ThemesGUI theme, boolean top, boolean bottom,
			boolean left, boolean right, float scale) {
		this.contents = contents;
		this.theme = theme;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		this.padding = PADDING.scale(scale);
		float w = 0, h = 0;
		if (this.top) {
			h += this.padding.height;
		}
		if (this.left) {
			w += this.padding.width;
		}
		this.add(this.contents, new Vector(w, h));
		if (this.bottom) {
			h += this.padding.height;
		}
		if (this.right) {
			w += this.padding.width;
		}
		this.paddingWidth = w;
		this.paddingHeight = h;
		this.triggerResize();
	}

	public SpecialBackgroundContainer(InterfaceContainer contents, boolean top, boolean bottom, boolean left,
			boolean right, float scale) {
		this(contents, ThemesGUI.getDefaultTheme(), top, bottom, left, right, scale);
	}

	public void setContents(InterfaceContainer contents) {
		this.remove(this.contents);
		this.contents = contents;
		if (this.contents != null) {
			this.add(this.contents, new Vector(this.left ? this.padding.width : 0, this.top ? this.padding.height : 0));
		}
	}

	@Override
	public void draw(Graphics g) {
		// Do not draw without contents
		if (this.contents != null) {
			Vector pos = this.getPosition();
			pos = pos.add(this.left ? 0 : -this.padding.width, this.top ? 0 : -this.padding.height);
			float width = this.contents.getHitbox().width;
			float height = this.contents.getHitbox().height;
			if (this.top) {
				if (this.left) {
					this.theme.drawBackgroundImage(g, Position.TOP_LEFT,
							new Rectangle(pos.x, pos.y, this.padding.width, this.padding.height + OVERLAP));
				}
				this.theme.drawBackgroundImage(g, Position.TOP_MIDDLE,
						new Rectangle(pos.x + this.padding.width, pos.y, width, this.padding.height + OVERLAP));
				if (this.right) {
					this.theme.drawBackgroundImage(g, Position.TOP_RIGHT,
							new Rectangle(pos.x + width + this.padding.width, pos.y, this.padding.width,
									this.padding.height + OVERLAP));
				}
			}
			if (this.left) {
				this.theme.drawBackgroundImage(g, Position.LEFT_MIDDLE, new Rectangle(pos.x,
						pos.y + this.padding.height - OVERLAP, this.padding.width, height + 2 * OVERLAP));
			}
			this.theme.drawBackgroundImage(g, Position.MIDDLE, new Rectangle(pos.x + this.padding.width,
					pos.y + this.padding.height - OVERLAP, width, height + 2 * OVERLAP));
			if (this.right) {
				this.theme.drawBackgroundImage(g, Position.RIGHT_MIDDLE,
						new Rectangle(pos.x + width + this.padding.width, pos.y + this.padding.height - OVERLAP,
								this.padding.width, height + 2 * OVERLAP));
			}
			if (this.bottom) {
				if (this.left) {
					this.theme.drawBackgroundImage(g, Position.BOTTOM_LEFT,
							new Rectangle(pos.x, pos.y + height + this.padding.height - OVERLAP, this.padding.width,
									this.padding.height + OVERLAP));
				}
				this.theme.drawBackgroundImage(g, Position.BOTTOM_MIDDLE, new Rectangle(pos.x + this.padding.width,
						pos.y + height + this.padding.height - OVERLAP, width, this.padding.height + OVERLAP));
				if (this.right) {
					this.theme.drawBackgroundImage(g, Position.BOTTOM_RIGHT,
							new Rectangle(pos.x + width + this.padding.width,
									pos.y + height + this.padding.height - OVERLAP, this.padding.width,
									this.padding.height + OVERLAP));
				}
			}
//			if (this.top) {
//				if (this.left) {
//					GraphicUtils.drawImage(g,
//							new Rectangle(pos.x, pos.y, this.padding.width, this.padding.height + OVERLAP),
//							this.theme.getImage(Position.TOP_LEFT));
//				}
//				GraphicUtils.drawImage(g,
//						new Rectangle(pos.x + this.padding.width, pos.y, width, this.padding.height + OVERLAP),
//						this.theme.getImage(Position.TOP_MIDDLE));
//				if (this.right) {
//					GraphicUtils.drawImage(g, new Rectangle(pos.x + width + this.padding.width, pos.y,
//							this.padding.width, this.padding.height + OVERLAP),
//							this.theme.getImage(Position.TOP_RIGHT));
//				}
//			}
//			if (this.left) {
//				GraphicUtils.drawImage(g, new Rectangle(pos.x, pos.y + this.padding.height - OVERLAP,
//						this.padding.width, height + 2 * OVERLAP), this.theme.getImage(Position.LEFT_MIDDLE));
//			}
//			GraphicUtils.drawImage(g, new Rectangle(pos.x + this.padding.width, pos.y + this.padding.height - OVERLAP,
//					width, height + 2 * OVERLAP), this.theme.getImage(Position.MIDDLE));
//			if (this.right) {
//				GraphicUtils
//						.drawImage(g,
//								new Rectangle(pos.x + width + this.padding.width, pos.y + this.padding.height - OVERLAP,
//										this.padding.width, height + 2 * OVERLAP),
//								this.theme.getImage(Position.RIGHT_MIDDLE));
//			}
//			if (this.bottom) {
//				if (this.left) {
//					GraphicUtils
//							.drawImage(g,
//									new Rectangle(pos.x, pos.y + height + this.padding.height - OVERLAP,
//											this.padding.width, this.padding.height + OVERLAP),
//									this.theme.getImage(Position.BOTTOM_LEFT));
//				}
//				GraphicUtils.drawImage(g, new Rectangle(pos.x + this.padding.width,
//						pos.y + height + this.padding.height - OVERLAP, width, this.padding.height + OVERLAP),
//						this.theme.getImage(Position.BOTTOM_MIDDLE));
//				if (this.right) {
//					GraphicUtils.drawImage(g,
//							new Rectangle(pos.x + width + this.padding.width,
//									pos.y + height + this.padding.height - OVERLAP, this.padding.width,
//									this.padding.height + OVERLAP),
//							this.theme.getImage(Position.BOTTOM_RIGHT));
//				}
//			}
			// Draw contents
			super.draw(g);
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.contents != null) { // Not shown when empty
			super.poll(input, secounds);
		}
	}

	@Override
	public void setBackgroundState(boolean state) {
		super.setBackgroundState(false); // Disable default background
	}

	@Override
	public boolean canBlockMouse() {
		return this.contents != null;
	}

	@Override
	protected boolean resize() {
		if (this.contents != null) {
			return this.setHitbox(this.contents.getHitbox().width + this.paddingWidth,
					this.contents.getHitbox().height + this.paddingHeight);
		} else {
			return this.setHitbox(this.paddingWidth, this.paddingHeight);
		}
	}
}
