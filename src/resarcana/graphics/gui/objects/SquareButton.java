/**
 *
 */
package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.ThemesGUI;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Shape;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * @author Erik Wagner
 * 
 */
public class SquareButton extends InterfaceObject {

	public static final float BUTTON_DIMENSION = Parameter.GUI_BUTTON_DIMENSION;

	private final String icon;

	/**
	 * Erzeigt einen neuen Button für das Interface
	 * 
	 * @param function Die Funktion des Buttons (Ein Enum aus
	 *                 {@link InterfaceFunctions});
	 * @param icon     Der Datei-Pfad des Icons, welches auf diesem Button
	 *                 dargestellt werden soll
	 */
	public SquareButton(InterfaceFunction function, int key, String icon) {
		super(function, key);
		this.icon = icon;
		this.setHitbox(new Rectangle(Vector.ZERO, BUTTON_DIMENSION, BUTTON_DIMENSION));
	}

	public SquareButton(InterfaceFunction function, String icon) {
		this(function, Input.KEY_ENTER, icon);
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		GraphicUtils.translate(g, this.getCenter());
		Shape actShape = this.getHitbox();
		GraphicUtils.drawImage(g, actShape, ResourceManager.getInstance().getImage(this.icon));
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
		GraphicUtils.draw(g, actShape);
		g.setColor(c);
		g.popTransform();
	}
}
