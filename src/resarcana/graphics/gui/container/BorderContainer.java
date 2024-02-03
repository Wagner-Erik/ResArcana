package resarcana.graphics.gui.container;

import resarcana.graphics.gui.InterfacePart;

/**
 * Ein BorderContainer ordnet die Objekte an bestimmten Stellen an (Ecken,
 * Mitten der Kanten, Mitte der gesamten Fläche). Dabei schiebt er die Objekte
 * jeweils an den Rand sofern dieser direkt an die Stelle angrenzt, ansonsten
 * wird das Objekt in der jeweiligen Richtung in der Mitte platziert.
 * 
 * @author Erik Wagner
 * 
 */
public class BorderContainer extends GridContainer {

	public final static int POSITION_MIDDLE = 1;
	public final static int POSITION_HIGH_LEFT = 2;
	public final static int POSITION_HIGH = 3;
	public final static int POSITION_HIGH_RIGHT = 4;
	public final static int POSITION_RIGHT = 5;
	public final static int POSITION_LOW_RIGHT = 6;
	public final static int POSITION_LOW = 7;
	public final static int POSITION_LOW_LEFT = 8;
	public final static int POSITION_LEFT = 9;

	/**
	 * Erzeugt einen neuen BorderContainer
	 */
	public BorderContainer() {
		super(3, 3);
	}

	/**
	 * Fügt ein InterfacePart an der entsprechenden Position hinzu
	 * 
	 * @param adding   Das hinzuzufügende InterfacePart
	 * @param position Die Position an der das Objekt hinzugefügt werden soll (eine
	 *                 der Konstanten aus {@link BorderContainer})
	 */
	public void add(InterfacePart adding, int position) {
		switch (position) {
		case POSITION_HIGH:
			this.add(adding, 0, 1, GridContainer.MODUS_DEFAULT, GridContainer.MODUS_Y_UP);
			break;
		case POSITION_HIGH_LEFT:
			this.add(adding, 0, 0, GridContainer.MODUS_X_LEFT, GridContainer.MODUS_Y_UP);
			break;
		case POSITION_HIGH_RIGHT:
			this.add(adding, 0, 2, GridContainer.MODUS_X_RIGHT, GridContainer.MODUS_Y_UP);
			break;
		case POSITION_MIDDLE:
			this.add(adding, 1, 1, GridContainer.MODUS_DEFAULT, GridContainer.MODUS_DEFAULT);
			break;
		case POSITION_LEFT:
			this.add(adding, 1, 0, GridContainer.MODUS_X_LEFT, GridContainer.MODUS_DEFAULT);
			break;
		case POSITION_RIGHT:
			this.add(adding, 1, 2, GridContainer.MODUS_X_RIGHT, GridContainer.MODUS_DEFAULT);
			break;
		case POSITION_LOW:
			this.add(adding, 2, 1, GridContainer.MODUS_DEFAULT, GridContainer.MODUS_Y_DOWN);
			break;
		case POSITION_LOW_LEFT:
			this.add(adding, 2, 0, GridContainer.MODUS_X_LEFT, GridContainer.MODUS_Y_DOWN);
			break;
		case POSITION_LOW_RIGHT:
			this.add(adding, 2, 2, GridContainer.MODUS_X_RIGHT, GridContainer.MODUS_Y_DOWN);
			break;
		default:
			break;
		}
	}
}
