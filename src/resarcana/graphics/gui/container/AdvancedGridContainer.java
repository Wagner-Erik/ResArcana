/**
 *
 */
package resarcana.graphics.gui.container;

import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfacePart;
import resarcana.math.Shape;
import resarcana.math.Vector;

/**
 * Ein GridContainer unterteilt die ihm zugeteilte Fläche in gleich große
 * Rechtecke. Objekte können in verschiedenen Modi angeordnet werden (
 * {@link #MODUS_DEFAULT}, {@link #MODUS_X_LEFT} oder {@link #MODUS_X_RIGHT},
 * {@link #MODUS_Y_DOWN} oder {@link #MODUS_Y_UP}).
 * 
 * @author Erik Wagner
 * 
 */
public class AdvancedGridContainer extends InterfaceContainer {

	/**
	 * Die Objekte werden so platziert, dass die Mitte (in x-Richtung oder
	 * y-Richtung) der Mitte der entprechenden Zelle entspricht
	 */
	public static final int MODUS_DEFAULT = 0;

	/**
	 * Die Objekte werden so platziert, dass sie an der rechten Zellenseite anliegen
	 */
	public static final int MODUS_X_RIGHT = 1;

	/**
	 * Die Objekte werden so platziert, dass sie an der linken Zellenseite anliegen
	 */
	public static final int MODUS_X_LEFT = 2;

	/**
	 * Die Objekte werden so platziert, dass sie an der unteren Zellenseite anliegen
	 */
	public static final int MODUS_Y_DOWN = 3;

	/**
	 * Die Objekte werden so platziert, dass sie an der oberen Zellenseite anliegen
	 */
	public static final int MODUS_Y_UP = 4;

	private static final float PADDING_DEFAULT = 10.0f;

	private final int defaultModusX;
	private final int defaultModusY;
	private final int rows;
	private final int cols;
	private int[][] modiX;
	private int[][] modiY;

	private float[] maxCellWidth, maxCellHeight;
	private Vector[][] positions;

	private float paddingX, paddingY;

