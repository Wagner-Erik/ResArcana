package resarcana.graphics.gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Rectangle;

public enum ThemesGUI {

	SIMPLE_THEME("interface/theme/default/special_background_full.png", 1024, 1024, 256, Color.white, Color.cyan,
			Color.yellow),

	SCROLL_THEME("interface/theme/scroll/background_scroll_full.png", 444, 570, 64, Color.black, Color.cyan,
			Color.yellow);

	public enum Position {
		TOP_LEFT, TOP_MIDDLE, TOP_RIGHT, LEFT_MIDDLE, MIDDLE, RIGHT_MIDDLE, BOTTOM_LEFT, BOTTOM_MIDDLE, BOTTOM_RIGHT,
		FULL
	};

	private static ThemesGUI defaultTheme = SIMPLE_THEME;

	public static ThemesGUI getDefaultTheme() {
		return defaultTheme;
	}

	public static void setDefaultTheme(ThemesGUI theme) {
		if (theme != null) {
			defaultTheme = theme;
		}
	}

	public final float imagePadding, imageWidth, imageHeight;
	public final String image;
	public final Color colorText, colorDown, colorOver;

	private ThemesGUI(String image, float imageWidth, float imageHeight, float imagePadding, Color text, Color down,
			Color over) {
		this.image = image;
		Scheduler.getInstance().scheduleResource(this.image);
		this.imagePadding = imagePadding;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.colorText = text;
		this.colorDown = down;
		this.colorOver = over;
	}

	public void drawBackgroundImage(Graphics g, Position pos, Rectangle box) {
		float srcx = 0, srcx2 = this.imagePadding;
		// x-ranges
		switch (pos) {
		case TOP_LEFT:
		case LEFT_MIDDLE:
		case BOTTOM_LEFT:
			break;
		case TOP_MIDDLE:
		case MIDDLE:
		case BOTTOM_MIDDLE:
			srcx = this.imagePadding;
			srcx2 = this.imageWidth - this.imagePadding;
			break;
		case TOP_RIGHT:
		case RIGHT_MIDDLE:
		case BOTTOM_RIGHT:
			srcx = this.imageWidth - this.imagePadding;
			srcx2 = this.imageWidth;
			break;
		case FULL:
			srcx2 = this.imageWidth;
			break;
		default:
			break;
		}
		// y-ranges
		float srcy = 0, srcy2 = this.imagePadding;
		switch (pos) {
		case TOP_LEFT:
		case TOP_MIDDLE:
		case TOP_RIGHT:
			break;
		case LEFT_MIDDLE:
		case MIDDLE:
		case RIGHT_MIDDLE:
			srcy = this.imagePadding;
			srcy2 = this.imageHeight - this.imagePadding;
			break;
		case BOTTOM_LEFT:
		case BOTTOM_MIDDLE:
		case BOTTOM_RIGHT:
			srcy = this.imageHeight - this.imagePadding;
			srcy2 = this.imageHeight;
			break;
		case FULL:
			srcy2 = this.imageHeight;
			break;
		default:
			break;
		}
		// draw some subsection of the full image filling the requested box
		g.drawImage(ResourceManager.getInstance().getImage(this.image), box.x, box.y, box.x + box.width,
				box.y + box.height, srcx, srcy, srcx2, srcy2);
	}
}
