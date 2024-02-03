package resarcana.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class Server implements CommunicationListener {

	private final boolean automaticStart;

	private ServerThread accepting;
	private volatile ArrayList<ClientInfo> clients = new ArrayList<ClientInfo>();

	private boolean gameStarted = false;
	private int totalNumberOfGames, numberOfGames;

	public Server(boolean automaticStart, int totalNumberOfGames) throws IOException {

		this.automaticStart = automaticStart;
		this.totalNumberOfGames = totalNumberOfGames;
		this.numberOfGames = 0;

		// Print all ip's available on this machine
		String ip;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					ServerLog.info(iface.getDisplayName() + " " + ip);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		this.initServerSocket();
	}

	public void startServer() {
		this.accepting.start();
		this.serverLoop();
	}

	private void initServerSocket() throws IOException {
		this.accepting = new ServerThread(this, new ServerSocket(CommunicationKeys.SERVER_PORT, 4, null));
	}

	/**
	 * Main loop of the server to check if a game is still running and to start a
	 * new one if the current game has finished
	 */
	private void serverLoop() {
		while (this.numberOfGames < this.totalNumberOfGames && !this.accepting.isClosed()) {
			ServerLog
					.info("Ready for a new game --- " + (this.totalNumberOfGames - this.numberOfGames) + " games left");
			while (!this.allClientsDisconnected()) {
				// Let server sleep for a while before rechecking
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					ServerLog.warn("Server interrupted while sleeping in game loop " + e);
				}
				if (this.accepting.isClosed()) {
					ServerLog.info("ServerSocket closed, shutting down server");
					break;
				}
			}
			if (this.allClientsDisconnected()) {
				ServerLog.info("Game finished and all clients have disconnected");
			}
			ServerLog.info("Clearing current clients");
			// Make the server ready for a new game
			synchronized (this.clients) {
				this.clients.clear();
			}
			this.gameStarted = false;
		}
		ServerLog.info("Shutting down server");
		// Close ServerSocket
		this.accepting.closeSocket();
		// List should be clear but in theory a new Client could have just been added
		synchronized (this.clients) {
			for (ClientInfo client : this.clients) {
				client.disconnect();
			}
		}
		ServerLog.info("Bye");
	}

	@SuppressWarnings("unchecked")
	public ArrayList<ClientInfo> getClients() {
		synchronized (this.clients) {
			return (ArrayList<ClientInfo>) this.clients.clone();
		}
	}

	public void addClient(ClientInfo clientInfo) {
		synchronized (this.clients) {
			this.clients.add(clientInfo);
		}
		this.sendToAllClients(
				CommunicationKeys.META_ADD_PLAYER + CommunicationKeys.SEPERATOR_MAIN + (this.clients.size() - 1)
						+ CommunicationKeys.SEPERATOR_PARTS + this.clients.get((this.clients.size() - 1)).getName());
	}

	@Override
	public void inform(String line) {
		synchronized (this.clients) {
			ServerLog.info("Recieved: " + line);
			String[] split = line.split(CommunicationKeys.SEPERATOR_END)[0].split(CommunicationKeys.SEPERATOR_MAIN);
			if (split.length == 4) {
				if (split[0].equalsIgnoreCase(CommunicationKeys.MARKER_CLIENT)) {
					int client = Integer.parseInt(split[1]);
					String action = split[2];
					String value = split[3];
					if (client < this.clients.size() && client >= 0) {
						if (action.equalsIgnoreCase(CommunicationKeys.META_SET_NAME)) {
							ServerLog.info("Change name of " + client + " to " + value);
							this.setClientName(client, value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.META_SET_READY)) {
							this.clients.get(client).setReady(Boolean.parseBoolean(value));
							if (this.automaticStart) {
								this.startGame();
							}
						} else if (action.equalsIgnoreCase(CommunicationKeys.META_GAME_FINISHED)) {
							ServerLog.info("Requesting disconnect of client " + client);
							this.sendToAllClients(
									CommunicationKeys.META_GAME_FINISHED + CommunicationKeys.SEPERATOR_MAIN + value);
							this.clients.get(client).disconnect();
						} else if (action.equalsIgnoreCase(CommunicationKeys.META_DISCONNECT)) {
							ServerLog.info("Requesting disconnect of client " + client);
							this.sendToAllClients(
									CommunicationKeys.META_DISCONNECT + CommunicationKeys.SEPERATOR_MAIN + value);
							this.clients.get(client).disconnect();
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_ACTION)) {
							this.sendToAllClients(CommunicationKeys.GAME_ACTION + CommunicationKeys.SEPERATOR_PARTS
									+ split[1] + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_RESUME)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_RESUME + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_SHUFFLE)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_SHUFFLE + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_DRAFT)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_DRAFT + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_DEAL_CARDS)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_DEAL_CARDS + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_NEXT_ROUND)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_NEXT_ROUND + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_VOTE_NEXT_ROUND)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_VOTE_NEXT_ROUND + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_INCOME_DONE)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_INCOME_DONE + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_ATTACK)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_ATTACK + CommunicationKeys.SEPERATOR_MAIN + value);
						} else if (action.equalsIgnoreCase(CommunicationKeys.GAME_CONTROL)) {
							this.sendToAllClients(
									CommunicationKeys.GAME_CONTROL + CommunicationKeys.SEPERATOR_MAIN + value);
						} else {
							ServerLog.error("Unkwon input from Client " + client + ": " + action
									+ CommunicationKeys.SEPERATOR_MAIN + value);
						}
					}
				}
			}
		}
	}

	public boolean startGame() {
		if (!this.hasGameStarted()) {
			if (this.allClientsReady()) {
				// Transmit number of clients to make sure everyone has the same player number
				ServerLog.info("Starting game");
				this.numberOfGames++;
				synchronized (this.clients) {
					this.sendToAllClients(
							CommunicationKeys.GAME_START + CommunicationKeys.SEPERATOR_MAIN + this.clients.size());
				}
				this.gameStarted = true;
				return true;
			} else {
				ServerLog.info("Not all clients ready");
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean allClientsReady() {
		synchronized (this.clients) {
			if (this.clients.size() == 0) { // No use in starting the game without clients
				return false;
			}
			for (int i = 0; i < this.clients.size(); i++) {
				if (!this.clients.get(i).isReady()) {
					return false;
				}
			}
			return true;
		}
	}

	private boolean allClientsDisconnected() {
		synchronized (this.clients) {
			if (this.clients.size() == 0) { // No client has ever connected
				return false;
			}
			for (int i = 0; i < this.clients.size(); i++) {
				if (!this.clients.get(i).hasDisconnected()) {
					return false;
				}
			}
			return true;
		}
	}

	private void setClientName(int client, String name) {
		synchronized (this.clients) {
			this.clients.get(client).setName(name);
			this.sendToAllClients(CommunicationKeys.META_SET_NAME + CommunicationKeys.SEPERATOR_MAIN + client
					+ CommunicationKeys.SEPERATOR_PARTS + name);
		}
	}

	private void sendToAllClients(String message) {
		synchronized (this.clients) {
			if (message.split(CommunicationKeys.SEPERATOR_MAIN).length == 2) {
				message = CommunicationKeys.MARKER_SERVER + CommunicationKeys.SEPERATOR_MAIN + "true"
						+ CommunicationKeys.SEPERATOR_MAIN + message;
			}
			if (!message.endsWith(CommunicationKeys.SEPERATOR_END)) {
				message += CommunicationKeys.SEPERATOR_END;
			}
			ServerLog.info("Sending to " + this.clients.size() + " clients: " + message);
			for (int i = 0; i < this.clients.size(); i++) {
				if (!this.clients.get(i).hasDisconnected()) {
					PrintWriter writer = this.clients.get(i).getWriter();
					writer.println(message);
					writer.flush();
				}
			}
		}
	}

	public boolean hasGameStarted() {
		return this.gameStarted;
	}

	public void stop() {
		this.accepting.closeSocket();
	}

	@Override
	public void disconnected(ListeningThread listeningThread) {
		for (ClientInfo clientInfo : this.clients) {
			if (clientInfo.getThread() == listeningThread) {
				this.sendToAllClients(
						CommunicationKeys.META_DISCONNECT + CommunicationKeys.SEPERATOR_MAIN + clientInfo.id);
			}
		}
	}
}
