package resarcana.communication;

public class CommunicationKeys {

	public static final int SERVER_PORT = 4100;

	public static final String SEPERATOR_MAIN = "/";
	public static final String SEPERATOR_PARTS = "#";
	public static final String SEPERATOR_VALUES = "~";
	public static final String SEPERATOR_END = "%";
	public static final String SEPERATOR_NAME = "_";

	public static final String MARKER_SERVER = "Server";
	public static final String MARKER_CLIENT = "Client";

	public static final String META_CONNECT_NEW = "ConnectNew";
	public static final String META_CONNECT_LOADED = "ConnectLoaded";
	public static final String META_CONNECT_FINISH = "FinishConnect";
	public static final String META_ADD_PLAYER = "AddPlayer";
	public static final String META_SET_NAME = "SetName";
	public static final String META_SET_READY = "Ready";
	public static final String META_GAME_FINISHED = "GameFinished";
	public static final String META_DISCONNECT = "Disconnect";

	public static final String GAME_START = "Start";
	public static final String GAME_RESUME = "Resume";
	public static final String GAME_SHUFFLE = "Shuffle";
	public static final String GAME_DEAL_CARDS = "DealCards";
	public static final String GAME_DRAFT = "Draft";
	public static final String GAME_ACTION = "Action";
	public static final String GAME_NEXT_ROUND = "StartNextRound";
	public static final String GAME_VOTE_NEXT_ROUND = "VoteNextRound";
	public static final String GAME_INCOME_DONE = "IncomeDone";
	public static final String GAME_ATTACK = "Attack";
	public static final String GAME_CONTROL = "Control";

	public static final String VALUE_GAME_INIT = "GameInit";
	public static final String VALUE_REFILL_DECK = "RefillDeck";
	public static final String VALUE_START_DRAFT = "StartDraft";
	public static final String VALUE_SHUFFLE_DRAFT = "ShuffleDraft";

	public static final String VALUE_CHANGE_COLLECT = "ChangeCollect";

	public static final String VALUE_ARTIFACTS = "Artifacts";
	public static final String VALUE_MAGIC_ITEMS = "MagicItems";
	public static final String VALUE_MAGES = "Mages";
	public static final String VALUE_MONUMENTS = "Monuments";
	public static final String VALUE_POWERPLACES = "PowerPlaces";
}
