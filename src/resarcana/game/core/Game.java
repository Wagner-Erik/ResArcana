package resarcana.game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import javafx.util.Pair;
import resarcana.communication.CommunicationKeys;
import resarcana.game.GameClient;
import resarcana.game.abilities.Attack;
import resarcana.game.abilities.specials.VialOfLight;
import resarcana.game.core.Tappable.CollectMode;
import resarcana.game.utils.BoardPositioner;
import resarcana.game.utils.DraftHelper;
import resarcana.game.utils.DraftModes;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.GameCamera;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.Numerator;
import resarcana.game.utils.animation.FadingTrace;
import resarcana.game.utils.factory.ArtifactFactory;
import resarcana.game.utils.factory.MageFactory;
import resarcana.game.utils.factory.MagicItemFactory;
import resarcana.game.utils.factory.MonumentFactory;
import resarcana.game.utils.factory.PowerPlaceFactory;
import resarcana.game.utils.factory.ScrollFactory;
import resarcana.game.utils.statistics.StatisticProperties;
import resarcana.game.utils.statistics.StatisticsElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.DrawablePollable;
import resarcana.graphics.Engine;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.Scheduler;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;
import resarcana.utils.UtilFunctions;

public class Game implements DrawablePollable, Selecting {

	private static final int MAXIMUM_PLAYERS = 4;
	private static final int POWER_PLACE_BASE_COUNT = 2; // #places = #players + POWER_PLACE_BASE_COUNT
	private static final int MONUMENT_BASE_COUNT = 4; // #monuments = 2 * #players + MONUMENTS_BASE_COUNT
	private static final int POINTS_TO_WIN = 10;

	public static final int START_CARDS = 3;
	public static final int BUYABLE_MONUMENTS = 3; // INCLUDES the draw pile as a buy option

	public static final int PLAYERS_PER_ROW = 2;
	public static final int PRIORITY_PLAYERS_PER_ROW = 2;

	private static final float PLAYER_OFFSET_RATIO = 0.6f; // should be 0.5+
	private static final float PLAYER_DISTANCE_WIDTH_RATIO = 1.3f; // should be 1+
	private static final float PLAYER_DISTANCE_HEIGHT_RATIO = 1.6f; // should be 1+

	private static final float PLACE_DISTANCE_RATIO = 1.2f; // should be 1+

	public static final Vector POWER_PLACE_POSITION = new Vector(
			Player.PLAYER_HITBOX.width * (PLAYER_OFFSET_RATIO + PLAYER_DISTANCE_WIDTH_RATIO / 2),
			Player.PLAYER_HITBOX.height * (PLAYER_OFFSET_RATIO + PLAYER_DISTANCE_HEIGHT_RATIO / 2)
					- 3 * PLACE_DISTANCE_RATIO * PowerPlace.PLACE_HITBOX.height);

	public static final Vector MAGIC_ITEM_POSITION = new Vector(
			Player.POSITION_FIRST_BOARD_SPACE.x + Player.PLAYER_HITBOX.width * PLAYER_OFFSET_RATIO,
			Player.PLAYER_HITBOX.height * (PLAYER_OFFSET_RATIO + PLAYER_DISTANCE_HEIGHT_RATIO / 2));

	public static final Vector MONUMENT_POSITION = MAGIC_ITEM_POSITION
			.add(Player.PLAYER_HITBOX.width * PLAYER_DISTANCE_WIDTH_RATIO, 0);

	public static final Vector SCROLL_POSITION = MONUMENT_POSITION.add(Monument.MONUMENT_HITBOX.width * 4.0f,
			-Scroll.SCROLL_HITBOX.height * 0.55f);

	public static void scheduleImages() {		
		// Load all miscellaneous images
		Scheduler.getInstance().addMarker("Miscellaneous");
		Scheduler.getInstance().scheduleResource("misc/artifact_back.png");
		Scheduler.getInstance().scheduleResource("misc/item_back.png");
		Scheduler.getInstance().scheduleResource("misc/mage_back.png");
		Scheduler.getInstance().scheduleResource("misc/monument_back.png");
		Scheduler.getInstance().scheduleResource("misc/scroll_back.png");
		Scheduler.getInstance().scheduleResource("misc/token_firstplayer.png");
		Scheduler.getInstance().scheduleResource("misc/token_passed.png");
		Scheduler.getInstance().scheduleResource("misc/point_token.png");
		Scheduler.getInstance().scheduleResource("misc/winner_banner.png");
		Scheduler.getInstance().scheduleResource("misc/discard_all.png");
		Scheduler.getInstance().scheduleResource("misc/discard_gold.png");
		Scheduler.getInstance().scheduleResource("misc/your_turn.png");
		Scheduler.getInstance().scheduleResource("misc/income_pending.png");
		Scheduler.getInstance().scheduleResource("misc/ability_border_active.png");
		Scheduler.getInstance().scheduleResource("misc/ability_border_available.png");
		Scheduler.getInstance().scheduleResource("misc/ability_hitbox.png");
		Scheduler.getInstance().scheduleResource("misc/player_glow.png");
		Scheduler.getInstance().scheduleResource("misc/glow_raw.png");
		Scheduler.getInstance().scheduleResource("misc/glow_cost_1.png");
		Scheduler.getInstance().scheduleResource("misc/glow_cost_2.png");
		Scheduler.getInstance().scheduleResource("misc/glow_cost_3.png");
		Scheduler.getInstance().scheduleResource("misc/glow_cost_4.png");
		Scheduler.getInstance().scheduleResource("misc/particle.png");
		Scheduler.getInstance().scheduleResource("misc/particle_star.png");
		Scheduler.getInstance().scheduleResource("misc/history_discard_all.png");
		Scheduler.getInstance().scheduleResource("misc/history_discard_gold.png");
		Scheduler.getInstance().scheduleResource("misc/history_income.png");
		
		// Load animation images
		Scheduler.getInstance().addMarker("Animations");
		FadingTrace.scheduleImages();
		Scheduler.getInstance().scheduleResource("animation/arrow.png");
		Scheduler.getInstance().scheduleResource("animation/arrow_2.png");
		Scheduler.getInstance().scheduleResource("animation/dragon.png");
		Scheduler.getInstance().scheduleResource("animation/demon.png");
		Scheduler.getInstance().scheduleResource("animation/dancing_sword.png");
		Scheduler.getInstance().scheduleResource("animation/demon_slayer.png");
		Scheduler.getInstance().scheduleResource("animation/guard_dog.png");
		Scheduler.getInstance().scheduleResource("animation/lion.png");
		Scheduler.getInstance().scheduleResource("animation/protect_tree.png");
		Scheduler.getInstance().scheduleResource("animation/shield.png");
		Scheduler.getInstance().scheduleResource("animation/smoke.png");

		// Load all background images
		Scheduler.getInstance().addMarker("Background");
		Scheduler.getInstance().scheduleResource("background/background_parchment.png");
		Scheduler.getInstance().scheduleResource("background/background_hand.png");

		// Make sure the Essences are available
		Scheduler.getInstance().addMarker("Essences");
		for (Essences e : Essences.values()) {
			e.getImage();
		}
		
		// Make sure the DraftModes are available
		Scheduler.getInstance().addMarker("Draft");
		for (DraftModes draft : DraftModes.values()) {
			draft.getImage();
		}
	}

