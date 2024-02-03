package resarcana.game.utils;

import java.util.ArrayList;
import java.util.Collections;

import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.GameClient;
import resarcana.game.core.Artifact;
import resarcana.game.core.Game;
import resarcana.game.core.Mage;
import resarcana.game.core.MagicItem;
import resarcana.game.core.Player;
import resarcana.game.core.Tappable;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.utils.UtilFunctions;

/**
 * Represents a draft for a {@link Game} for one active player and some number
 * of remote players whose actions will be delivered via
 * {@link UserInputOverwrite} objects
 * 
 * @author Erik
 *
 */
public class DraftHelper implements Selecting {

	public static final int CARDS_PER_CHOICE_REMAINING = 0;
	public static final int CARDS_PER_CHOICE_REMOVE = 0;
	public static final int CARDS_PER_CHOICE_PER_PLAYER = 1;
	public static final int MAGE_CHOICES = 2;

	/**
	 * The {@link Game} for which the draft is used
	 */
	private final Game parent;
	/**
	 * Total number of players participating in the game
	 */
	private final int numberPlayers;
	/**
	 * Id of the active player of this draft (this player will do his choices with
	 * this {@link DraftHelper})
	 */
	private final int activePlayer;

	/**
	 * list of available {@link Artifact}s in the draft
	 */
	private ArrayList<Tappable> artifacts;
	/**
	 * list of available {@link Mage}s in the draft
	 */
	private ArrayList<Tappable> mages;
	/**
	 * list of available {@link MagicItem}s in the draft
	 */
	private ArrayList<Tappable> items;

	/**
	 * Choices to pick cards from. The {@link #activePlayer} will only see one per
	 * pick, the others are used to keep track of the draft of all other players.
	 */
	private ArrayList<ArrayList<Tappable>> choices;
	/**
	 * The picked cards for each players forming his deck
	 */
	private ArrayList<ArrayList<Artifact>> cardsPicked;
	/**
	 * The picked mages
	 */
	private Mage[] mageChoice;
	/**
	 * The picked items
	 */
	private MagicItem[] itemChoice;

	/**
	 * Id of the choice currently pending
	 */
	private ArrayList<Integer> curChoice = new ArrayList<Integer>();
	/**
	 * Id of the next choice to be requested, this can and will be "higher" (in
	 * sense of {@link #normalizeId(int)}) than {@link #curChoice} if other players
	 * have already picked a card and the active player is still deciding his choice
	 */
	// private int nextChoice = -1;
	/**
	 * Determines if the card choices are currently given in clockwise or
	 * counterclockwise rotation to the next player, either +1 or -1
	 */
	private int sign = 1;
	/**
	 * Setting for the draft, see {@link #startDraft(int, int, int, int, int)}
	 */
	private int startPlayer, cardsDeck, cardsRemove, cardsRemain, cardsPerChoice;
	/**
	 * <code>true</code> if all artifacts, mages and items have been picked by all
	 * players and the decks have been shuffled
	 */
	private boolean finished = false;

	/**
	 * Creates a draft helper. The draft is started with {@link #startDraft}
	 * 
	 * @param parent        the game for which the draft is used
	 * @param artifacts     list of artifacts to draft from
	 * @param mages         list of mages to draft from
	 * @param items         list of items to draft from
	 * @param numberPlayers number of players participating in the game
	 * @param activePlayer  id of the player to receive choices from this draft, the
	 *                      other players will do the same draft on different
	 *                      clients, the choices will be sync'd between them via
	 *                      {@link Game#draftAction(UserInputOverwrite)} and
	 *                      {@link GameClient#informAllClients_Draft(String)}
	 */
	public DraftHelper(Game parent, ArrayList<Artifact> artifacts, ArrayList<Mage> mages, ArrayList<MagicItem> items,
			int numberPlayers, int activePlayer) {
		this.parent = parent;
		this.numberPlayers = numberPlayers;
		this.activePlayer = activePlayer;
		this.artifacts = new ArrayList<Tappable>();
		this.mages = new ArrayList<Tappable>();
		this.items = new ArrayList<Tappable>();
		this.artifacts.addAll(artifacts);
		this.mages.addAll(mages);
		this.items.addAll(items);

		if (this.mages.size() < this.numberPlayers * MAGE_CHOICES) {
			Log.error("Not enough mages for the draft");
		}
		if (this.items.size() < this.numberPlayers) {
			Log.error("Not enough items for the draft");
		}
	}

