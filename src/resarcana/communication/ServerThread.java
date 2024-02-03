package resarcana.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Ein Thread, der für ein Informable auf Antworten von einem Socket wartet
 * 
 * @author Erik Wagner
 * 
 */
public class ServerThread extends Thread {

	private final Server parent;
	private final ServerSocket server;

	private boolean closing = false;

	public ServerThread(Server parent, ServerSocket server) {
		this.parent = parent;
		this.server = server;
	}

	private void acceptClients() throws SocketException, IOException {
		// Auf den Clienten warten
		Socket client;
		client = this.server.accept();

		ServerLog.info("Client found");

		// Streams öffnen, um den Namen des Clienten zu empfangen und ihm
		// eine ID zu senden
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		PrintWriter pw = new PrintWriter(client.getOutputStream(), false);

		ArrayList<ClientInfo> clients = this.parent.getClients();
		int id = -1;
		// Server does not test for the amount of players
		// if to many players would be connected, the excess ones can only spectate
		if (!this.parent.hasGameStarted()) {
			// übermitteln
			id = clients.size();
			ServerLog.info("Client " + id + " accepted");
		}
		pw.println("" + id);
		pw.flush();
		if (id != -1) {
			for (int i = 0; i < clients.size(); i++) {
				pw.println(CommunicationKeys.MARKER_SERVER + CommunicationKeys.SEPERATOR_MAIN + "false"
						+ CommunicationKeys.SEPERATOR_MAIN + CommunicationKeys.META_ADD_PLAYER
						+ CommunicationKeys.SEPERATOR_MAIN + i + CommunicationKeys.SEPERATOR_PARTS
						+ clients.get(i).getName());
				pw.flush();
			}
			pw.println(CommunicationKeys.META_CONNECT_FINISH);
			pw.flush();

			// Wait for confirmation from client
			String line = br.readLine();
			if (!line.startsWith(CommunicationKeys.META_CONNECT_FINISH)) {
				ServerLog.warn("Recieved unexpected line from client: " + line);
			}

			// ListeningThread erstellen und starten
			ListeningThread thread = new ListeningThread(client, this.parent, CommunicationKeys.MARKER_CLIENT
					+ CommunicationKeys.SEPERATOR_MAIN + id + CommunicationKeys.SEPERATOR_MAIN,
					CommunicationKeys.MARKER_SERVER);
			thread.start();

			// Client ablegen
			this.parent.addClient(new ClientInfo(id, "Client " + id, thread, pw));
			ServerLog.info("Client " + id + " added");
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.acceptClients();
			} catch (SocketException e) {
				if (!(e.getLocalizedMessage().equalsIgnoreCase("socket closed") && this.closing)) {
					ServerLog.error("SocketException while accepting clients: " + e.getLocalizedMessage());
					// e.printStackTrace();
				}
			} catch (IOException e) {
				ServerLog.error("IOException while accepting clients: " + e.getLocalizedMessage());
				// e.printStackTrace();
			} finally {
				if (this.closing) {
					break;
				} else {
					if(this.server.isClosed()) {
						this.closing = true;
					}
				}
			}
		}
	}

	public void closeSocket() {
		this.closing = true;
		try {
			ServerLog.info("Closing ServerSocket");
			this.server.close();
		} catch (IOException e) {
			ServerLog.error("Could not close ServerSocket");
		}
	}

	public boolean isClosed() {
		return this.closing;
	}
}
