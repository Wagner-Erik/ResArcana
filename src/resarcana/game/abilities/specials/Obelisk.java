package resarcana.game.abilities.specials;

import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Monument;
import resarcana.game.core.Player;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;

public class Obelisk extends Monument {

	private static final int STATE_IDLE = 0;
	private static final int STATE_OUTPUT = 1;

	private int state = STATE_IDLE;

	public Obelisk(Game parent) {
		super(parent, "monument/monument_06.png");
		this.setName("Obelisk");
		this.setPoints(1);
		this.setRawCosts(new EssenceSelection(Essences.GOLD, 4));
	}

	@Override
	public void assignPlayer(Player player) {
		super.assignPlayer(player);
		if (player.isActive()) {
			this.getGameClient().addSelector(new EssenceSelector(this, new EssenceSelection(6, Essences.GOLD),
					"Choose output of " + this.getName()).disableCancel());
			this.state = STATE_OUTPUT;
		}
	}

	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.state == STATE_OUTPUT) {
			this.getGameClient().unsetSelector(sel);
			this.getGameClient().informAllClients_Action(new UserInputOverwrite(this.getPlayer(), "BuyObelisk", this,
					((EssenceSelector) sel).getSelection()));
			this.state = STATE_IDLE;
		} else {
			super.processSelection(sel);
		}
	}
}
