package resarcana.game.abilities;

import resarcana.game.core.Ability;
import resarcana.game.core.Player;
import resarcana.game.core.Tappable;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class VictoryChecker extends Ability {

	public VictoryChecker(Tappable parent, Vector relPos) {
		super(parent, relPos);
	}

	@Override
	public boolean activate() {
		return true;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		Player winner = this.getGame().checkWinningCondition();
		if (winner != null) {
			this.getGame().triggerGameOver(winner);
		}
		return new HistoryElement(this);
	}

}
