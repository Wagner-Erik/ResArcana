package resarcana.communication;

import java.io.PrintWriter;

public class ClientInfo {

	public final int id;
	private String name;
	private boolean ready = false;
	private final ListeningThread thread;
	private final PrintWriter writer;

	public ClientInfo(int id, String name, ListeningThread thread, PrintWriter writer) {
		this.id = id;
		this.name = name;
		this.thread = thread;
		this.writer = writer;
	}

	public ListeningThread getThread() {
		return this.thread;
	}

	public PrintWriter getWriter() {
		return this.writer;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isReady() {
		return this.ready;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getName() {
		return this.name;
	}

	public void disconnect() {
		this.thread.disconnect();
	}

	public boolean hasDisconnected() {
		return this.thread.hasDisconnected();
	}
}
