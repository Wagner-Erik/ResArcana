package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Monument;
import resarcana.game.core.Player;
import resarcana.game.core.Tappable;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.ImageHolder;
import resarcana.game.utils.userinput.ImageOrderSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

/**
 * Ability to reorder the top {@value #AMOUNT} cards of a deck of a player or
 * the monument deck
 * 
 * @author Erik
 *
 */
public class Reorder extends Ability implements Selecting {

	/**
	 * Amount of cards to reorder at maximum
	 */
	private static final int AMOUNT = 3;

	private static final int STATE_IDLE = 0;
	private static final int STATE_TYPE = 1;
	private static final int STATE_REORDER = 2;

	/**
	 * Selected type, either {@link #TYPE_DECK} or {@link #TYPE_MONUMENTS}
	 */
	private ImageHolder type = null;

	/**
	 * Current status of the ability
	 */
	private int status = STATE_IDLE;

	/**
	 * Cards to keep from a player's deck (which are not reordered but transmitted
	 * via {@link UserInputOverwrite})
	 */
	private ArrayList<Tappable> keeping;

	/**
	 * Create a reorder ability for a parent at some relative positon, see also
	 * {@link Ability#Ability(Tappable, Vector)}
	 */
	public Reorder(Tappable parent, Vector relPos) {
		super(parent, relPos);
		this.keeping = new ArrayList<Tappable>();
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable()
				&& (this.getPlayer().getDeck().size() + this.getPlayer().getDiscard().size() > 0 || this.getGame().getMonuments().size() > 2);
	}

	@Override
	public boolean activate() {
		ArrayList<ImageHolder> types = new ArrayList<ImageHolder>();
		if (this.getPlayer().getDeck().size() + this.getPlayer().getDiscard().size() > 0) {
			types.add(TYPE_DECK);
		}
		if (this.getGame().getMonuments().size() > 2) {
			types.add(TYPE_MONUMENTS);
		}
		this.getGameClient().addSelector(new ImageSelector<ImageHolder>(this, types, "Look at deck or monuments?"));
		this.status = STATE_TYPE;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		if (overwrite.getParts().get(0).equalsIgnoreCase("Deck")) {
			this.getTappable().tap();
			this.getPlayer().reorderDeck(UtilFunctions.StringArrayToTappables(
					overwrite.getParts().get(1).split(CommunicationKeys.SEPERATOR_VALUES), this.getGame()));
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(TYPE_DECK);
		} else if (overwrite.getParts().get(0).equalsIgnoreCase("Monuments")) {
			this.getTappable().tap();
			this.getGame().reorderMonuments(UtilFunctions.StringArrayToTappables(
					overwrite.getParts().get(1).split(CommunicationKeys.SEPERATOR_VALUES), this.getGame()));
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(TYPE_MONUMENTS);
		} else {
			Log.warn("Unknown reorder type " + overwrite.getParts().get(0));
			return new HistoryElement(this)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		}
	}

	/**
	 * Issue the reorder-selector for the given type
	 * 
	 * If looking at a deck of a player that has less than {@value #AMOUNT} cards
	 * than a {@link Player#refillAndShuffleDeck()} is called to refill the deck and
	 * look at more cards afterwards
	 * 
	 * @param type either {@link #TYPE_DECK} or {@link #TYPE_MONUMENTS}
	 */
	private void askReorder(ImageHolder type) {
		this.type = type;
		ArrayList<Tappable> toReorder = new ArrayList<Tappable>();
		if (this.type == TYPE_DECK) {
			if (this.getPlayer().getDeck().size() < 3) {
				for (Artifact artifact : this.getPlayer().getDeck()) {
					toReorder.add(artifact);
				}
				this.getPlayer().refillAndShuffleDeck();
			}
			for (Artifact artifact : this.getPlayer().getDeck()) {
				if (!toReorder.contains(artifact)) {
					toReorder.add(artifact);
				}
			}
			this.keeping.clear();
			while (toReorder.size() > AMOUNT) {
				this.keeping.add(toReorder.get(AMOUNT));
				toReorder.remove(AMOUNT);
			}
		} else if (this.type == TYPE_MONUMENTS) {
			for (Monument monument : this.getGame().getMonuments()) {
				toReorder.add(monument);
			}
			toReorder.remove(0);
			toReorder.remove(0);
			while (toReorder.size() > AMOUNT) {
				toReorder.remove(AMOUNT);
			}
		} else {
			Log.warn("Unknown reorder type " + type);
			this.status = STATE_IDLE;
			return;
		}
		this.getGameClient().addSelector(
				new ImageOrderSelector<Tappable>(this, toReorder, "Choose order (1: top-most card)").disableCancel());
		this.status = STATE_REORDER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_TYPE) {
			this.getGameClient().unsetSelector(sel);
			this.askReorder(((ImageSelector<ImageHolder>) sel).getResult());
		} else if (sel instanceof ImageOrderSelector && this.status == STATE_REORDER) {
			this.getGameClient().unsetSelector(sel);
			ArrayList<Tappable> reorder = ((ImageOrderSelector<Tappable>) sel).getResult();
			if (this.type == TYPE_DECK) {
				reorder.addAll(this.keeping);
				this.getGame().abilityFinished(this,
						new UserInputOverwrite(this, "Deck", UtilFunctions.ListToString(reorder)));
			} else if (this.type == TYPE_MONUMENTS) {
				this.getGame().abilityFinished(this,
						new UserInputOverwrite(this, "Monuments", UtilFunctions.ListToString(reorder)));
			} else {
				Log.warn("Unknown reorder type " + type);
			}
			this.status = STATE_IDLE;
		} else {
			Log.warn("Unknown selector " + sel + " for " + this);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		if (sel instanceof ImageOrderSelector) {
			Log.error(this + " canceled while selecting order but this should NOT be possible!");
		}
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}

	/**
	 * Display of the monument-deck for the type-selection
	 */
	private static final ImageHolder TYPE_MONUMENTS = new ImageHolder() {

		@Override
		public String getImage() {
			return "misc/monument_back.png";
		}

		@Override
		public Rectangle getHitbox() {
			return Artifact.ARTIFACT_HITBOX;
		}
	};

	/**
	 * Display of the player-deck for the type-selection
	 */
	private static final ImageHolder TYPE_DECK = new ImageHolder() {

		@Override
		public String getImage() {
			return "misc/artifact_back.png";
		}

		@Override
		public Rectangle getHitbox() {
			return Artifact.ARTIFACT_HITBOX;
		}
	};

}
