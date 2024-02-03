package resarcana.game.utils.animation;

import org.newdawn.slick.Color;

import resarcana.graphics.DrawablePollable;

public interface Animation extends DrawablePollable {
	
	public Animation setFixedColor(Color color);
	
	public void start(Color color);
	
	public boolean isRunning();
}
