package resarcana.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.communication.CommunicationListener;
import resarcana.communication.ListeningThread;
import resarcana.communication.Server;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Player;
import resarcana.game.core.PowerPlace;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.ImageViewer;
import resarcana.game.utils.LogBox;
import resarcana.game.utils.statistics.GameStatistics;
import resarcana.game.utils.statistics.StatisticProperties;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.Engine;
import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.HideableContainer;
import resarcana.graphics.gui.ImageDisplay;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.BorderContainer;
import resarcana.graphics.gui.container.Dialog;
import resarcana.graphics.gui.container.GridContainer;
import resarcana.graphics.gui.container.SpecialBackgroundContainer;
import resarcana.graphics.gui.objects.Checkbox;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.Slider;
import resarcana.graphics.gui.objects.TextField;
import resarcana.graphics.gui.objects.TileableBackgroundButton;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.GraphicsLogSystem;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.Scheduler;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.JarHandler;
import resarcana.utils.Parameter;

/**
 * This is a client for the game which acts as a proxy between the game logic in
 * {@link Game} and the communication with the {@link Server} and provides some
 * GUI accesspoints
 * <p>
 * User selections can be issued via {@link #addSelector(Selector)} and should
 * be reset via {@link #unsetSelector(Selector)}
 * <p>
 * Two {@link ImageViewer} are provided for the deck ({@link #getDeckViewer()})
 * and the game history ({@link #getHistoryDisplay()})
 * <p>
 * Communications with other game instances (via a {@link Server}) are issued
 * with {@link #informAllClients_Action(UserInputOverwrite)} and similar
 * 
 * @author Erik
 *
 */
public class GameClient implements DrawPollInterface, CommunicationListener, ContentListener {

	private static final Rectangle HITBOX_RULES = PowerPlace.PLACE_HITBOX;

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Rules");
		// Load explanation images
		Scheduler.getInstance().scheduleResource("misc/explanation_1.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_2.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_3.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_4.png");
		Scheduler.getInstance().scheduleResource("misc/controls.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_addon_1.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_addon_2.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_addon_3.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_addon_4.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_addon_5.png");
		Scheduler.getInstance().scheduleResource("misc/explanation_addon_6.png");

		// Make statistics available
		Scheduler.getInstance().addMarker("Statistics icons");
		for (StatisticProperties property : StatisticProperties.values()) {
			property.getHitbox();
		}
	}

	// Kommunikations-Parameter
	private int id = -1;
	private volatile ArrayList<String> packetBuffer = new ArrayList<String>();
	private ListeningThread thread;
	private Socket socket = null;
	private PrintWriter writer;
	private boolean ready;

	// GUI
	private BorderContainer interfaceContainer;
	private SpecialBackgroundContainer selector;
	private ImageViewer deckViewer, rules, history;
	private LogBox logbox;
	private GameStatistics statistics;
	private Dialog settings;
	private Slider soundVolume, musicVolume;

	private String detailedCard = null;
	private ArrayList<ImageDisplay> detailedDeck = null;
	private Rectangle detailedBox, detailedDeckBox;
	private String detailedDescription = "";

	private Selector curSelector = null;
	private ArrayList<Selector> nextSelectors = new ArrayList<Selector>();
	private boolean switchSelector = false;

	// Allgemeines
	private final GameState parent;
	private Game game;
