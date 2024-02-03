package resarcana.game;

import javax.swing.JOptionPane;

import resarcana.utils.JarClassLoader;

public class ResArcanaLauncher {

	/**
	 * @param args Kommandozeilenargumente werden an {@link ResArcana}
	 *             weitergegeben.
	 */
	public static void main(String[] args) {

		// Der JarClassLoader kann auch native Bibliotheken aus einem Archiv
		// laden, wenn das nötig ist.
		try {
			new JarClassLoader().invokeMain("resarcana.game.Launcher", args);
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"An unexpected error occured. Please check the log file.", "Res Arcana - Unexpected error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
