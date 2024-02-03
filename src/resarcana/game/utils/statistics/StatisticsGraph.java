package resarcana.game.utils.statistics;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.Log;

import javafx.util.Pair;
import resarcana.game.core.Game;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * A graphical display for statistical information about a {@link Game}
 * 
 * @author Erik
 *
 */
public class StatisticsGraph extends InterfaceObject {

	private static final Color BACKGROUND_COLOR = Color.darkGray;
	private static final Color FONT_COLOR = Color.white;
	private static final Color[] LINE_COLORS = new Color[] { Color.red, Color.blue, Color.yellow, Color.green };

	private final Font font;
	private final int maxLabels;

	private String[] names;
	private int[][] data;
	private int minimum = 0, maximum = 1;
	private ArrayList<Pair<Vector, String>> labels = new ArrayList<Pair<Vector, String>>();
	private ArrayList<Integer> roundsMarker = new ArrayList<Integer>();

	/**
	 * Create a new graph to display statistical information
	 * 
	 * @param box       the box used for drawing the graph (its position is
	 *                  <b>not</b> used)
	 * @param players   names for all players to include in the graph
	 * @param fontScale the relative scale of the font to use (relative to
	 *                  {@link Parameter#GUI_STANDARD_FONT_SIZE})
	 */
	public StatisticsGraph(Rectangle box, String[] players, float fontScale) {
		super(InterfaceFunctions.NONE);
		this.font = FontManager.getInstance().getFont((int) (fontScale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.setHitbox(box.addToWidthAndHeight(FontManager.getInstance().getWidth(this.font, "100"),
				FontManager.getInstance().getLineHeight(this.font) * 1.5f));
		this.maxLabels = (int) Math
				.ceil(this.getHitbox().height / FontManager.getInstance().getLineHeight(this.font) / 2);
		this.data = new int[players.length][0];
		this.names = players.clone();
	}

	/**
	 * Add a round-marker at the currently last datapoint position
	 */
	public void addRoundMarker() {
		this.roundsMarker.add(this.data[0].length);
	}

	/**
	 * Reset all round-marker
	 */
	public void resetRoundMarkers() {
		this.roundsMarker.clear();
	}

	/**
	 * Set the data for a player
	 * 
	 * @param player the id of the player
	 * @param data   the data-set for the player
	 */
	public void setData(int player, int[] data) {
		if (player < this.data.length && player >= 0) {
			this.data[player] = data;
			this.calculateRange(player);
			this.calculateLabels();
		} else {
			Log.warn("Can't set data for player " + player + " out of " + this.data.length);
		}
	}

	/**
	 * Recalculate all display ranges based on all datapoints
	 */
	public void resetRanges() {
		this.minimum = 0;
		this.maximum = 1;
		for (int i = 0; i < this.data.length; i++) {
			this.calculateRange(i);
		}
		this.calculateLabels();
	}

	/**
	 * Recalculate the display ranges based on the values of a player
	 * 
	 * @param player id of the player
	 */
	private void calculateRange(int player) {
		for (int j = 0; j < this.data[player].length; j++) {
			if (this.data[player][j] > this.maximum) {
				this.maximum = this.data[player][j];
			} else if (data[player][j] < this.minimum) {
				this.minimum = this.data[player][j];
			}
		}
	}

	/**
	 * Recalculate the labels for the graph
	 */
	private void calculateLabels() {
		this.labels.clear();
		int numLabels = Math.max(2, Math.min(this.maxLabels, this.maximum - this.minimum + 1)) - 1;
		float lh = FontManager.getInstance().getLineHeight(this.font);
		float y0 = this.getHitbox().height - 2 * lh, y1 = 0;
		float x0 = 2;
		float label = this.minimum, range = this.maximum - this.minimum;
		boolean roundDown = false;
		this.labels.add(new Pair<Vector, String>(new Vector(x0, y0), "" + this.minimum));
		for (int i = 1; i < numLabels; i++) {
			label = i * range / numLabels + this.minimum;
			label = (float) (roundDown ? Math.floor(label) : Math.ceil(label));
			roundDown = !roundDown;
			this.labels.add(new Pair<Vector, String>(new Vector(x0, y0 + (label - this.minimum) / range * (y1 - y0)),
					"" + (int) label));
		}
		this.labels.add(new Pair<Vector, String>(new Vector(x0, y1), "" + this.maximum));
	}

	@Override
	public void draw(Graphics g) {
		g.setFont(this.font);
		Color c = g.getColor();
		g.pushTransform();
		GraphicUtils.translate(g, this.getPosition());

		// Draw background
		g.setColor(BACKGROUND_COLOR);
		float padding = 2;
		g.fillRect(-padding, -padding, this.getHitbox().width + 2 * padding, this.getHitbox().height + 2 * padding);

		// Draw axes and labels
		g.setColor(FONT_COLOR);
		g.setLineWidth(3);
		float x0 = FontManager.getInstance().getWidth(this.font, "100"),
				y0 = this.getHitbox().height - FontManager.getInstance().getLineHeight(this.font) * 1.5f; // axes
																											// positions
		float rx = this.getHitbox().width - x0,
				ry = FontManager.getInstance().getLineHeight(this.font) * 2.0f - this.getHitbox().height; // axes length
		g.drawLine(x0, y0, x0 + rx, y0);
		g.drawLine(x0, y0, x0, y0 + ry);
		for (int i = 0; i < this.labels.size(); i++) {
			GraphicUtils.drawString(g, this.labels.get(i).getKey(), this.labels.get(i).getValue());
		}

		// Draw graphs
		if (data[0].length > 1) {
			float x1, x2, y1, y2;
			rx = rx / (data[0].length - 1);
			// Draw round marker
			g.setLineWidth(1);
			for (int i = 0; i < this.roundsMarker.size(); i++) {
				g.drawLine(x0 + rx * this.roundsMarker.get(i), y0, x0 + rx * this.roundsMarker.get(i), y0 + ry);
			}
			g.setLineWidth(3);
			ry = ry / (this.maximum - this.minimum);
			// Draw player graphs
			for (int i = 0; i < data.length; i++) {
				// Color for this player
				g.setColor(LINE_COLORS[i]);
				// Player's name
				GraphicUtils.drawString(g,
						new Vector(x0 + 20, FontManager.getInstance().getLineHeight(this.font) * (1.2f * i + 0.5f)),
						this.names[i]);
				// First point
				x2 = x0;
				y2 = (data[i][0] - this.minimum) * ry + y0;
				for (int j = 0; j < data[i].length; j++) {
					// Last point becomes start point
					x1 = x2;
					y1 = y2;
					// Next point becomes end point
					x2 = j * rx + x0;
					y2 = (data[i][j] - this.minimum) * ry + y0;
					// Draw line
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}

		// Reset color and transformation
		g.popTransform();
		g.setColor(c);
		g.setLineWidth(1);
	}

	/**
	 * Set the name for a player
	 * 
	 * @param id   Player-id to set name for
	 * @param name new name for the player
	 */
	public void setName(int id, String name) {
		if (id >= 0 && id < this.names.length) {
			this.names[id] = name;
		}
	}

}