//	private TextButton loadSaveButton;

	public GameClient(GameState parent) {
		this.parent = parent;
		this.game = new Game(this);
		this.initGui();
	}

	// GUI

	/**
	 * Initializes all GUI parts for the game client
	 */
	private void initGui() {
		this.detailedBox = new Rectangle(
				new Vector(Engine.getInstance().getWidth() / 2, Engine.getInstance().getHeight() / 2),
				Math.min(Engine.getInstance().getHeight() * 0.8f, 800) * Artifact.ARTIFACT_HITBOX.width
						/ Artifact.ARTIFACT_HITBOX.height,
				Math.min(Engine.getInstance().getHeight() * 0.8f, 800));
		this.detailedDeckBox = this.detailedBox.scale(0.5f);

		this.interfaceContainer = new BorderContainer();

		this.selector = new SpecialBackgroundContainer(null, true, false, true, true, 1.5f);
		this.interfaceContainer.add(this.selector, BorderContainer.POSITION_LOW);

		this.settings = new Dialog();
		this.settings.addContentListenerToAllContentables(this);
		this.settings.addInformableToAllButtons(this);
		this.settings.addTextField("Name");
		this.settings.getContentable("Name").setContent(JarHandler.existJar() ? Launcher.defaultName : "Test player");
		this.settings.addTextButton(InterfaceFunctions.CLIENT_NAME, "Change name");
		this.settings.addTextField("Serveradress");
		try {
			this.settings.getContentable("Serveradress").setContent(
					JarHandler.existJar() ? Launcher.defaultServer : InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.settings.addContentable(new Checkbox(InterfaceFunctions.CLIENT_READY, false), "Ready");
		((Checkbox) this.settings.getContentable("Ready")).setWriteable(false);
		this.settings.addTextButton(InterfaceFunctions.CLIENT_CONNECT, "Connect to server");
		this.settings.addTextButton(InterfaceFunctions.CLIENT_DISCONNECT, "Disconnect from server");

//		this.settings.addTextField("Savefile");
//		this.loadSaveButton = this.settings.addTextButton(InterfaceFunctions.CLIENT_LOAD_SAVE, "Load game");
		// TODO: implement save and load

		this.soundVolume = new Slider(InterfaceFunctions.CLIENT_VOLUME_SOUND, 0, 1, 0.5f);
		this.soundVolume.addContentListener(this);
		this.settings.addContentable(this.soundVolume, "Sound Volume");
		this.musicVolume = new Slider(InterfaceFunctions.CLIENT_VOLUME_MUSIC, 0, 1, 0.2f);
		this.musicVolume.addContentListener(this);
		this.settings.addContentable(this.musicVolume, "Music Volume");

		this.settings.show();
		this.interfaceContainer.add(this.settings, BorderContainer.POSITION_HIGH);

		this.createRules();
		this.interfaceContainer.add(this.rules, BorderContainer.POSITION_HIGH);

		this.deckViewer = new ImageViewer(0.75f, Parameter.DEFAULT_CARDS_PER_DECK, 1);
		this.interfaceContainer.add(this.deckViewer, BorderContainer.POSITION_HIGH);

		this.history = new ImageViewer(0.75f, 8, 1, true);
		this.interfaceContainer.add(this.history, BorderContainer.POSITION_HIGH);

		this.logbox = new LogBox("Errors and warnings");
		GraphicsLogSystem.errorLogBox = this.logbox;
		this.interfaceContainer.add(this.logbox, BorderContainer.POSITION_LEFT);

		int buttonLength = 2;
		float buttonScale = 0.7f;
		GridContainer buttons = new GridContainer(6, 1, GridContainer.MODUS_X_RIGHT, GridContainer.MODUS_DEFAULT);

		buttons.add(new TileableBackgroundButton(InterfaceFunctions.CLIENT_SHOW_SETTINGS, "Settings", buttonScale,
				buttonLength, true, false).addInformable(this), 0, 0);
		buttons.add(new TileableBackgroundButton(InterfaceFunctions.CLIENT_SHOW_RULES, "Rules", buttonScale,
				buttonLength, true, false).addInformable(this), 1, 0);
		buttons.add(new TileableBackgroundButton(InterfaceFunctions.CLIENT_SHOW_DECK, "Deck", buttonScale, buttonLength,
				true, false).addInformable(this), 2, 0);
		buttons.add(new TileableBackgroundButton(InterfaceFunctions.CLIENT_SHOW_HISTORY, "History", buttonScale,
				buttonLength, true, false).addInformable(this), 3, 0);
		buttons.add(new TileableBackgroundButton(InterfaceFunctions.CLIENT_SHOW_STATISTICS, "Statistics", buttonScale,
				buttonLength, true, false).addInformable(this), 4, 0);
		buttons.add(new TileableBackgroundButton(InterfaceFunctions.CLIENT_SHOW_LOGBOX, "Error log", buttonScale,
				buttonLength, true, false).addInformable(this), 5, 0);

		this.interfaceContainer.add(buttons, BorderContainer.POSITION_HIGH_RIGHT);

		this.statistics = new GameStatistics();
		this.interfaceContainer.add(this.statistics, BorderContainer.POSITION_MIDDLE);

		this.interfaceContainer.maximizeSize();
	}

	/**
	 * Create the {@link #rules}-panel for explanation of game rules and control
	 * explanations
	 */
	private void createRules() {
		this.rules = new ImageViewer(1, 12, 2);
		this.rules.addImage("misc/artifact_back.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_1.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_2.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_3.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_4.png", HITBOX_RULES);
		this.rules.addImage("misc/controls.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_addon_1.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_addon_2.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_addon_3.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_addon_4.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_addon_5.png", HITBOX_RULES);
		this.rules.addImage("misc/explanation_addon_6.png", HITBOX_RULES);
	}

	/**
	 * Add a user-input {@link Selector} to the selector-queue, it will be displayed
	 * when the previous selector has been unset via
	 * {@link #unsetSelector(Selector)} which should happen either in
	 * {@link Selecting#processSelection(Selector)} or
	 * {@link Selecting#cancelSelection(Selector)}
	 * 
	 * @param sel the selector to add to the GUI
	 */
	public void addSelector(Selector sel) {
		if (sel != null) {
			if (this.curSelector == null) {
				this.setSelector(sel);
			} else {
				this.nextSelectors.add(sel);
			}
		} else {
			Log.error("Trying to add null-Selector");
		}
	}

	/**
	 * Unsets a {@link Selector} from the GUI if it is the currently displayed
	 * selector
	 * <p>
	 * The next selector in the selector-queue will be pushed forward
	 * 
	 * @param sel the selector to remove from the GUI
	 */
	public void unsetSelector(Selector sel) {
		if (this.curSelector == sel) {
			this.switchSelector = true;
		} else {
			Log.error("Trying to unset selector while it is not active:" + sel);
		}
	}

	/**
	 * Switches to the next selector in the queue if available
	 */
	private void nextSelector() {
		this.selector.setContents(null);
		if (!this.nextSelectors.isEmpty()) {
			this.setSelector(this.nextSelectors.get(0));
			this.nextSelectors.remove(0);
		} else {
			this.curSelector = null;
		}
		this.switchSelector = false;
	}

	/**
	 * Sets a {@link Selector} to be displayed in the GUI
	 * <p>
	 * For this it is set as the content of {@link #selector}
	 * 
	 * @param sel the selector to be displayed
	 */
	private void setSelector(Selector sel) {
		this.curSelector = sel;
		Log.debug("Set selector " + sel);
		this.selector.setContents(this.curSelector.getInterfaceContainer());
	}

	/**
	 * @return <code>true</code> if a {@link Selector} is currently displayed
	 */
	public boolean hasOpenSelectors() {
		return this.curSelector != null;
	}

	/**
	 * @return The {@link ImageViewer} of the GUI for displaying a deck of artifacts
	 */
	public ImageViewer getDeckViewer() {
		return this.deckViewer;
	}

	/**
	 * @return The {@link ImageViewer} of the GUI for displaying a history of game
	 *         events
	 */
	public ImageViewer getHistoryDisplay() {
		return this.history;
	}

	public LogBox getErrorLog() {
		return this.logbox;
	}

	// GUI event handling

	@Override
	public void inform(String line) {
		synchronized (this.packetBuffer) {
			this.packetBuffer.add(line);
		}
	}

	@Override
	public void disconnected(ListeningThread listeningThread) {
		// Nothing to do
	}

	@Override
	public InterfaceContainer getInterfaceContainer() {
		return this.interfaceContainer;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		InterfaceFunction function = object.getFunction();
		if (object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (function == InterfaceFunctions.CLIENT_NAME) {
				this.refreshName();
			} else if (function == InterfaceFunctions.CLIENT_SHOW_SETTINGS) {
				this.switchShow(this.settings);
			} else if (function == InterfaceFunctions.CLIENT_SHOW_DECK) {
				this.switchShow(this.deckViewer);
			} else if (function == InterfaceFunctions.CLIENT_SHOW_RULES) {
				this.switchShow(this.rules);
			} else if (function == InterfaceFunctions.CLIENT_SHOW_HISTORY) {
				this.switchShow(this.history);
			} else if (function == InterfaceFunctions.CLIENT_SHOW_LOGBOX) {
				this.switchShow(this.logbox);
			} else if (function == InterfaceFunctions.CLIENT_SHOW_STATISTICS) {
				this.switchShow(this.statistics);
			} else if (function == InterfaceFunctions.CLIENT_CONNECT) {
				if (!this.isConnected()) {
					this.connectToServer();
					this.refreshName();
				} else {
					Log.info("Already connected to server");
				}
			} else if (function == InterfaceFunctions.CLIENT_DISCONNECT) {
				if (this.isConnected()) {
					this.addSelector(new EssenceSelector(new Selecting() {

						@Override
						public void processSelection(Selector sel) {
							GameClient.this.unsetSelector(sel);
							GameClient.this.thread.disconnect();
							GameClient.this.sendToServer(CommunicationKeys.META_DISCONNECT
									+ CommunicationKeys.SEPERATOR_MAIN + GameClient.this.id);
						}

						@Override
						public void cancelSelection(Selector sel) {
							GameClient.this.unsetSelector(sel);
						}
					}, new EssenceSelection(EnumSet.allOf(Essences.class)), "Disconnect from server?"));
				}
			}
		}
	}

	private void switchShow(HideableContainer con) {
		if (con.isShown()) {
			this.hideAll();
		} else {
			this.hideAll();
			con.show();
		}
	}

	private void hideAll() {
		this.deckViewer.hide();
		this.settings.hide();
		this.rules.hide();
		this.history.hide();
		this.logbox.hide();
		this.statistics.hide();
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void contentChanged(Contentable object) {
		if (object instanceof Checkbox) {
			if (((Checkbox) object).getFunction() == InterfaceFunctions.CLIENT_READY) {
				this.ready = ((Checkbox) object).isChecked();
				this.sendReady();
			}
		} else if (object instanceof Slider) {
			if (((Slider) object).getFunction() == InterfaceFunctions.CLIENT_VOLUME_SOUND) {
				SoundManager.getInstance().setSoundVolume((float) Math.pow(((Slider) object).getValue(), 2));
			} else if (((Slider) object).getFunction() == InterfaceFunctions.CLIENT_VOLUME_MUSIC) {
				SoundManager.getInstance().setMusicVolume((float) Math.pow(((Slider) object).getValue(), 2));
			}
		}
	}

	// Draw und Poll

	@Override
	public void draw(Graphics g) {
		// Draw game
		this.game.draw(g);
	}

	public void drawDetailedCard(Graphics g) {
		if (this.detailedCard != null) {
			GraphicUtils.drawImageUndistorted(g, this.detailedBox,
					ResourceManager.getInstance().getImage(this.detailedCard));
		}
		if (this.detailedDeck != null) {
			int rows = Math.min((int) Math.ceil(this.detailedDeck.size() / 4.f), 2);
			int cols = (int) Math.ceil(this.detailedDeck.size() * 1.f / rows);
			for (int i = 0; i < this.detailedDeck.size(); i++) {
				GraphicUtils.drawImageUndistorted(g,
						this.detailedDeckBox.modifyCenter(this.detailedDeckBox.getCenter().add(
								this.detailedDeckBox.width * 1.1f * ((i % cols) - (cols - 1) / 2.f),
								this.detailedDeckBox.height * 1.1f * ((i / cols) - (rows - 1) / 2.f))),
						ResourceManager.getInstance().getImage(this.detailedDeck.get(i).getImage()));
			}
			g.setFont(FontManager.getInstance().getFont(Parameter.GUI_STANDARD_FONT_SIZE * 3));
			GraphicUtils.drawStringCentered(g,
					this.detailedDeckBox.getCenter().add(0,
							-0.55f * FontManager.getInstance().getLineHeight(g.getFont())
									- (0.55f * rows - 0.05f) * this.detailedDeckBox.height),
					this.detailedDescription, Color.white);
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.switchSelector) {
			this.nextSelector();
		}
		this.game.poll(input, secounds);
		if (!input.isKeyDown(Input.KEY_LALT)) {
			this.detailedCard = null;
			this.detailedDeck = null;
		}
	}

	// Kommunikation mit dem Server

	@SuppressWarnings("unchecked")
	public void processPackets() {
		ArrayList<String> packets;
		synchronized (this.packetBuffer) {
			packets = (ArrayList<String>) this.packetBuffer.clone();
		}
		for (String line : packets) {
			this.processLine(line);
		}
	}

	/**
	 * Processes a single line (which should represent a single command) received
	 * from the {@link Server}
	 * <p>
	 * See {@link CommunicationKeys} for explanation and key-phrases to use
	 * 
	 * @param line the line to be processed
	 */
	private void processLine(String line) {
		Log.info(this.id + " received " + line);
		String[] split = line.split(CommunicationKeys.SEPERATOR_END)[0].split(CommunicationKeys.SEPERATOR_MAIN);
		if (split.length == 4 && split[0].equalsIgnoreCase(CommunicationKeys.MARKER_SERVER)) {
			boolean allAdressed = Boolean.parseBoolean(split[1]);
			String action = split[2];
			String value = split[3];
			if (allAdressed) {
				if (action.equalsIgnoreCase(CommunicationKeys.META_ADD_PLAYER)) {
					this.addPlayer(value);
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_START)) {
					this.startGame(Integer.parseInt(value));
					this.refreshName();
					// Disable ready-checkbox
					((Checkbox) this.settings.getContentable("Ready")).setWriteable(false);
				} else if (action.equalsIgnoreCase(CommunicationKeys.META_SET_NAME)) {
					this.setName(value);
				} else if (action.equalsIgnoreCase(CommunicationKeys.META_GAME_FINISHED)) {
					// Nothing to do, this just triggers an previous disconnect()-call on
					// this.thread
				} else if (action.equalsIgnoreCase(CommunicationKeys.META_DISCONNECT)) {
					this.game.disconnect(value);
				} else if (action.startsWith(CommunicationKeys.GAME_ACTION)) {
					this.game.applyAction(new UserInputOverwrite(value));
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_SHUFFLE)) {
					if (value.startsWith(CommunicationKeys.VALUE_GAME_INIT)) {
						this.game.initialShuffle(value);
					} else if (value.startsWith(CommunicationKeys.VALUE_REFILL_DECK)) {
						this.game.refillShuffle(value);
					} else if (value.startsWith(CommunicationKeys.VALUE_SHUFFLE_DRAFT)) {
						this.game.draftShuffle(value);
					}
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_DRAFT)) {
					this.game.draftAction(new UserInputOverwrite(value));
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_DEAL_CARDS)) {
					this.game.dealCardsToPlayers();
					this.deckViewer.hide();
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_NEXT_ROUND)) {
					this.game.nextRound();
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_VOTE_NEXT_ROUND)) {
					this.game.voteNextRound(value);
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_INCOME_DONE)) {
					this.game.incomeFinished(value);
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_ATTACK)) {
					this.game.attack(new UserInputOverwrite(value));
				} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_CONTROL)) {
					this.game.control(new UserInputOverwrite(value));
				} else {
					Log.warn("Recieved unrecognized action: " + action);
				}
			} else {
				Log.warn("Recieved unrecognized action: " + action);
			}
		}
		synchronized (this.packetBuffer) {
			this.packetBuffer.remove(line);
		}
	}

	/**
	 * Inform all clients about a game event
	 * <p>
	 * This will issue a call of {@link Game#applyAction(UserInputOverwrite)} on all
	 * clients connected to the game
	 * 
	 * @param action the game event
	 */
	public void informAllClients_Action(UserInputOverwrite action) {
		this.sendToServer(CommunicationKeys.GAME_ACTION + CommunicationKeys.SEPERATOR_MAIN + action.getCode());
	}

	/**
	 * Inform all clients about a shuffle of a deck of cards that has happened
	 * <p>
	 * This will issue a call of {@link Game#initialShuffle(String)},
	 * {@link Game#draftShuffle(String)} or {@link Game#refillShuffle(String)} on
	 * all clients connected to the game
	 * 
	 * @param shuffle the shuffle transcribed as {@link String}
	 */
	public void informAllClients_Shuffle(String shuffle) {
		this.sendToServer(CommunicationKeys.GAME_SHUFFLE + CommunicationKeys.SEPERATOR_MAIN + shuffle);
	}

	/**
	 * Inform all clients about a draft event
	 * <p>
	 * This will issue a call of {@link Game#draftAction(UserInputOverwrite)} on all
	 * clients connected to the game
	 * 
	 * @param action the draft event transcribed as {@link String}
	 */
	public void informAllClients_Draft(String draft) {
		this.sendToServer(CommunicationKeys.GAME_DRAFT + CommunicationKeys.SEPERATOR_MAIN + draft);
	}

	/**
	 * Inform all clients that they should deal the cards to all players
	 * <p>
	 * This will issue a call of {@link Game#dealCardsToPlayers()} on all clients
	 * connected to the game
	 */
	public void informAllClients_DealCards() {
		this.sendToServer(CommunicationKeys.GAME_DEAL_CARDS + CommunicationKeys.SEPERATOR_MAIN + this.id);
	}

	/**
	 * Inform all clients that they should start the next round of the game
	 * <p>
	 * This will issue a call of {@link Game#nextRound()} on all clients connected
	 * to the game
	 */
	public void informAllClients_NextRound() {
		this.sendToServer(CommunicationKeys.GAME_NEXT_ROUND + CommunicationKeys.SEPERATOR_MAIN + this.id);
	}

	/**
	 * Inform all clients that this client has finished everything in the current
	 * round and likes to start the next round
	 * <p>
	 * The {@link #id} of this {@link GameClient} is transmitted for identification
	 * <p>
	 * This will issue a call of {@link Game#voteNextRound(String)} on all clients
	 * connected to the game
	 */
	public void informAllClients_VoteNextRound() {
		this.sendToServer(CommunicationKeys.GAME_VOTE_NEXT_ROUND + CommunicationKeys.SEPERATOR_MAIN + this.id);
	}

	/**
	 * Inform all clients that this client has finished everything in the current
	 * income step
	 * <p>
	 * The {@link #id} of this {@link GameClient} is transmitted for identification
	 * <p>
	 * This will issue a call of {@link Game#incomeFinished(String)} on all clients
	 * connected to the game
	 */
	public void informAllClients_IncomeDone() {
		this.sendToServer(CommunicationKeys.GAME_INCOME_DONE + CommunicationKeys.SEPERATOR_MAIN + this.id);
	}

	/**
	 * Inform all clients about an attack game event
	 * <p>
	 * This will issue a call of {@link Game#attack(UserInputOverwrite)} on all
	 * clients connected to the game
	 * 
	 * @param action the attack game event
	 */
	public void informAllClients_Attack(UserInputOverwrite action) {
		this.sendToServer(CommunicationKeys.GAME_ATTACK + CommunicationKeys.SEPERATOR_MAIN + action);
	}

	/**
	 * Inform all clients about a game control event
	 * <p>
	 * This will issue a call of {@link Game#control(UserInputOverwrite)} on all
	 * clients connected to the game
	 * 
	 * @param action the game control event
	 */
	public void informAllClients_Control(UserInputOverwrite control) {
		this.sendToServer(CommunicationKeys.GAME_CONTROL + CommunicationKeys.SEPERATOR_MAIN + control);
	}

	/**
	 * Inform the {@link Server} that the game has finished
	 * <p>
	 * This will disconnect this client from the server and vice versa
	 * <p>
	 * This will issue a call of {@link ListeningThread#disconnect()} for this
	 * {@link #thread} and the corresponding {@link ListeningThread} of the
	 * {@link Server}
	 * <p>
	 * Also exports the final statistics of the game
	 * 
	 * @param action the game control event
	 */
	public void gameFinished(Player winner) {
		this.thread.disconnect();
		this.sendToServer(CommunicationKeys.META_GAME_FINISHED + CommunicationKeys.SEPERATOR_MAIN + winner.getName());
		this.statistics.exportStatistics();
	}

	/**
	 * Set the name of a player
	 * 
	 * @param value "PLAYER_ID" + {@link CommunicationKeys#SEPERATOR_PARTS} +
	 *              "NEW_NAME"
	 */
	private void setName(String value) {
		String[] split = value.split(CommunicationKeys.SEPERATOR_PARTS);
		this.game.changeName(Integer.parseInt(split[0]), split[1]);
	}

	/**
	 * Start the game with a given number of players
	 * <p>
	 * Hides the {@link #settings} dialog
	 * 
	 * @param numberPlayers
	 */
	private void startGame(int numberPlayers) {
		this.settings.hide();
		// this.loadSaveButton.setText("Save game");
		this.game.start(numberPlayers, this.id);
	}

	/**
	 * Add a player to the {@link #game} via {@link Game#addPlayer(String)}
	 * 
	 * @param value the name of the player to add
	 */
	private void addPlayer(String value) {
		Log.info("Adding player " + value);
		this.game.addPlayer(value);
	}

	/**
	 * Connect to the {@link Server}
	 * 
	 * @return <code>true</code> if the connection was successfully established
	 */
	private boolean connectToServer() {
		String host = this.settings.getContentable("Serveradress").getContent(),
				name = this.settings.getContentable("Name").getContent();
		try {
			if (host.length() == 0 || name.length() == 0) {
				Log.info("Host oder Name nicht angegeben");
				return false;
			}

			Log.info("Versuche zum Server " + host + " zu verbinden");

			Socket socket = new Socket(host, CommunicationKeys.SERVER_PORT);

			Log.info("Verbindung hergestellt - Starte Initialisierung");

			// Streams öffnen
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), false);

			// Auf Anfragen warten
			String inLine = br.readLine();
			if (inLine != null) {
				int number = Integer.parseInt(inLine);
				if (number == -1) {
					Log.info("Server does not accept any more clients");
					this.socket = null;
					socket.close();
					return false;
				} else {
					Log.info("Server accepted client");
					this.id = number;
					// Player empfangen
					while (true) {
						Log.info("Receive");
						inLine = br.readLine();
						Log.info("Received: " + inLine);
						if (inLine.startsWith(CommunicationKeys.META_CONNECT_FINISH)) {
							break;
						}
						String[] split = inLine.split(CommunicationKeys.SEPERATOR_MAIN);
						this.addPlayer(split[3].split(CommunicationKeys.SEPERATOR_PARTS)[1]);
					}
					this.sendReady();

					// Socket abspeichern und ListeningThread starten
					this.socket = socket;
					this.thread = new ListeningThread(this.socket, this, CommunicationKeys.MARKER_SERVER,
							CommunicationKeys.MARKER_CLIENT);
					this.thread.start();

					pw.println(CommunicationKeys.META_CONNECT_FINISH);
					pw.flush();

					Log.info("Initialisierung abgeschlossen");
					Log.info("Connected to Server as Client " + this.id + " on Port " + CommunicationKeys.SERVER_PORT);

					this.writer = pw;

					// Textfelder uneditierbar machen, wenn kein Fehler aufgetreten ist
					((TextField) this.settings.getContentable("Serveradress")).setWriteable(false);

					// Enable ready-checkbox
					((Checkbox) this.settings.getContentable("Ready")).setWriteable(true);

					return true;
				}
			}
		} catch (UnknownHostException e) {
			Log.warn("Could not connect to server " + host + " because host is not known: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.warn("Could not connect to server " + host + " because an IOException occured: "
					+ e.getLocalizedMessage());
		}
		return false;
	}

	/**
	 * @return <code>true</code> if connected to a {@link Server}
	 */
	public boolean isConnected() {
		if (this.socket != null) {
			return this.socket.isConnected() && !this.socket.isClosed();
		} else {
			return false;
		}
	}

	/**
	 * Send a message to the {@link Server}
	 * 
	 * @param message the message to send which is unified via
	 *                {@link #UnifyInfo(String)} before sending
	 */
	public void sendToServer(String message) {
		if (this.isConnected()) {
			message = this.UnifyInfo(message);
			Log.info(this.id + " sending " + message);
			this.writer.println(message);
			this.writer.flush();
		}
	}

	/**
	 * Unify the start and end of a message
	 * 
	 * @param message the message to unify
	 * @return the unified message
	 */
	private String UnifyInfo(String message) {
		if (!message.startsWith(CommunicationKeys.MARKER_CLIENT + CommunicationKeys.SEPERATOR_MAIN + this.id
				+ CommunicationKeys.SEPERATOR_MAIN)) {
			message = CommunicationKeys.MARKER_CLIENT + CommunicationKeys.SEPERATOR_MAIN + this.id
					+ CommunicationKeys.SEPERATOR_MAIN + message;
		}
		if (!message.endsWith(CommunicationKeys.SEPERATOR_END)) {
			message = message + CommunicationKeys.SEPERATOR_END;
		}
		return message;
	}

	/**
	 * Send the {@link #ready} status of this client to the server
	 */
	private void sendReady() {
		this.sendToServer(CommunicationKeys.META_SET_READY + CommunicationKeys.SEPERATOR_MAIN + this.ready);
	}

	/**
	 * Refresh the name of this client by sending it to the {@link Server}
	 */
	private void refreshName() {
		if (this.settings.getContentable("Name").getContent().length() > 0) {
			this.sendToServer(CommunicationKeys.META_SET_NAME + CommunicationKeys.SEPERATOR_MAIN
					+ this.settings.getContentable("Name").getContent());
		}
	}

	// Allgemeines

	/**
	 * @return the {@link #id} this client received from the {@link Server}
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @return the {@link GameState} which has created this client
	 */
	public GameState getGameState() {
		return this.parent;
	}

	/**
	 * Set the detailed card view to a given {@link Image} if no detailed view is
	 * currently set
	 * 
	 * @param image the image to display in large
	 */
	public void setDetailedCard(String image) {
		if (this.detailedCard == null) {
			this.detailedCard = image;
			this.detailedDeck = null;
			this.detailedDescription = "";
		}
	}

	/**
	 * Set the detailed card view to a given deck of {@link Image}s if no detailed
	 * view is currently set
	 * 
	 * @param images the images to display in large
	 */
	@SuppressWarnings("unchecked")
	public <T extends ImageDisplay> void setDetailedDeck(ArrayList<T> images, String description) {
		if (this.detailedDeck == null) {
			this.detailedDeck = (ArrayList<ImageDisplay>) images;
			this.detailedCard = null;
			this.detailedDescription = description;
		}
	}

	public GameStatistics getStatistics() {
		return this.statistics;
	}

	public boolean isActiveClient() {
		return this.parent.isActive(this);
	}
}
