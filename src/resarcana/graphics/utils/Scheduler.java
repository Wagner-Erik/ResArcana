package resarcana.graphics.utils;

import java.util.ArrayList;
import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.Log;

import javafx.util.Pair;
import resarcana.graphics.Drawable;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

public class Scheduler implements Drawable {

	private static Scheduler instance;

	public static Scheduler getInstance() {
		if (instance == null) {
			instance = new Scheduler();
		}
		return instance;
	}

	enum ScheduleType {
		RESOURCE, FONT, MARKER
	};

	private static final Color LOADING_BAR_COLOR = new Color(0.1f, 0.8f, 0.1f, 0.8f);

	private LinkedList<Pair<ScheduleType, String>> schedules = new LinkedList<Pair<ScheduleType, String>>();
	private String currentMarker = "";
	private int totalScheduled = 0;
	private int markerRemaining = 0;

	private Rectangle space = new Rectangle(Vector.ZERO, 500, 500), progressBar = this.space;
	private Vector progressPos = Vector.ZERO, markerPos = Vector.ZERO;

	private Font font;

	private Scheduler() {
	}

	public void setSpace(Rectangle space) {
		this.space = space;

		int fontsize = (int) Math.min(4 * Parameter.GUI_STANDARD_FONT_SIZE, this.space.height / 4);
		this.font = FontManager.getInstance().getFont(fontsize);

		this.markerPos = new Vector(this.space.x + this.space.width / 2,
				this.space.y + this.space.height / 2 - FontManager.getInstance().getLineHeight(this.font));
		this.progressPos = new Vector(this.space.x + this.space.width / 2,
				this.space.y + this.space.height / 2 + FontManager.getInstance().getLineHeight(this.font));

		this.progressBar = new Rectangle(this.space.getCenter(), this.space.width,
				FontManager.getInstance().getLineHeight(this.font) * 0.9f);
	}

	public void scheduleResource(String identifier) {
		if (identifier != null) {
			if (!identifier.isEmpty()) {
				Pair<ScheduleType, String> schedule = new Pair<ScheduleType, String>(ScheduleType.RESOURCE, identifier);
				if (!this.schedules.contains(schedule)) {
					this.schedules.add(schedule);
					this.totalScheduled++;
				}
			}
		}
	}

	public void scheduleFont(int size) {
		if (size > 0) {
			Pair<ScheduleType, String> schedule = new Pair<ScheduleType, String>(ScheduleType.FONT, "" + size);
			if (!this.schedules.contains(schedule)) {
				this.schedules.add(schedule);
				this.totalScheduled++;
			}
		}
	}

	public void addMarker(String marker) {
		if (marker != null) {
			if (!marker.startsWith("Loading ") && !marker.startsWith("Reloading ")) {
				marker = "Loading " + marker;
			}
			Pair<ScheduleType, String> schedule = new Pair<ScheduleType, String>(ScheduleType.MARKER, marker);
			this.schedules.add(schedule);
			this.markerRemaining++;
		}
	}

	public boolean hasItemsScheduled() {
		return !this.schedules.isEmpty();
	}

	public void loadNextScheduledItem() {
		Pair<ScheduleType, String> schedule = this.schedules.getFirst();
		switch (schedule.getKey()) {
		case MARKER:
			this.currentMarker = schedule.getValue();
			this.markerRemaining--;
			break;
		case FONT:
			FontManager.getInstance().loadFont(Integer.parseInt(schedule.getValue()));
			break;
		case RESOURCE:
			ResourceManager.getInstance().loadScheduled(schedule.getValue());
			break;
		default:
			Log.error("Unknown schedule type " + schedule.getKey());
			break;
		}
		this.schedules.removeFirst();
	}

	@Override
	public void draw(Graphics g) {
		int progress = this.totalScheduled + this.markerRemaining - this.schedules.size();

		g.setFont(this.font);
		GraphicUtils.drawStringCentered(g, this.markerPos, this.currentMarker);
		GraphicUtils.drawStringCentered(g, this.progressPos, progress + " of " + this.totalScheduled);

		float x = this.progressBar.x, y0 = this.progressBar.y, y1 = y0 + this.progressBar.height;
		float dx = 5;
		GraphicUtils.fill(g,
				new Rectangle(x, y0, (this.progressBar.width - dx) * progress / this.totalScheduled + dx, y1 - y0),
				LOADING_BAR_COLOR);

		progress = (int) (progress * 1.0f / this.totalScheduled * 10);
		y1 = y0 + (y1 - y0) * 0.25f;
		x = x + dx;
		Color c = g.getColor();
		g.setColor(Color.black);
		for (int i = 0; i < progress + 1; i++) {
			GraphicUtils.drawLine(g, new Vector(x, y0), new Vector(x, y1));
			x += (this.progressBar.width - dx) / 10;
		}
		g.setColor(c);
	}

	public void scheduleAllResources(ArrayList<String> resources) {
		for (String resource : resources) {
			this.scheduleResource(resource);
		}
	}

	public void resetScheduleCounter() {
		int marker = 0;
		for (Pair<ScheduleType, String> pair : this.schedules) {
			if (pair.getKey() == ScheduleType.MARKER) {
				marker++;
			}
		}
		this.markerRemaining = marker;
		this.totalScheduled = this.schedules.size() - this.markerRemaining;
	}
}