	/**
	 * Checks if a draft is possible with the given settings
	 * 
	 * @param numberPlayers   number of players in the game
	 * @param numberArtifacts total number of artifacts to draft from
	 * @param cardsDeck       cards per deck of each player
	 * @param cardsRemove     cards to remove per choice-round (per player)
	 * @param cardsRemain     cards to remain per choice-round (per player)
	 * @param cardsPerChoice  cards to pick from each choice (per player), this
	 *                        results in (a maximum of) numberPlayer *
	 *                        cardsPerChoice picked cards for each player in each
	 *                        choice-round
	 * @return whether a draft is possible or not
	 */
	public static boolean checkSettings(int numberPlayers, int numberArtifacts, int numberMages, int numberItems,
			DraftModes mode) {
		int choices = (int) Math.ceil(mode.deck * 1. / (numberPlayers * mode.round));
		return (mode.deck + choices * mode.remove + mode.remain) * numberPlayers <= numberArtifacts
				&& numberPlayers * 2 <= numberMages && numberPlayers + 1 <= numberItems;
	}

	/**
	 * Starts a draft with the given settings, if possible (tested with
	 * {@link DraftHelper#checkSettings})
	 * 
	 * @param startPlayer    the id of the player starting the game after the draft,
	 *                       needed for MagicItem pick-order
	 * @param cardsDeck      cards per deck of each player
	 * @param cardsRemove    cards to remove per choice-round (per player)
	 * @param cardsRemain    cards to remain per choice-round (per player)
	 * @param cardsPerChoice cards to pick from each choice (per player), this
	 *                       results in (a maximum of) numberPlayer * cardsPerChoice
	 *                       picked cards for each player in each choice-round
	 */
	public void startDraft(int startPlayer, DraftModes mode) {
		if (this.cardsPicked == null && this.choices == null) {
			if (!checkSettings(this.numberPlayers, this.artifacts.size(), this.mages.size(), this.items.size(), mode)) {
				Log.warn("Draft settings not possible!");
				return;
			}
			this.startPlayer = startPlayer;
			this.cardsDeck = mode.deck;
			this.cardsRemove = mode.remove;
			this.cardsRemain = mode.remain;
			this.cardsPerChoice = mode.round;

			this.parent.getGameClient().getDeckViewer().resize(this.cardsDeck, 1);

			this.cardsPicked = new ArrayList<ArrayList<Artifact>>();
			this.choices = new ArrayList<ArrayList<Tappable>>();
			for (int i = 0; i < this.numberPlayers; i++) {
				this.cardsPicked.add(new ArrayList<Artifact>());
				this.choices.add(new ArrayList<Tappable>());
			}
			this.mageChoice = new Mage[this.numberPlayers];
			this.itemChoice = new MagicItem[this.numberPlayers];
			if (mode == DraftModes.RANDOM || mode == DraftModes.RANDOM_BIG) {
				// Fill all players decks
				for (int i = 0; i < this.cardsDeck; i++) {
					for (int j = 0; j < this.numberPlayers; j++) {
						this.cardsPicked.get(j).add((Artifact) this.artifacts.get(i * this.numberPlayers + j));
						if (this.activePlayer == j) {
							this.parent.getGameClient().getDeckViewer()
									.addImage(this.artifacts.get(i * this.numberPlayers + j));
						}
					}
				}
				this.doShuffle();
			} else {
				this.prepareChoices();
				this.askArtifact(this.activePlayer);
			}
			this.parent.getGameClient().getDeckViewer().show();
		} else {
			Log.error("Draft already started!");
		}

	}

	/**
	 * Prepares a new set of artifact-choices for the draft
	 * <p>
	 * The old choices are cleared, but should already used up
	 * ({@link #allChoicesUsedUp()}
	 */
	private void prepareChoices() {
		// Move remaining cards to the back of the artifact deck in reverse player order
		for (int j = this.numberPlayers - 1; j >= 0; j--) {
			if (this.choices.get(j).size() > this.cardsRemain) {
				Log.error("Choices " + j + " not used up!" + UtilFunctions.ListToString(this.choices.get(j)));
			}
			for (int i = 0; i < this.choices.get(j).size(); i++) {
				this.artifacts.remove(this.choices.get(j).get(i));
				this.artifacts.add(this.choices.get(j).get(i));
			}
			this.choices.get(j).clear();
		}
		// Needed amount of cards for the draftrounds
		int numCards = Math.min(this.numberPlayers * this.cardsPerChoice,
				this.cardsDeck - this.cardsPicked.get(0).size());
		// additional cards for remove/remain option per draftround
		numCards += this.cardsRemove + this.cardsRemain;
		// Fill the new card choices
		for (int i = 0; i < numCards; i++) {
			for (int j = 0; j < this.numberPlayers; j++) {
				this.choices.get(j).add(this.artifacts.get(i * this.numberPlayers + j));
			}
		}
		this.curChoice.clear();
	}

