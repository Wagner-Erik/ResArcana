package resarcana.graphics.gui;

import org.newdawn.slick.Graphics;

import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;

/**
 * Die Hauptklasse für das User-Interface.
 * <p>
 * Es wird ein MainContainer gesetzt, in diesen können andere
 * {@link InterfacePart} eingefügt werden.
 * <p>
 * poll() und draw() werden für das MainGUI aufgerufen und an die anderen
 * Objekte weitergegeben.
 * <p>
 * draw(): es wird unabhängig von vorherigen Translationen/Sklierungen etc.
 * gearbeitet, sodass das Interface immer gleich aussieht.
 * 
 * @author Erik Wagner
 * 
 */
public class MainGUI extends InterfaceContainer {

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("GUI");
		// Load images for various interface elements
		Scheduler.getInstance().scheduleResource("interface-icons/back-arrow.png");
		Scheduler.getInstance().scheduleResource("interface-icons/forth-arrow.png");
		Scheduler.getInstance().scheduleResource("interface-icons/delete.png");
		Scheduler.getInstance().scheduleResource("interface-icons/TextBackground_left_2_short.png");
		Scheduler.getInstance().scheduleResource("interface-icons/TextBackground_middle_2.png");
		Scheduler.getInstance().scheduleResource("interface-icons/TextBackground_right_2_short.png");
		Scheduler.getInstance().scheduleResource("interface-icons/interface_dialog_background.png");
		Scheduler.getInstance().scheduleResource("interface-icons/slider.png");
		Scheduler.getInstance().scheduleResource("interface-icons/TextBackground_down.png");
	}

	private InterfaceContainer mainContainer;

	private final Interfaceable interfaceable;

	/**
	 * @param parent
	 */
	public MainGUI(Interfaceable parent) {
		super();
		this.interfaceable = parent;
	}

	@Override
	public Interfaceable getInterfaceable() {
		return this.interfaceable;
	}

	public void setMainContainer(InterfaceContainer con) {
		this.remove(mainContainer);
		this.add(con, Vector.ZERO);
		this.mainContainer = con;
	}

	public InterfaceContainer getMainContainer() {
		return this.mainContainer;
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		g.resetTransform();
		super.draw(g);
		g.popTransform();
	}

	@Override
	protected boolean resize() {
		return this.setHitbox(this.getInterfaceable().getWidth(), this.getInterfaceable().getHeight());
	}

	@Override
	public void triggerResize() {
		super.triggerResize();
		this.updatePosition();
	}
}