	/**
	 * Indexer for all {@link Tappable}s to give each a unique index
	 */
	Numerator tappableIndexer = new Numerator();
	Numerator playerIndexer = new Numerator();
	Numerator abilityIndexer = new Numerator();
	/**
	 * List of all {@link Tappable}s to make them accessable via {@link getTappable}
	 * and {@link Game#getTappable(String)}
	 */
	ArrayList<Tappable> allTappables = new ArrayList<Tappable>();
	/**
	 * <code>false</code> if no more {@link Tappable}s should currently be created
	 */
	ArrayList<Player> allPlayers = new ArrayList<Player>();
	ArrayList<Ability> allAbilities = new ArrayList<Ability>();
	boolean constructionAllowed = true;

	private static final int STATE_IDLE = 0;
	private static final int STATE_DRAFT_MODE = 1;
	private static final int STATUS_VIAL_OF_LIGHT = 2;

	private final GameClient parent;
	private final BoardPositioner playerPositions, powerPlacePositions, monumentPositions, scrollPositions;

	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Player> activePlayers = new ArrayList<Player>();
	private ArrayList<Player> disconnectedPlayers = new ArrayList<Player>();
	private ArrayList<Integer> votedNextRound = new ArrayList<Integer>();

	private ArrayList<Artifact> artifacts;
	private ArrayList<Monument> monuments;
	private ArrayList<MagicItem> items;
	private ArrayList<Mage> mages;
	private ArrayList<PowerPlace> places;
	private ArrayList<Scroll> scrolls;

	private int turn = -1, round = -1, activePlayerNumber = -1;

	private GameCamera camera;
	private Ability activeAbility;

	private boolean started = false;
	private int playerId = -1;
	private boolean cardsDealt = false;
	private int startingPlayer = 0, nextStartingPlayer = 0;
	private boolean allIncomeFinished = false;
	private Player playerPlayingCard = null;
	private int protectionsRemaining = 0;
	private boolean gameFinished;
	private Player winner = null;
	private DraftHelper draft = null;
	private int status = STATE_IDLE;
	private ArrayList<EssenceSelection> incomeCounts = new ArrayList<EssenceSelection>();

	private VialOfLight vialOfLight = null;
	private boolean vialOfLightAction = false;

	public Game(GameClient parent) {
		this.parent = parent;
		this.camera = new GameCamera(Vector.ZERO, Player.PLAYER_HAND_HITBOX, this.getTableHitbox().scale(1.2f), 1.0f,
				Engine.getInstance().getHeight() / Artifact.ARTIFACT_HITBOX.height);
		this.camera.setGUI(this.getGameClient().getGameState());
		// Position setup for players, places of power and scrolls around the table
		this.playerPositions = new BoardPositioner((int) Math.ceil(1.0 * MAXIMUM_PLAYERS / PLAYERS_PER_ROW),
				PLAYERS_PER_ROW,
				new Vector(Player.PLAYER_HITBOX.width * PLAYER_OFFSET_RATIO,
						Player.PLAYER_HITBOX.height * PLAYER_OFFSET_RATIO),
				Vector.ZERO, Player.PLAYER_HITBOX.width * PLAYER_DISTANCE_WIDTH_RATIO,
				Player.PLAYER_HITBOX.height * PLAYER_DISTANCE_HEIGHT_RATIO, PRIORITY_PLAYERS_PER_ROW);
		this.powerPlacePositions = new BoardPositioner(POWER_PLACE_BASE_COUNT + MAXIMUM_PLAYERS, 1,
				POWER_PLACE_POSITION, Vector.ZERO, 0, PowerPlace.PLACE_HITBOX.height * PLACE_DISTANCE_RATIO);
		this.monumentPositions = new BoardPositioner(1, BUYABLE_MONUMENTS, MONUMENT_POSITION, Vector.ZERO);
		this.scrollPositions = new BoardPositioner(2, 4, SCROLL_POSITION, Vector.ZERO,
				Scroll.SCROLL_HITBOX.width * 1.1f, Scroll.SCROLL_HITBOX.height * 1.1f);
		// Reset players, tappables and abilities
		this.resetConstruction();
		// Generate all cards once
		this.artifacts = ArtifactFactory.createAll(this);
		this.monuments = MonumentFactory.createAll(this);
		this.items = MagicItemFactory.createAll(this);
		this.mages = MageFactory.createAll(this);
		this.places = PowerPlaceFactory.createAll(this);
		this.scrolls = ScrollFactory.createAll(this);
		// Stop card generation
		this.stopConstruction();
		// Magic item start positions
		for (int i = 0; i < this.items.size(); i++) {
			this.items.get(i).setPosition(
					MAGIC_ITEM_POSITION.add(new Vector(BoardPositioner.DEFAULT_DISTANCE_COLUMN, 0).mul(i)));
		}
		// Scroll start positions
		for (int i = 0; i < this.scrolls.size(); i++) {
			this.scrolls.get(i).setPosition(this.scrollPositions.getNextEmptyPosition());
		}
	}

