package resarcana.graphics.gui;

public interface Contentable {

	/**
	 * @return Der "Inhalt" dieses Objekts (z.B. eine eingestellte Zahl oder ein
	 *         eingegebener Text)
	 */
	public String getContent();

	public void setContent(String newContent);

	public void addContentListener(ContentListener listener);

	public InterfacePart getInterfacePart();
}
