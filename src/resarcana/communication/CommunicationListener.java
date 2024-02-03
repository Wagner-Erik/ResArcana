package resarcana.communication;

public interface CommunicationListener {

	public void inform(String line);

	public void disconnected(ListeningThread listeningThread);
}
