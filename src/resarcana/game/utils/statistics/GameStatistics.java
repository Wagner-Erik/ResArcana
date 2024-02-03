package resarcana.game.utils.statistics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.game.Launcher;
import resarcana.game.core.Game;
import resarcana.graphics.gui.HideableContainer;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.container.SpecialBackgroundContainer;
import resarcana.graphics.gui.objects.ImageButton;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.TextButton;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

/**
 * The statistics for a {@link Game}
 * 
 * @author Erik
 *
 */
public class GameStatistics extends HideableContainer implements Informable {

	private static final Rectangle GRAPH_BOX = new Rectangle(Vector.ZERO, 800, 500);
	private static final float GRAPH_FONT_SCALE = 1.25f;

	private SpecialBackgroundContainer container;
	private AdvancedGridContainer mainCon;
	private StatisticsGraph graph;

	private ArrayList<PlayerStatistic> statistics;

	private ImageButton curProperty;

	/**
	 * Creates an empty statistics keeper GUI object including buttons to switch the
	 * displayed {@link StatisticProperties}
	 * <p>
	 * Use {@link #setPlayerNumber(String[])} to initialize the player count and
	 * {@link #addStatisticsBatch(ArrayList)} to supply with data, rounds can be
	 * marked with {@link #addRoundMarker()}
	 */
	public GameStatistics() {
		super();

		this.statistics = new ArrayList<PlayerStatistic>();
		this.mainCon = new AdvancedGridContainer(3, 1);

		TextButton closeButton = new TextButton(InterfaceFunctions.DIALOG_CLOSE, Input.KEY_ESCAPE, "Close", 2);
		closeButton.addInformable(this);
		TextButton exportButton = new TextButton(InterfaceFunctions.STATISTICS_EXPORT, Input.KEY_ENTER, "Export", 2);
		exportButton.addInformable(this);

		AdvancedGridContainer buttonCon = new AdvancedGridContainer(1, StatisticProperties.values().length);
		ImageButton imageButton;
		int col = 0;
		for (StatisticProperties property : StatisticProperties.values()) {
			imageButton = new ImageButton(property, property.getHitbox(), property.getImage());
			imageButton.addInformable(this);
			buttonCon.add(imageButton, 0, col);
			col++;
			if (this.curProperty == null) {
				this.curProperty = imageButton;
				this.curProperty.setBorder(true);
			}
		}
		this.mainCon.add(buttonCon, 0, 0);

		buttonCon = new AdvancedGridContainer(1, 2, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, GRAPH_BOX.width / 4, 10);
		buttonCon.add(exportButton, 0, 0, AdvancedGridContainer.MODUS_X_LEFT, AdvancedGridContainer.MODUS_DEFAULT);
		buttonCon.add(closeButton, 0, 1, AdvancedGridContainer.MODUS_X_RIGHT, AdvancedGridContainer.MODUS_DEFAULT);
		this.mainCon.add(buttonCon, 2, 0);

		this.graph = new StatisticsGraph(GRAPH_BOX, new String[] { "Player_0" }, GRAPH_FONT_SCALE);
		this.graph.setData(0, new int[] { 0, 3, 5, 2, 3, 0, 1 }); // Test data
		this.mainCon.add(this.graph, 1, 0);

		this.container = new SpecialBackgroundContainer(this.mainCon, true, true, true, true, 1.f);
		this.add(this.container, Vector.ZERO);
	}

	/**
	 * Add a round-marker at the current datapoint
	 */
	public void addRoundMarker() {
		this.graph.addRoundMarker();
	}

	/**
	 * Set the name for a player in the statistics
	 * 
	 * @param id   the player-id
	 * @param name the new name for the player
	 */
	public void setName(int id, String name) {
		this.graph.setName(id, name);
	}

	/**
	 * Sets the number of players in these statistics
	 * <p>
	 * This also <b>resets</b> the statistics
	 * 
	 * @param players
	 */
	public void setPlayerNumber(String[] players) {
		Log.debug("Creating statistics for " + players.length + " players");
		this.statistics.clear();
		for (int i = 0; i < players.length; i++) {
			this.statistics.add(new PlayerStatistic());
		}
		this.mainCon.remove(this.graph);
		this.graph = new StatisticsGraph(GRAPH_BOX, players, GRAPH_FONT_SCALE);
		this.mainCon.add(this.graph, 1, 0);
	}

	/**
	 * Adds a new datapoint for all players
	 * 
	 * @param stats the data to append to all players data, one
	 *              {@link StatisticsElement} for each player
	 */
	public void addStatisticsBatch(ArrayList<StatisticsElement> stats) {
		if (stats.size() == this.statistics.size()) {
			for (int i = 0; i < this.statistics.size(); i++) {
				this.statistics.get(i).addStatisticsElement(stats.get(i));
			}
			this.refreshDisplayedData();
		} else {
			Log.warn("Mismatching statistics batch-size: " + stats.size() + ", expected " + this.statistics.size());
		}
	}

	/**
	 * Refresh the displayed data using the currently selected
	 * {@link StatisticProperties} (stored in {@link #curProperty})
	 */
	public void refreshDisplayedData() {
		for (int i = 0; i < this.statistics.size(); i++) {
			this.graph.setData(i,
					this.statistics.get(i).getValues((StatisticProperties) this.curProperty.getFunction()));
		}
		this.graph.resetRanges();
	}

	/**
	 * Export the statistics to a file in "./stats/" using the
	 * {@link Launcher#FILE_IDENTIFIER} for the filename
	 */
	public void exportStatistics() {
		String file = "./stats/" + Launcher.FILE_IDENTIFIER + ".stats";
		if (this.statistics.size() > 0) {
			PrintStream stream;
			try {
				stream = new PrintStream(file);
				String line;
				for (int idx = 0; idx < this.statistics.get(0).size(); idx++) {
					line = "";
					for (int i = 0; i < this.statistics.size(); i++) {
						line = line + this.statistics.get(i).getElement(idx) + " ";
					}
					stream.println(line);
				}
				stream.close();
			} catch (FileNotFoundException e) {
				Log.error("Could not save statistics", e);
			}
		}
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (object.getFunction() == InterfaceFunctions.DIALOG_CLOSE) {
				this.hide();
			} else if (object.getFunction() == InterfaceFunctions.STATISTICS_EXPORT) {
				this.exportStatistics();
			} else if (object.getFunction() instanceof StatisticProperties) {
				this.curProperty.setBorder(false);
				this.curProperty = (ImageButton) object;
				this.curProperty.setBorder(true);
				this.refreshDisplayedData();
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

	@Override
	protected boolean resize() {
		return this.setHitbox(this.container.getHitbox());
	}
}
