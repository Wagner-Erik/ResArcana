package resarcana.game;

import resarcana.graphics.DrawablePollable;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceContainer;

public interface DrawPollInterface extends DrawablePollable, Informable {

	public InterfaceContainer getInterfaceContainer();
}
