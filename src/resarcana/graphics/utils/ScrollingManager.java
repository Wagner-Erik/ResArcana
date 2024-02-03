package resarcana.graphics.utils;

import java.util.ArrayList;

import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;

public class ScrollingManager implements MouseListener {

	private static ScrollingManager instance;

	public static ScrollingManager getInstance() {
		if (instance == null) {
			instance = new ScrollingManager();
		}
		return instance;
	}

	private Input input;

	private ArrayList<ScrollingListener> listeners = new ArrayList<ScrollingListener>();

	private ScrollingManager() {
	}

	public void addListener(ScrollingListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	public void removeListener(ScrollingListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void setInput(Input input) {
		if(this.input != null) {
			this.input.removeMouseListener(this);
		}
		this.input = input;
		if(this.input != null) {
			this.input.addMouseListener(this);
		}
	}

	@Override
	public boolean isAcceptingInput() {
		return true;
	}

	@Override
	public void inputEnded() {
	}

	@Override
	public void inputStarted() {
	}

	@Override
	public void mouseWheelMoved(int change) {
		for (ScrollingListener scrollingListener : this.listeners) {
			if (scrollingListener.isAcceptingScrollingInput()) {
				scrollingListener.mouseWheelMoved(this.input, change);
			}
		}
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
	}

	@Override
	public void mousePressed(int button, int x, int y) {
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
	}
}