	/**
	 * Get a {@link Player} by its {@link Player#toString()} identifier
	 * 
	 * @param identifier the identifier of the sought after {@link Player}
	 * @return the {@link Player} or <code>null</code> if it could not be found (was
	 *         not generated in this form (yet?))
	 */
	public Player getPlayer(String identifier) {
		for (Player player : allPlayers) {
			if (player.toString().equalsIgnoreCase(identifier)) {
				return player;
			}
		}
		Log.error("No Player found with identifier: " + identifier);
		return null;
	}

	/**
	 * Get a {@link Ability} by its {@link Ability#toString()} identifier
	 * 
	 * @param identifier the identifier of the sought after {@link Ability}
	 * @return the {@link Ability} or <code>null</code> if it could not be found
	 *         (was not generated in this form (yet?))
	 */
	public Ability getAbility(String identifier) {
		for (Ability ability : this.allAbilities) {
			if (ability.toString().equalsIgnoreCase(identifier)) {
				return ability;
			}
		}
		Log.warn("No Ability found with identifier: " + identifier);
		return null;
	}

	/**
	 * Get a {@link Tappable} by its {@link Tappable#toString()} identifier
	 * 
	 * @param identifier the identifier of the sought after {@link Tappable}
	 * @return the {@link Tappable} or <code>null</code> if it could not be found
	 *         (was not generated in this form (yet?))
	 */
	public Tappable getTappable(String identifier) {
		for (Tappable tappable : allTappables) {
			if (tappable.toString().equalsIgnoreCase(identifier)) {
				return tappable;
			}
		}
		Log.warn("No Tappable found with identifier: " + identifier);
		return null;
	}

	/**
	 * Resets the {@link Tappable}, {@link Ability} and {@link Player} indexer and
	 * counter, effectivly allowing for a new set of {@link Tappable}s,
	 * {@link Ability}s and {@link Player}s to be generated
	 * <p>
	 * Resets also a previous {@link Game#stopConstruction()}
	 */
	public void resetConstruction() {
		this.allAbilities.clear();
		this.abilityIndexer = new Numerator();
		this.allPlayers.clear();
		this.playerIndexer = new Numerator();
		this.allTappables.clear();
		this.tappableIndexer = new Numerator();
		this.constructionAllowed = true;
	}

	/**
	 * Stop the construction of {@link Tappable}s and {@link Ability}s. This will
	 * result in a {@link Log#warn(String)} if another one is created afterwards
	 */
	public void stopConstruction() {
		constructionAllowed = false;
	}

	private Rectangle getTableHitbox() {
		int num = this.players.size();
		if (num == 0) {
			num = MAXIMUM_PLAYERS;
		}
		int rows = ((num <= PRIORITY_PLAYERS_PER_ROW * (int) Math.ceil(1.0 * MAXIMUM_PLAYERS / PLAYERS_PER_ROW))
				? (int) Math.ceil(1.0 * num / PLAYERS_PER_ROW)
				: (int) Math.ceil(1.0 * MAXIMUM_PLAYERS / PLAYERS_PER_ROW));
		return new Rectangle(0, 0, Math.max((int) (Math.ceil(num / rows)), 2) * 1.2f * Player.PLAYER_HITBOX.width,
				(rows + 0.2f) * 1.25f * Player.PLAYER_HITBOX.height);
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		// Apply camera
		this.getCamera().applyCamera(g);
		// Boards of players
		for (Player player : this.players) {
			player.draw(g);
		}
		if (this.started) {
			// Remaining MagicItems
			if (this.round >= 0) {
				for (MagicItem item : this.items) {
					item.draw(g);
				}
			}
			// Remaining places of power
			for (PowerPlace place : this.places) {
				if (this.getClientPlayer().isActive() && this.getClientPlayer().isPayable(place.getCost())) {
					place.drawBuyGlowing(g);
				}
				place.draw(g);
			}
			// Remaining monuments
			for (int i = 0; i < this.monuments.size() && i < BUYABLE_MONUMENTS; i++) {
				Monument monument = this.monuments.get(i);
				if (this.getClientPlayer().isActive() && this.getClientPlayer().isPayable(monument.getCost())) {
					monument.drawBuyGlowing(g);
				}
				if (i == BUYABLE_MONUMENTS - 1) {
					monument.drawCardBack(g);
					// Remaining count
					g.setFont(FontManager.getInstance().getFont(2 * Parameter.GUI_STANDARD_FONT_SIZE));
					GraphicUtils.drawString(g, monument.getHitbox().getTopRightCorner().add(Player.OFFSET_CARD_COUNTER),
							"" + (this.monuments.size() - 2));
				} else {
					monument.draw(g);
				}
			}
			// Remaining scrolls
			for (Scroll scroll : this.scrolls) {
				scroll.draw(g);
			}
			// Hand of this clients player
			this.getClientPlayer().drawHand(g);
		}
		g.popTransform();
	}

	@Override
	public void poll(Input input, float secounds) {
		this.camera.poll(input, secounds);
		if (this.winner != null && !this.gameFinished) {
			this.finishGame(winner);
		}
		if (this.started) {
			for (Player player : this.players) {
				player.poll(input, secounds);
			}
		}
		for (int i = 0; i < this.places.size(); i++) {
			PowerPlace place = this.places.get(i);
			place.pollDetailedView(input, secounds);
			if (this.started && !this.gameFinished) {
				if (!this.isWaitingForAbility() && this.getClientPlayer().isActive()
						&& this.getClientPlayer().isPayable(place.getCost())) {
					place.pollBuyHitbox(input, secounds);
				}
			}
		}
		for (int i = 0; i < this.monuments.size() && i < BUYABLE_MONUMENTS; i++) {
			Monument monument = this.monuments.get(i);
			if (i < BUYABLE_MONUMENTS - 1) { // Don't poll face-down monuments detailed view
				monument.pollDetailedView(input, secounds);
			}
			if (this.started && !this.gameFinished) {
				if (!this.isWaitingForAbility() && this.getClientPlayer().isActive()
						&& this.getClientPlayer().isPayable(monument.getCost())) {
					monument.pollBuyHitbox(input, secounds);
				}
			}
		}
		for (int i = 0; i < this.items.size(); i++) {
			this.items.get(i).pollDetailedView(input, secounds);
		}
		for (int i = 0; i < this.scrolls.size(); i++) {
			this.scrolls.get(i).pollDetailedView(input, secounds);
		}
	}

