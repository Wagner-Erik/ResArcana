package resarcana.graphics.utils;

import org.newdawn.slick.Input;

public interface ScrollingListener {

	public void mouseWheelMoved(Input input, int change);

	public boolean isAcceptingScrollingInput();
}