	/**
	 * Erzeugt einen neuen GridContainer, der seine Fläche in gleich große Zellen
	 * unterteilt, in denen die Objekte gemäß der Modi angeordnet werden
	 * 
	 * @param rows   Die Anzahl der Reihen Gitters
	 * @param cols   Die Anzahl der Spalten des Gittes
	 * @param modusX Der Default-Modus für die X-Anordnung ({@link #MODUS_DEFAULT},
	 *               {@link #MODUS_X_LEFT} oder {@link #MODUS_X_RIGHT})
	 * @param modusY Der Default-Modus für die Y-Anordnung ({@link #MODUS_DEFAULT},
	 *               {@link #MODUS_Y_DOWN} oder {@link #MODUS_Y_UP})
	 */
	public AdvancedGridContainer(int rows, int cols, int modusX, int modusY, float paddingX, float paddingY) {
		super();
		this.rows = rows;
		this.cols = cols;
		this.defaultModusX = modusX;
		this.defaultModusY = modusY;
		this.modiX = new int[cols][rows];
		this.modiY = new int[cols][rows];
		this.maxCellWidth = new float[cols];
		this.maxCellHeight = new float[rows];
		this.paddingX = paddingX;
		this.paddingY = paddingY;
		this.positions = new Vector[cols][rows];
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				this.modiX[i][j] = modusX;
				this.modiY[i][j] = modusY;
			}
		}
		this.triggerResize();
	}

	/**
	 * Erzeugt einen neuen GridContainer mit {@code modusX} = {@link #MODUS_DEFAULT}
	 * und {@code modusY} = {@link #MODUS_DEFAULT}
	 * 
	 * @see #GridContainer(int, int, int, int)
	 */
	public AdvancedGridContainer(int rows, int cols) {
		this(rows, cols, MODUS_DEFAULT, MODUS_DEFAULT, PADDING_DEFAULT, PADDING_DEFAULT);
	}

	/**
	 * Fügt dem GridManager ein InterfacePart in einer bestimmten Zelle mit den
	 * Default-Modi hinzu
	 * 
	 * @see #add(InterfacePart, int, int, int, int)
	 * 
	 * @param adding Der hinzuzufügen Objekt
	 * @param row    Die Reihe der Zelle, die dieses Objekt als Grundlage nehmen
	 *               soll (beginnend mit 0)
	 * @param col    Die Spalte der Zelle, die dieses Objekt als Grundlage nehmen
	 *               soll (beginnend mit 0)
	 */
	public void add(InterfacePart adding, int row, int col) {
		this.add(adding, row, col, this.defaultModusX, this.defaultModusY);
	}

	/**
	 * Fügt dem GridManager ein InterfacePart in einer bestimmten Zelle mit neuen
	 * Modi hinzu
	 * 
	 * @param adding Der hinzuzufügen Objekt
	 * @param row    Die Reihe der Zelle, die dieses Objekt als Grundlage nehmen
	 *               soll (beginnend mit 0)
	 * @param col    Die Spalte der Zelle, die dieses Objekt als Grundlage nehmen
	 *               soll (beginnend mit 0)
	 * @param modusX Der Modus in x-Richtung für die Zelle, in die das Objekt
	 *               geaddet wird
	 * @param modusY Der Modus in y-Richtung für die Zelle, in die das Objekt
	 *               geaddet wird
	 */
	public void add(InterfacePart adding, int row, int col, int modusX, int modusY) {
		this.modiX[col][row] = modusX;
		this.modiY[col][row] = modusY;
		super.add(adding, new Vector(col, row));
	}

	@Override
	public Vector getPositionFor(InterfacePart object) {
		if (this.objects.containsKey(object)) {
			Vector cell = this.objects.get(object);
			Shape shape = object.getHitbox();
			int x = (int) cell.x;
			int y = (int) cell.y;
			return this.positions[x][y]
					.add(this.getXPosInCell(x, y, shape.getXRange()), this.getYPosInCell(x, y, shape.getYRange()))
					.add(this.getPosition());
		} else {
			return null;
		}
	}

	/**
	 * Liefert die x-Koordinate für das Objekt in der Zelle gemäß dem eingestellen
	 * x-Modus
	 */
	private float getXPosInCell(int col, int row, float objectWidth) {
		switch (this.modiX[col][row]) {
		case MODUS_X_LEFT:
			return this.paddingX / 2;
		case MODUS_X_RIGHT:
			return this.maxCellWidth[col] - objectWidth - this.paddingX / 2;
		case MODUS_DEFAULT:
		default:
			return (this.maxCellWidth[col] - objectWidth) / 2;
		}
	}

	/**
	 * Liefert die y-Koordinate für das Objekt in der Zelle gemäß dem eingestellen
	 * y-Modus
	 */
	private float getYPosInCell(int col, int row, float objectHeight) {
		switch (this.modiY[col][row]) {
		case MODUS_Y_UP:
			return this.paddingY / 2;
		case MODUS_Y_DOWN:
			return this.maxCellHeight[row] - objectHeight - this.paddingY / 2;
		case MODUS_DEFAULT:
		default:
			return (this.maxCellHeight[row] - objectHeight) / 2;
		}
	}

	@Override
	protected boolean resize() {
		// Reset dimensions
		for (int i = 0; i < this.cols; i++) {
			this.maxCellWidth[i] = 0;
		}
		for (int i = 0; i < this.rows; i++) {
			this.maxCellHeight[i] = 0;
		}

		// Setup basic padding
		float[][] cellWidth = new float[cols][rows];
		float[][] cellHeight = new float[cols][rows];
		for (int i = 0; i < this.cols; i++) {
			for (int j = 0; j < this.rows; j++) {
				cellWidth[i][j] = this.paddingX;
				cellHeight[i][j] = this.paddingY;
			}
		}

		// Loop all objects and enlarge the containing cells
		for (InterfacePart part : this.keys) {
			Shape size = part.getHitbox();
			int x = (int) this.objects.get(part).x;
			int y = (int) this.objects.get(part).y;
			cellWidth[x][y] = Math.max(cellWidth[x][y], size.getXRange() + this.paddingX);
			cellHeight[x][y] = Math.max(cellHeight[x][y], size.getYRange() + this.paddingY);
		}

		// Get maximum width/height of each column/row
		for (int i = 0; i < this.cols; i++) {
			for (int j = 0; j < this.rows; j++) {
				this.maxCellWidth[i] = Math.max(this.maxCellWidth[i], cellWidth[i][j]);
				this.maxCellHeight[j] = Math.max(this.maxCellHeight[j], cellHeight[i][j]);
			}
		}

		// Caculate positions of corners of the cell grid
		Vector pos = Vector.ZERO, posCol = Vector.ZERO;
		for (int i = 0; i < this.cols; i++) {
			pos = posCol;
			for (int j = 0; j < this.rows; j++) {
				this.positions[i][j] = pos;
				pos = pos.add(0, this.maxCellHeight[j]);
			}
			posCol = posCol.add(this.maxCellWidth[i], 0);
		}

		// Total width and height
		float width = 0, height = 0;
		for (int i = 0; i < this.cols; i++) {
			width += this.maxCellWidth[i];
		}
		for (int i = 0; i < this.rows; i++) {
			height += this.maxCellHeight[i];
		}

		// Register hitbox
		return this.setHitbox(width, height);
	}
}
