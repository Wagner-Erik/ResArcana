package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.MagicItem;
import resarcana.game.core.Tappable;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class Pass extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_ITEM = 1;

	private int status = STATE_IDLE;

	private final Font font;

	public Pass(Tappable parent, Vector relPos) {
		super(parent, relPos);
		this.font = FontManager.getInstance().getFont((int) (Ability.ABILITY_HITBOX.y * 2.25f));
	}

	@Override
	public void draw(Graphics g) {
		if (this.isActivable()) {
			g.setFont(this.font);
			GraphicUtils.drawStringCentered(g, this.getRelPos(), "Pass");
		}
		super.draw(g);
	}

	@Override
	protected boolean isActivable() {
		return this.getPlayer() != null && !this.getPlayer().hasPassed() && this.getPlayer().isActive();
	}

	@Override
	public boolean activate() {
		this.getGameClient().addSelector(
				new ImageSelector<MagicItem>(this, this.getGame().getItems(), "Select your next Magic Item"));
		this.status = STATE_ITEM;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		MagicItem target = (MagicItem) this.getGame().getTappable(overwrite.getParts().get(0));
		if (overwrite.getParts().size() == 1) {
			this.getPlayer().pass(target, null);
		} else {
			this.getPlayer().pass(target, (Artifact) this.getGame().getTappable(overwrite.getParts().get(1)));
		}
		return new HistoryElement(this, target.getPlayer().getName()).setOptionalOne(target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_ITEM) {
			this.getGameClient().unsetSelector(sel);
			MagicItem item = ((ImageSelector<MagicItem>) sel).getResult();
			ArrayList<Artifact> draw = this.getPlayer().drawTopCards(1);
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, item, UtilFunctions.ListToString(draw)));
			this.status = STATE_IDLE;
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}

}