	/**
	 * 
	 * @return whether all choices have been used up (only {@link #cardsRemain}
	 *         cards left per choice
	 */
	private boolean allChoicesUsedUp() {
		for (int i = 0; i < this.numberPlayers; i++) {
			if (this.choices.get(i).size() > this.cardsRemain) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Normalizes an id to {@link #numberPlayers}
	 * 
	 * @param id the id to normalize
	 * @return the normalized id (0 <= return < {@link #numberPlayers})
	 */
	private int normalizeId(int id) {
		return ((id % this.numberPlayers) + this.numberPlayers) % this.numberPlayers;
	}

	/**
	 * Process a selection action and advance the draft accordingly, e.g. by giving
	 * the active player the next choice
	 * <p>
	 * After the draft is concluded Player_0 will shuffle all decks and inform all
	 * {@link DraftHelper} about it
	 * 
	 * @param action the selection action to process, this should have been sent by
	 *               a this or another {@link DraftHelper}
	 */
	public void processAction(UserInputOverwrite action) {
		Tappable result = this.parent.getTappable(action.getSource());
		int choice = Integer.parseInt(action.getParts().get(0));
		int player = Integer.parseInt(action.getParts().get(1));
		if (result instanceof Artifact) {
			if (this.choices.get(choice).size() <= this.numberPlayers + this.cardsRemain) {
				Log.info("Adding " + result + " to deck of player " + player);
				this.cardsPicked.get(player).add((Artifact) result);
				if (this.activePlayer == player) {
					this.parent.getGameClient().getDeckViewer().addImage(result);
				}
			}
			this.choices.get(choice).remove(result);
			this.artifacts.remove(result);
			if (this.activePlayer == this.normalizeId(player + this.sign)) {
				this.askArtifact(choice);
			}
			if (this.allChoicesUsedUp()) {
				if (this.cardsPicked.get(0).size() < this.cardsDeck) {
					this.prepareChoices();
					this.sign *= -1;
					this.askArtifact(this.activePlayer);
				} else {
					this.doShuffle();
				}
			}
		} else if (result instanceof Mage) {
			this.mageChoice[player] = (Mage) result;
			if (this.activePlayer == this.normalizeId(this.startPlayer - 1) && this.activePlayer == player) {
				this.askItem();
			}
		} else if (result instanceof MagicItem) {
			this.itemChoice[player] = (MagicItem) result;
			this.items.remove(result);
			// Finish draft when startPlayer has picked his MagicItem
			if (player == this.startPlayer) {
				// Finish the draft
				this.finished = true;
				this.parent.getGameClient().getDeckViewer().resetHighlights();
				// Player_0 informs all clients to deal cards
				if (this.activePlayer == 0) {
					this.parent.getGameClient().informAllClients_DealCards();
				}
			} else {
				if (this.activePlayer == this.normalizeId(player - 1)) {
					this.askItem();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void doShuffle() {
		// Player_0 does shuffle alone and informs others about it
		if (this.activePlayer == 0) {
			String shuffles = "" + this.numberPlayers;
			ArrayList<Artifact> buffer;
			for (int i = 0; i < this.cardsPicked.size(); i++) {
				buffer = (ArrayList<Artifact>) this.cardsPicked.get(i).clone();
				Collections.shuffle(buffer);
				shuffles = shuffles + CommunicationKeys.SEPERATOR_PARTS + UtilFunctions.ListToString(buffer);
			}
			this.parent.getGameClient().informAllClients_Shuffle(
					CommunicationKeys.VALUE_SHUFFLE_DRAFT + CommunicationKeys.SEPERATOR_PARTS + shuffles);
		}
	}

	/**
	 * Prepare the drafted decks according to a shuffle created by Player_0
	 * <p>
	 * The shuffles for different players are seperated by
	 * {@link CommunicationKeys#SEPERATOR_PARTS}, the cards in each shuffle by
	 * {@link CommunicationKeys#SEPERATOR_VALUES}
	 * <p>
	 * The current decks of this draft are overwriten with the given shuffles
	 * 
	 * @param value the String containing the shuffles for <b>all</b> players of the
	 *              draft
	 */
	public void shuffleDecks(String value) {
		Log.info("Shuffling drafted decks");
		String[] parts = value.split(CommunicationKeys.SEPERATOR_PARTS);
		if (this.numberPlayers != Integer.parseInt(parts[1])) {
			Log.error("Wrong number of players for draft shuffle");
		}
		ArrayList<ArrayList<Tappable>> shuffles = new ArrayList<ArrayList<Tappable>>();
		for (int i = 2; i < parts.length; i++) {
			shuffles.add(UtilFunctions.StringArrayToTappables(parts[i].split(CommunicationKeys.SEPERATOR_VALUES),
					this.parent));
			if (!this.cardsPicked.get(i - 2).containsAll(shuffles.get(i - 2))) {
				Log.warn("Shuffle " + (i - 2) + " not correct. Contains "
						+ UtilFunctions.ListToString(shuffles.get(i - 2)) + " instead of "
						+ UtilFunctions.ListToString(this.cardsPicked.get(i - 2)));
			}
		}
		// Mark starting hand in deck viewer
		for (int i = 0; i < Game.START_CARDS; i++) {
			this.parent.getGameClient().getDeckViewer().highlightElement(
					this.cardsPicked.get(this.activePlayer).indexOf(shuffles.get(this.activePlayer).get(i)));
		}
		// Fill decks
		for (int i = 0; i < this.numberPlayers; i++) {
			this.cardsPicked.get(i).clear();
			for (int j = 0; j < this.cardsDeck; j++) {
				this.cardsPicked.get(i).add((Artifact) shuffles.get(i).get(j));
			}
		}
		this.askMages();
	}

	/**
	 * Issue a selector for the next {@link Artifact} for {@link #activePlayer}
	 */
	private void askArtifact(int choice) {
		if (this.choices.get(choice).size() > this.cardsRemain) {
			String message;
			if (this.choices.get(choice).size() > this.numberPlayers + this.cardsRemain) {
				message = "Remove one artifact from the game";
			} else {
				message = "Add one artifact to your deck";
			}
			this.parent.getGameClient()
					.addSelector(new ImageSelector<Tappable>(this, this.choices.get(choice), message).disableCancel());
			this.curChoice.add(choice);
		}
	}

	/**
	 * Issue a selector for the {@link Mage} for {@link #activePlayer}
	 */
	private void askMages() {
		ArrayList<Tappable> select = new ArrayList<Tappable>();
		select.add(this.mages.get(this.activePlayer * 2));
		select.add(this.mages.get(this.activePlayer * 2 + 1));
		this.parent.getGameClient()
				.addSelector(new ImageSelector<Tappable>(this, select, "Choose your mage").disableCancel());
		this.curChoice.add(this.activePlayer);
	}

	/**
	 * Issue a selector for the {@link MagicItem} for {@link #activePlayer}
	 */
	private void askItem() {
		this.parent.getGameClient().addSelector(
				new ImageSelector<Tappable>(this, this.items, "Choose your first magic item").disableCancel());
		this.curChoice.add(this.activePlayer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector) {
			this.parent.getGameClient().unsetSelector(sel);
			this.parent.getGameClient()
					.informAllClients_Draft(new UserInputOverwrite(((ImageSelector<Tappable>) sel).getResult(),
							this.curChoice.get(0), this.activePlayer).getCode());
			this.curChoice.remove(0);
		} else {
			Log.warn("Unknown selector " + sel + " for " + this);
		}
	}

	/**
	 * Deal the cards of the draft to the given players
	 * <p>
	 * This will <b>not</b> draw a starting hand for the players
	 * 
	 * @param players the players to deal the cards to
	 * @return the drafted {@link MagicItem}s for the players
	 */
	public MagicItem[] dealCards(ArrayList<Player> players) {
		if (players.size() != this.numberPlayers) {
			Log.error("Mismatch in player numbers. " + players.size() + " players given for draft with "
					+ this.numberPlayers + " players!");
		}
		Player p;
		for (int i = 0; i < players.size(); i++) {
			p = players.get(i);
			p.initArtifactDeck(this.cardsPicked.get(i));
			p.initMage(this.mageChoice[i]);
			p.setItem(this.itemChoice[i]);
		}
		return this.itemChoice;
	}

	/**
	 * @return <code>true</code> if all artifacts, mages and items have been picked
	 *         by all players and the decks have been shuffled
	 */
	public boolean isFinished() {
		return this.finished;
	}

	@Override
	public void cancelSelection(Selector sel) {
		Log.warn("Cancel should not be possible during draft " + sel);
		this.parent.getGameClient().unsetSelector(sel);
	}

}
