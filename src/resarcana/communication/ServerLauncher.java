package resarcana.communication;

import java.io.IOException;

public class ServerLauncher {

	public static void main(String[] args) {
		int numberOfGames = 1;
		if (args.length == 1) {
			numberOfGames = Integer.parseInt(args[0]);
		}
		try {
			Server s = new Server(true, numberOfGames);
			s.startServer();
		} catch (IOException e) {
			ServerLog.error("IOException from server: " + e);
			e.printStackTrace();
		}
	}

}
