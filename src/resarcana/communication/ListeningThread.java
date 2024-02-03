package resarcana.communication;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Ein Thread, der für ein Informable auf Antworten von einem Socket wartet
 * 
 * @author Erik Wagner
 * 
 */
public class ListeningThread extends Thread {

	private BufferedReader inputStream;
	private PrintStream outputStream;
	private CommunicationListener parent;
	private String informAbout, resend;

	private boolean disconnect = false, disconnected = false;

	public ListeningThread(Socket client, CommunicationListener parent, String informAbout, String resend) {
		try {
			// Input- und Output-Streams erstellen
			this.inputStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.outputStream = new PrintStream(new BufferedOutputStream(client.getOutputStream()), false);
			this.parent = parent;
			this.informAbout = informAbout;
			this.resend = resend;
		} catch (IOException e) {
			ServerLog.error("ListeningThread: Could not created I/O-streams: " + e);
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String inLine;
		try {
			while ((inLine = this.inputStream.readLine()) != null) {
				if (inLine.startsWith(this.resend)) {
					// Bei fälschlich aufgefangenen Kommando vom Server dieses
					// wieder in den Stream leiten
					this.outputStream.println(inLine);
					this.outputStream.flush();
					ServerLog.info("ListeningThread: Resend: " + inLine);
				} else if (inLine.startsWith(this.informAbout)) {
					// Antwort an den Server weiterleiten
					this.parent.inform(inLine);
				} else {
					// Fehlermeldung
					ServerLog.error("ListeningThread: Received unknown message: " + inLine);
				}
				if (this.disconnect) {
					ServerLog.info("ListeningThread: Disconnecting " + this);
					this.inputStream.close();
					this.outputStream.close();
					this.disconnected = true;
					break;
				}
			}
		} catch (SocketException e) {
			this.disconnected = true;
			if (!e.getMessage().equalsIgnoreCase("socket closed")) {
				ServerLog.error("ListeningThread: Error while receiving messages from server: " + e);
			}
		} catch (IOException e) {
			this.disconnected = true;
			ServerLog.error("ListeningThread: Error while receiving messages from server: " + e);
		}
		if (!this.disconnect) {
			this.parent.disconnected(this);
		}
	}

	public void disconnect() {
		ServerLog.info("ListeningThread: Requesting disconnect of " + this);
		this.disconnect = true;
	}

	public boolean hasDisconnected() {
		return this.disconnected;
	}
}