	public Player getClientPlayer() {
		return this.players.get(this.playerId);
	}

	public GameCamera getCamera() {
		return this.camera;
	}

	public GameClient getGameClient() {
		return this.parent;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<MagicItem> getItems() {
		return (ArrayList<MagicItem>) this.items.clone();
	}

	public void swapItems(MagicItem returnItem, MagicItem takenItem) {
		returnItem.assignPlayer(null);
		returnItem.untap();
		returnItem.setPosition(takenItem.getPosition());
		this.items.add(returnItem);
		this.items.remove(takenItem);
	}

	public void activate(Ability ability) {
		if (!this.isWaitingForAbility()) { // Ignore if any ability is pending
			if (!ability.activate()) { // If ability is not finished, set ability as pending
				this.activeAbility = ability;
			} else {
				this.getGameClient().informAllClients_Action(new UserInputOverwrite(ability));
			}
		}
	}

	public void abilityFinished(Ability ability, UserInputOverwrite overwrite) {
		if (this.activeAbility == ability) {
			this.activeAbility = null;
			this.getGameClient().informAllClients_Action(overwrite);
		} else {
			Log.warn("Unexpected ability (" + ability + ") called finish. Currently active: " + this.activeAbility);
		}
	}

	public void applyAction(UserInputOverwrite action) {
		switch (action.getSourceType()) {
		case UserInputOverwrite.SOURCE_TYPE_ABILITY:
			this.addToHistory(this.getAbility(action.getSource()).activateOverwrite(action));
			break;
		case UserInputOverwrite.SOURCE_TYPE_TAPPABLE:
			this.getTappable(action.getSource()).userAction(action);
			break;
		case UserInputOverwrite.SOURCE_TYPE_PLAYER:
			this.getPlayer(action.getSource()).userAction(action);
			break;
		default:
			Log.warn("Unknown source type received, skipping action: " + action);
			return;
		}
		// Check if a Vial of Light selection is still pending
		if (!this.vialOfLightAction) {
			this.nextTurn();
		}
	}

	public void giveEssenceToAll(EssenceSelection sel) {
		for (Player p : players) {
			p.modifyEssence(sel, false);
		}
	}

	public void changeName(int id, String name) {
		if (id < this.players.size()) {
			Log.info("Changing name of " + id + " to " + name);
			this.players.get(id).setName(name);
			this.getGameClient().getStatistics().setName(id, name);
		} else {
			Log.error("Attempting to change name of player " + id + " while only " + this.players.size()
					+ " were created.");
		}
	}

	public void start(int numberPlayers, int id) {
		if (this.players.size() == numberPlayers) {
			Log.info("Starting game with " + numberPlayers + " players");
			this.started = true;
			this.playerId = id;
			this.camera.setPosition(this.getClientPlayer().getHitbox().getTopLeftCorner());
			String[] names = new String[numberPlayers];
			for (int i = 0; i < this.players.size(); i++) {
				names[i] = this.players.get(i).getName();
			}
			this.getGameClient().getStatistics().setPlayerNumber(names);
			// Resize camera
			this.camera = new GameCamera(this.camera.getPosition(), this.camera.getBlockedArea(),
					this.getTableHitbox().scale(1.2f), this.camera.getVelocity(), this.camera.getMaxZoom());
			this.camera.setGUI(this.getGameClient().getGameState());
			if (this.playerId == 0) { // First player shuffles cards and informs all clients
				Random rnd = new Random();
				Collections.shuffle(this.artifacts, rnd);
				this.getGameClient()
						.informAllClients_Shuffle(CommunicationKeys.VALUE_GAME_INIT + CommunicationKeys.SEPERATOR_VALUES
								+ CommunicationKeys.VALUE_ARTIFACTS + CommunicationKeys.SEPERATOR_PARTS
								+ UtilFunctions.ListToString(this.artifacts));
				Collections.shuffle(this.monuments, rnd);
				this.getGameClient()
						.informAllClients_Shuffle(CommunicationKeys.VALUE_GAME_INIT + CommunicationKeys.SEPERATOR_VALUES
								+ CommunicationKeys.VALUE_MONUMENTS + CommunicationKeys.SEPERATOR_PARTS
								+ UtilFunctions.ListToString(this.monuments));
				Collections.shuffle(this.items, rnd);
				this.getGameClient()
						.informAllClients_Shuffle(CommunicationKeys.VALUE_GAME_INIT + CommunicationKeys.SEPERATOR_VALUES
								+ CommunicationKeys.VALUE_MAGIC_ITEMS + CommunicationKeys.SEPERATOR_PARTS
								+ UtilFunctions.ListToString(this.items));
				Collections.shuffle(this.mages, rnd);
				this.getGameClient()
						.informAllClients_Shuffle(CommunicationKeys.VALUE_GAME_INIT + CommunicationKeys.SEPERATOR_VALUES
								+ CommunicationKeys.VALUE_MAGES + CommunicationKeys.SEPERATOR_PARTS
								+ UtilFunctions.ListToString(this.mages));
				Collections.shuffle(this.places, rnd);
				this.getGameClient()
						.informAllClients_Shuffle(CommunicationKeys.VALUE_GAME_INIT + CommunicationKeys.SEPERATOR_VALUES
								+ CommunicationKeys.VALUE_POWERPLACES + CommunicationKeys.SEPERATOR_PARTS
								+ UtilFunctions.ListToString(this.places));
				this.askDraftMode();
			}
		} else {
			Log.error("Game start not possible because mismatch of player numbers between this client and server");
		}
	}

	/**
	 * Adds a player to the game if the game has not started and the
	 * {@link #MAXIMUM_PLAYERS} amount has not been reached
	 * 
	 * @param value the name of the player to add
	 */
	public void addPlayer(String value) {
		if (!this.started && this.players.size() < MAXIMUM_PLAYERS) {
			this.players.add(new Player(this, value, this.playerPositions.getNextEmptyPosition()));
		} else {
			Log.warn("Can't add new player because game is already running or full");
		}
	}

//	private Vector getNextPlayerPosition() {
//		return new Vector(Player.PLAYER_HITBOX.width * (0.6f + 1.2f * (this.players.size() % PLAYERS_PER_ROW)),
//				Player.PLAYER_HITBOX.height * (0.6f + 1.3f * (this.players.size() / PLAYERS_PER_ROW)));
//	}

	public int getPlayerId() {
		return this.playerId;
	}

	public void initialShuffle(String value) {
		String[] split = value.split(CommunicationKeys.SEPERATOR_PARTS);
		String[] split1 = split[0].split(CommunicationKeys.SEPERATOR_VALUES);
		String[] split2 = split[1].split(CommunicationKeys.SEPERATOR_VALUES);
		ArrayList<Tappable> tappables = UtilFunctions.StringArrayToTappables(split2, this);
		if (split1[1].equalsIgnoreCase(CommunicationKeys.VALUE_ARTIFACTS)) {
			this.artifacts.clear();
			for (int i = 0; i < tappables.size(); i++) {
				this.artifacts.add((Artifact) tappables.get(i));
			}
		} else if (split1[1].equalsIgnoreCase(CommunicationKeys.VALUE_MONUMENTS)) {
			this.monuments.clear();
			for (int i = 0; i < tappables.size() && i < this.players.size() * 2 + MONUMENT_BASE_COUNT; i++) {
				this.monuments.add((Monument) tappables.get(i));
			}
			this.repositionMonuments();
		} else if (split1[1].equalsIgnoreCase(CommunicationKeys.VALUE_MAGIC_ITEMS)) {
			this.items.clear();
			for (int i = 0; i < tappables.size(); i++) {
				this.items.add((MagicItem) tappables.get(i));
			}
		} else if (split1[1].equalsIgnoreCase(CommunicationKeys.VALUE_MAGES)) {
			this.mages.clear();
			for (int i = 0; i < tappables.size(); i++) {
				this.mages.add((Mage) tappables.get(i));
			}
		} else if (split1[1].equalsIgnoreCase(CommunicationKeys.VALUE_POWERPLACES)) {
			this.places.clear();
			int[] ids = new int[this.players.size() + POWER_PLACE_BASE_COUNT];
			int cnt;
			PowerPlace place;
			for (int i = 0; i < tappables.size()
					&& this.places.size() < this.players.size() + POWER_PLACE_BASE_COUNT; i++) {
				place = (PowerPlace) tappables.get(i);
				for (cnt = 0; cnt < this.places.size(); cnt++) {
					if (ids[cnt] == PowerPlaceFactory.OTHER_SIDES[place.place_ID]) {
						break;
					}
				}
				if (cnt == this.places.size()) {
					this.places.add(place);
					place.setPosition(this.powerPlacePositions.getNextEmptyPosition());
					ids[cnt] = place.place_ID;
				}
			}
		} else {
			Log.error("Unknown shuffle-list-type: " + split1[1]);
		}
	}

	public void refillShuffle(String value) {
		String[] split = value.split(CommunicationKeys.SEPERATOR_PARTS);
		String[] split1 = split[0].split(CommunicationKeys.SEPERATOR_VALUES);
		String[] split2 = split[1].split(CommunicationKeys.SEPERATOR_VALUES);
		Player p = this.getPlayer(split1[1]);
		if (!p.isClientPlayer()) { // Already done for this player
			p.refillAndOrderDeck(UtilFunctions.StringArrayToTappables(split2, this));
		}
	}

	private void saveEssenceCounts() {
		this.incomeCounts.clear();
		for (int i = 0; i < this.players.size(); i++) {
			this.incomeCounts.add(new EssenceSelection(this.players.get(i).getEssenceCounter().getCount()));
		}
	}

	public EssenceSelection getCurrentEssenceDifference(Player player) {
		return new EssenceSelection(player.getEssenceCounter().getCount())
				.getDifference(this.incomeCounts.get(player.getId()));
	}

	public Player checkWinningCondition() {
		int points, essences;
		int maxP = 0, maxE = 0, maxId = -1;
		for (int i = 0; i < this.players.size(); i++) {
			points = this.players.get(i).getTotalPoints();
			essences = this.players.get(i).getEssenceCounter().getTotalCount()
					+ this.players.get(i).getEssenceCounter().getCount()[Essences.GOLD.ordinal()];
			// First player has advantage in case of double tie
			if (points > maxP || (points == maxP && essences > maxE)) {
				maxP = points;
				maxE = essences;
				maxId = i;
			}
		}
		if (maxP >= POINTS_TO_WIN) {
			return this.players.get(maxId);
		} else {
			return null;
		}
	}

	private void finishGame(Player winner) {
		Log.info("Player " + winner + " has won the game with " + winner.getTotalPoints() + " points!");
		this.gameFinished = true;
		SoundManager.getInstance().playGameFinish();
		winner.makeWinner();
		for (Player player : this.players) {
			player.setActive(false);
		}
		this.getGameClient().getStatistics().addStatisticsBatch(this.getCurrentStatistics());
		this.getGameClient().gameFinished(winner);
	}

	public boolean hasGameFinished() {
		return this.gameFinished;
	}

	public void triggerGameOver(Player winner) {
		this.winner = winner;
	}

	@SuppressWarnings("unchecked")
	public void nextRound() {
		Player winner = this.checkWinningCondition();
		if (winner != null) {
			this.triggerGameOver(winner);
		} else {
			this.round++;
			this.getGameClient().getStatistics().addRoundMarker();
			this.votedNextRound.clear();
			Log.info("Starting round " + this.round);
			this.startingPlayer = this.nextStartingPlayer;
			this.activePlayerNumber = this.startingPlayer - 1;
			this.activePlayers = (ArrayList<Player>) this.players.clone();
			for (Player player : this.activePlayers) {
				player.prepareNewRound();
			}
			// Pass disconnected players
			for (Player player : this.disconnectedPlayers) {
				player.pass(this.items.get(0), null);
			}
			this.turn = -1;
			// Process income only for the active player of this game-instance
			// Other players will have their income process via UserInputOverwrite actions
			this.saveEssenceCounts();
			this.getClientPlayer().processIncome();
			this.allIncomeFinished = false;
		}
	}

	public void nextTurn() {
		// Block next turn while income collecting is pending or all players have passed
		if (this.allIncomeFinished && this.activePlayers.size() > 0) {
			this.turn++;
			this.getGameClient().getStatistics().addStatisticsBatch(this.getCurrentStatistics());
			this.activePlayerNumber = (this.activePlayerNumber + 1) % this.activePlayers.size();
			Log.info("Starting turn " + this.turn + " in round " + this.round + " with active player "
					+ this.activePlayerNumber);
			this.printGameInfos();
			this.saveEssenceCounts();
			if (this.activePlayers.get(this.activePlayerNumber).getId() == this.playerId) {
				Log.info("Activating player " + this.playerId);
				this.getClientPlayer().setActive(true);
				SoundManager.getInstance().playYourTurn();
			} else {
				Log.info("Deactivating player " + this.playerId);
				this.getClientPlayer().setActive(false);
			}
		}

	}

	public ArrayList<StatisticsElement> getCurrentStatistics() {
		ArrayList<StatisticsElement> stats = new ArrayList<StatisticsElement>();
		for (int i = 0; i < this.players.size(); i++) {
			stats.add(this.players.get(i).getStats());
		}
		return stats;
	}

	private void printGameInfos() {
		Log.debug("Id: " + this.getPlayerId() + " -- Players: " + UtilFunctions.ListToString(this.players)
				+ " -- Actives: " + UtilFunctions.ListToString(this.activePlayers) + " -- Items: "
				+ UtilFunctions.ListToString(this.items) + " -- Monuments: "
				+ UtilFunctions.ListToString(this.monuments) + " -- Places: " + UtilFunctions.ListToString(this.places)
				+ " -- Scrolls: " + UtilFunctions.ListToString(this.scrolls));
		for (Player player : this.players) {
			Log.debug(player.toString() + "::" + player.getName() + " -- Essences: "
					+ new EssenceSelection(player.getEssenceCounter().getCount()) + " -- Deck: "
					+ UtilFunctions.ListToString(player.getDeck()) + " -- Hand: "
					+ UtilFunctions.ListToString(player.getHand()) + " -- Scrolls: "
					+ UtilFunctions.ListToString(player.getScrolls()) + " -- Discard: "
					+ UtilFunctions.ListToString(player.getDiscard()) + " -- InPlay: "
					+ UtilFunctions.ListToString(player.getTappablesInPlay()) + " -- costRed: "
					+ UtilFunctions.ListToString(player.getCostReducers()) + " -- Protect: "
					+ UtilFunctions.ListToString(player.getProtection()));
		}
	}

	public void passPlayer(Player pass) {
		if (!this.hasAnyPassed()) {
			this.nextStartingPlayer = pass.getId();
		}
		pass.setActive(false);
		if (this.activePlayerNumber >= this.activePlayers.indexOf(pass)) {
			this.activePlayerNumber--;
		}
		this.activePlayers.remove(pass);
		if (this.activePlayers.isEmpty()) {
			this.getGameClient().informAllClients_VoteNextRound();
		}
		SoundManager.getInstance().playPass();
	}

	public boolean isNextStartingPlayer(Player player) {
		return this.nextStartingPlayer == player.getId();
	}

	public boolean hasPassed(Player player) {
		return !this.activePlayers.contains(player);
	}

	public boolean hasAnyPassed() {
		return this.activePlayers.size() < this.players.size() - this.disconnectedPlayers.size();
	}

	public void dealCardsToPlayers() {
		if (!this.cardsDealt) {
			if (this.draft != null) {
				if (this.draft.isFinished()) {
					Log.info("Dealing cards to players");
					MagicItem[] itemChoice = this.draft.dealCards(this.players);
					for (int i = 0; i < itemChoice.length; i++) {
						this.items.remove(itemChoice[i]);
					}
					// Place remaining MagicItems on Board
					for (int i = 0; i < this.items.size(); i++) {
						this.items.get(i).setPosition(
								MAGIC_ITEM_POSITION.add(new Vector(BoardPositioner.DEFAULT_DISTANCE_COLUMN, 0).mul(i)));
					}
					for (Player player : this.players) {
						player.drawTopCards(START_CARDS);
					}
					this.nextRound();
				} else {
					Log.warn("Could not deal cards because draft has not finished");
				}
			} else {
				Log.warn("Could not deal cards because draft is not available");
			}
		} else {
			Log.warn("Card dealing requested after cards were already dealt");
		}
	}

	public void incomeFinished(Player player) {
		Log.info("Player finished income: " + player);
		this.parent.informAllClients_IncomeDone();
	}

	public void incomeFinished(String overwrite) {
		Log.info("Finished income: " + overwrite);
		Player finished = this.players.get(Integer.parseInt(overwrite));
		finished.setIncomeFinished(true);
		this.addToHistory(new HistoryElement(finished.getName(), this.getCurrentEssenceDifference(finished)));
		this.allIncomeFinished = true;
		for (Player player : this.players) {
			if (!player.hasIncomeFinished() && !player.hasDisconnected()) {
				this.allIncomeFinished = false;
				break;
			}
		}
		if (this.allIncomeFinished) { // Income finished continue with a turn
			this.nextTurn();
		}
	}

	/**
	 * @return Whether the income for any player is pending
	 */
	public boolean isIncomePending() {
		return this.round >= 0 && !this.allIncomeFinished;
	}

	public boolean isWaitingForAbility() {
		return this.activeAbility != null || this.playerPlayingCard != null || this.vialOfLightAction;
	}

	public void cardFromHandPlayed(Player player, UserInputOverwrite overwrite) {
		if (this.playerPlayingCard != player) {
			Log.warn(
					"Trying to play card for " + player + " while waiting for " + this.playerPlayingCard + " to do so");
		}
		this.playerPlayingCard = null;
		this.getGameClient().informAllClients_Action(overwrite);
	}

	public void markPlayingCard(Player player) {
		if (this.isWaitingForAbility()) {
			Log.warn("Marking to play card for " + player + " while already waiting for an ability");
		}
		this.playerPlayingCard = player;
	}

	public void voteNextRound(String value) {
		if (!this.votedNextRound.contains(new Integer(Integer.parseInt(value)))) {
			this.votedNextRound.add(new Integer(Integer.parseInt(value)));
		}
		// Client 0 checks if all remaining clients have voted for the next round
		if (this.playerId == 0) {
			for (Player player : this.players) {
				if (!player.hasDisconnected()) { // No need to check disconnected players
					if (!this.votedNextRound.contains(new Integer(player.getId()))) {
						return; // No next round yet
					}
				}
			}
			// Initiate next round
			this.getGameClient().informAllClients_NextRound();
		}
	}

	public void buyPowerPlace(PowerPlace place) {
		if (this.getClientPlayer().isActive() && !this.isWaitingForAbility()) {
			this.getClientPlayer().buyPowerPlace(place);
		}
	}

	public void removePlace(PowerPlace place) {
		this.places.remove(place);
	}

	public void buyMonument(Monument monument) {
		if (this.getClientPlayer().isActive() && !this.isWaitingForAbility()) {
			this.getClientPlayer().buyMonument(monument);
		}
	}

	public void removeMonument(Monument monument) {
		this.monuments.remove(monument);
		this.repositionMonuments();
	}

	private void repositionMonuments() {
		this.monumentPositions.reset();
		for (int i = 0; i < this.monuments.size() && i < BUYABLE_MONUMENTS; i++) {
			this.monuments.get(i).setPosition(this.monumentPositions.getNextEmptyPosition());
		}
	}

	public void attack(UserInputOverwrite action) {
		Attack attack = (Attack) this.getAbility(action.getSource());
		if (this.protectionsRemaining == 0) {
			if (action.getParts().get(0).equalsIgnoreCase("Attack")) {
				this.protectionsRemaining = this.players.size();
				this.activeAbility = attack;
				attack.activateAttack(new EssenceSelection(action.getParts().get(1)), this.getClientPlayer());
			} else {
				Log.warn("Wrong initial attack received: " + action);
			}
		} else {
			this.protectionsRemaining--;
			attack.activateOverwrite(action); // Apply defense to attack
		}
		if (this.protectionsRemaining == 0) {
			this.activeAbility = null;
			this.nextTurn();
		}
	}

	public Pair<Player, Integer> getMaxEssenceCount(Essences input, Player except) {
		int value, max = 0;
		Player pmax = except;
		for (Player player : this.players) {
			if (player != except) {
				value = player.getEssenceCounter().getCount()[input.ordinal()];
				if (value > max) {
					max = value;
					pmax = player;
				}
			}
		}
		return new Pair<Player, Integer>(pmax, new Integer(max));
	}

	public Pair<Player, Integer> getMaxDemonCount(Player except) {
		int value, max = 0;
		Player pmax = except;
		for (Player player : this.players) {
			if (player != except) {
				value = player.getDemons().size();
				if (value > max) {
					max = value;
					pmax = player;
				}
			}
		}
		return new Pair<Player, Integer>(pmax, new Integer(max));
	}

	public ArrayList<Monument> getMonuments() {
		return this.monuments;
	}

	public void reorderMonuments(ArrayList<Tappable> reorder) {
		if (this.monuments.subList(2, 2 + reorder.size()).containsAll(reorder)) {
			for (int i = 0; i < reorder.size(); i++) {
				this.monuments.set(i + 2, (Monument) reorder.get(i));
			}
			this.repositionMonuments();
		} else {
			Log.warn("Trying to reorder " + UtilFunctions.ListToString(reorder)
					+ " while some elements are not in the front of the monument-deck "
					+ UtilFunctions.ListToString(this.monuments));
		}
	}

	public ArrayList<Artifact> getAllDiscards() {
		ArrayList<Artifact> discards = new ArrayList<Artifact>();
		for (Player player : this.players) {
			discards.addAll(player.getDiscard());
		}
		return discards;
	}

	public void control(UserInputOverwrite control) {
		switch (control.getSourceType()) {
		case UserInputOverwrite.SOURCE_TYPE_TAPPABLE:
			if (control.getParts().get(0).equalsIgnoreCase(CommunicationKeys.VALUE_CHANGE_COLLECT)) {
				this.getTappable(control.getSource()).setAutoCollect(CollectMode.valueOf(control.getParts().get(1)));
			}
			break;
		default:
			Log.warn("Unexpected control source " + control.getSourceType());
			break;
		}
	}

	/**
	 * 
	 * @param player the player to test
	 * @return <code>true</code> if player is player active in the current turn
	 */
	public boolean isActivePlayer(Player player) {
		if (this.activePlayerNumber < 0 || this.activePlayerNumber >= this.activePlayers.size()
				|| this.isIncomePending()) {
			return false;
		} else {
			return this.activePlayers.get(this.activePlayerNumber) == player;
		}
	}

	/**
	 * Cancel the activation of an ability
	 * 
	 * @param ability the ability to cancel
	 */
	public void cancelAbility(Ability ability) {
		if (this.activeAbility == ability) {
			this.activeAbility = null;
		} else {
			Log.warn("Trying to cancel " + ability + " while " + this.activeAbility + " is active");
		}
	}

	/**
	 * Inform the game that a player has canceled playing a card from his hand
	 * 
	 * @param player the player who canceled the playing action
	 */
	public void cancelCardPlay(Player player) {
		if (this.playerPlayingCard != player) {
			Log.warn("Canceling card play for player " + player + " while " + this.playerPlayingCard
					+ " is playing a card");
		} else {
			this.playerPlayingCard = null;
		}
	}

	/**
	 * Adds a {@link HistoryElement} to the history display of the parent
	 * ({@link GameClient#getHistoryDisplay()})
	 * 
	 * @param elem the history element to add
	 */
	public void addToHistory(HistoryElement elem) {
		this.getGameClient().getHistoryDisplay().addHistory(elem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_DRAFT_MODE) {
			this.getGameClient().unsetSelector(sel);
			DraftModes mode = ((ImageSelector<DraftModes>) sel).getResult();
			// Inform all clients about selected draft mode
			this.nextStartingPlayer = new Random().nextInt(this.players.size());
			this.getGameClient().informAllClients_Draft(new UserInputOverwrite(this.players.get(0),
					CommunicationKeys.VALUE_START_DRAFT, "" + mode.ordinal(), "" + this.nextStartingPlayer).getCode());
			this.status = STATE_IDLE;
		} else if (sel instanceof EssenceSelector && this.status == STATUS_VIAL_OF_LIGHT) {
			this.getGameClient().unsetSelector(sel);
			this.status = STATE_IDLE;
			this.getGameClient().informAllClients_Action(new UserInputOverwrite(this.vialOfLight.getPlayer(),
					"VialOfLight", ((EssenceSelector) sel).getSelection()));
		} else {
			Log.warn("Game " + this + " could not process selector: " + sel);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		Log.error("Cancelation of game selector should not be possible " + sel);
		this.getGameClient().unsetSelector(sel);
	}

	/**
	 * Ask the player to select a draft mode
	 */
	private void askDraftMode() {
		ArrayList<DraftModes> images = new ArrayList<DraftModes>();
		for (DraftModes mode : DraftModes.values()) {
			if (DraftHelper.checkSettings(this.players.size(), this.artifacts.size(), this.mages.size(),
					this.items.size(), mode)) {
				images.add(mode);
			}
		}
		if (images.size() > 0) {
			this.getGameClient()
					.addSelector(new ImageSelector<DraftModes>(this, images, "Choose draft mode").disableCancel());
			this.status = STATE_DRAFT_MODE;
		} else {
			Log.error("No draft mode possible for " + this.players.size() + " players, " + this.artifacts.size()
					+ " Artifacts, " + this.mages.size() + " mages and " + this.items.size() + " items");
		}
	}

	/**
	 * Process a drafting action or start a draft
	 * 
	 * @param action the drafting action
	 */
	public void draftAction(UserInputOverwrite action) {
		if (this.started) {
			if (action.getParts().get(0).equalsIgnoreCase(CommunicationKeys.VALUE_START_DRAFT)) {
				this.draft = new DraftHelper(this, this.artifacts, this.mages, this.items, this.players.size(),
						this.playerId);
				this.nextStartingPlayer = Integer.parseInt(action.getParts().get(2));
				this.draft.startDraft(this.nextStartingPlayer,
						DraftModes.values()[Integer.parseInt(action.getParts().get(1))]);
			} else {
				if (this.draft != null) {
					this.draft.processAction(action);
				} else {
					Log.warn("Draft not started!");
				}
			}
		} else {
			Log.error("Can't draft, game not started!");
		}
	}

	/**
	 * Shuffle the decks after the draft has been finished
	 * 
	 * @param shuffle the shuffle including decks for all players
	 */
	public void draftShuffle(String shuffle) {
		this.draft.shuffleDecks(shuffle);
	}

	public ArrayList<Tappable> getAllTappablesInPlay() {
		ArrayList<Tappable> all = new ArrayList<Tappable>();
		for (Player player : this.players) {
			all.addAll(player.getTappablesInPlay());
		}
		return all;
	}

	public void claimScroll(Scroll scroll) {
		this.scrollPositions.freePosition(scroll.getPosition());
		this.scrolls.remove(scroll);
	}

	public void returnScroll(Scroll scroll) {
		this.scrolls.add(scroll);
		scroll.setPosition(this.scrollPositions.getNextEmptyPosition());
		scroll.assignPlayer(null);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Scroll> getScrolls() {
		return (ArrayList<Scroll>) this.scrolls.clone();
	}

	public void registerVialOfLightInPlay(VialOfLight card) {
		this.vialOfLight = card;
	}

	public void removeVialOfLightFromPlay() {
		this.vialOfLight = null;
	}

	public boolean isVialOfLightInPlay() {
		return this.vialOfLight != null;
	}

	public void registerVialOfLightAction() {
		if (this.isVialOfLightInPlay()) {
			this.vialOfLightAction = true;
			if (this.vialOfLight.getPlayer().isClientPlayer()) {
				this.getGameClient()
						.addSelector(new EssenceSelector(this, new EssenceSelection(1, Essences.GOLD, Essences.DEATH),
								"Select essences to produce with Vial of Light").disableCancel());
				this.status = STATUS_VIAL_OF_LIGHT;
			}
		} else {
			Log.error("Request to register Vial of Light action while it is not in play");
		}
	}

	public void resolveVialOfLightAction() {
		this.vialOfLightAction = false;
	}

	public void disconnect(String value) {
		int disconnected = Integer.parseInt(value);
		if (disconnected < this.players.size()) {
			Player p = this.players.get(disconnected);
			if (!this.disconnectedPlayers.contains(p)) {
				Log.info("Disconnecting " + p);
				this.disconnectedPlayers.add(p);
				if (!p.hasPassed()) {
					if (this.isActivePlayer(p)) {
						p.pass(this.items.get(0), null);
						this.nextTurn();
					} else {
						p.pass(this.items.get(0), null);
					}
				}
			} else {
				Log.warn(p + " already disconnected!");
			}
		}
	}

	public boolean hasDisconnected(Player player) {
		return this.disconnectedPlayers.contains(player);
	}

	public boolean isSinglePlayerGame() {
		return this.started && this.players.size() == 1;
	}

	public void playDragonAnimations(Player attacker, Color color) {
		for (Player player : this.activePlayers) {
			if (player != attacker) {
				player.playDragonAnimation(color);
			}
		}
	}

	public void playDemonAnimations(Player attacker, Color color) {
		for (Player player : this.activePlayers) {
			if (player != attacker) {
				player.playDemonAnimation(color);
			}
		}
	}

	public void playBowAttackAnimations(Player attacker) {
		for (Player player : this.activePlayers) {
			if (player != attacker) {
				player.playArrowAttack();
			}
		}
	}
}
