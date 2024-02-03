/**
 * 
 */
package resarcana.graphics.gui.container;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.HideableContainer;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.InterfacePart;
import resarcana.graphics.gui.objects.Label;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.NumberSelection;
import resarcana.graphics.gui.objects.TextButton;
import resarcana.graphics.gui.objects.TextField;
import resarcana.math.Vector;

/**
 * Eine Klasse f�r ein Dialog-Feld, das NumberSelections und Textfelder auf
 * schwarzem Hintergrund darstellt
 * 
 * Kann ein und ausgeblendet werden
 * 
 * @author e.wagner
 * 
 */
public class Dialog extends HideableContainer implements Informable {

	private static ArrayList<Dialog> Instances = new ArrayList<Dialog>();

	private AdvancedGridContainer mainCon, curCon;
	private SpecialBackgroundContainer background;
	private ArrayList<DialogPart> contents = new ArrayList<DialogPart>();
	private ArrayList<TextButton> buttons = new ArrayList<TextButton>();
	private ArrayList<Informable> informables = new ArrayList<Informable>();
	private ArrayList<ContentListener> contentListener = new ArrayList<ContentListener>();

	/**
	 * 
	 */
	public Dialog() {
		super();

		TextButton closeButton = new TextButton(InterfaceFunctions.DIALOG_CLOSE, Input.KEY_ESCAPE, "Dialog schließen");
		closeButton.addInformable(this);
		this.buttons.add(closeButton);

		this.mainCon = new AdvancedGridContainer(1, 1, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, 40, 40);
		this.background = new SpecialBackgroundContainer(this.mainCon, true, true, true, true, 1.f);
		this.add(this.background, Vector.ZERO);

		this.updateCon();
		Dialog.Instances.add(this);
	}

	public static boolean isAnyDialogActive() {
		for (Dialog obj : Dialog.Instances) {
			if (obj.isActive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Gibt zurück, ob der Dialog aktiv ist (entspricht, ob er angezeigt
	 *         ist)
	 */
	public boolean isActive() {
		return this.isShown();
	}

	public int getNumberOfParts() {
		// Zusammengezählte Zahl aus Contentables und Buttons abzüglich des
		// closeButtons
		return this.contents.size() + this.buttons.size() - 1;
	}

	/**
	 * @return Eine Hash-Map, die nach Bezeichnungen geordnet die Werte der
	 *         Dialog-Teile (als Strings) enthält
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getContents() {
		HashMap<String, String> re = new HashMap<String, String>();
		for (DialogPart part : (ArrayList<DialogPart>) this.contents.clone()) {
			re.put(part.name, part.content.getContent());
		}
		return re;
	}

	public void addPart(DialogPart dialogPart) {
		this.contents.add(dialogPart);
		for (int i = 0; i < this.contentListener.size(); i++) {
			dialogPart.content.addContentListener(this.contentListener.get(i));
		}
		this.updateCon();
	}

	private void updateCon() {
		AdvancedGridContainer con = new AdvancedGridContainer(this.contents.size() + this.buttons.size() + 1, 2,
				AdvancedGridContainer.MODUS_DEFAULT, AdvancedGridContainer.MODUS_DEFAULT, 3.0f, 3.0f);
		for (int i = 0; i < this.contents.size(); i++) {
			con.add(new Label(this.contents.get(i).name, 1, Color.black), i, 0, GridContainer.MODUS_X_LEFT,
					GridContainer.MODUS_DEFAULT);
			con.add(this.contents.get(i).part, i, 1, GridContainer.MODUS_X_LEFT, GridContainer.MODUS_DEFAULT);
		}
		for (int i = 1; i < this.buttons.size(); i++) {
			con.add(this.buttons.get(i), i + this.contents.size(), 0, GridContainer.MODUS_X_LEFT,
					GridContainer.MODUS_DEFAULT);
		}
		con.add(this.buttons.get(0), this.buttons.size() + this.contents.size(), 1,
				GridContainer.MODUS_X_RIGHT, GridContainer.MODUS_DEFAULT);
		this.changeCon(con);
	}

	private void changeCon(AdvancedGridContainer gridContainer) {
		this.mainCon.remove(this.curCon);
		this.mainCon.add(gridContainer, 0, 0);
		this.curCon = gridContainer;
	}

	/**
	 * Fügt dem Dialog ein Contentable hinzu
	 * 
	 * @param part Das Contentable
	 * @param name Die Bezeichnung, unter der das Contentable abgelegt wird
	 */
	public void addContentable(Contentable part, String name) {
		this.addPart(new DialogPart(part, name));
	}

	/**
	 * Fügt dem Dialog eine Zahlenauswahl hinzu
	 * 
	 * @param name Bezeichnung der Zahlenauswahl
	 * @param min  Der minimale Wert
	 * @param max  Der maximale Wert
	 */
	public void addNumberSelection(String name, int min, int max) {
		this.addPart(new DialogPart(new NumberSelection(min, max), name));
	}

	/**
	 * Fügt dem Dialog ein Textfeld hinzu
	 * 
	 * @param name Bezeichnung des Textfeldes
	 */
	public void addTextField(String name) {
		this.addPart(new DialogPart(new TextField(InterfaceFunctions.INTERFACE_TEXTFIELD), name));
	}

	public TextButton addTextButton(InterfaceFunction function, String text) {
		TextButton b = new TextButton(function, text);
		this.buttons.add(b);
		for (int i = 0; i < this.informables.size(); i++) {
			b.addInformable(this.informables.get(i));
		}
		this.updateCon();
		return b;
	}

	public void addInformableToAllButtons(Informable toAdd) {
		for (int i = 0; i < this.buttons.size(); i++) {
			if (!this.informables.contains(toAdd)) {
				this.buttons.get(i).addInformable(toAdd);
				this.informables.add(toAdd);
			}
		}
	}

	public void addContentListenerToAllContentables(ContentListener toAdd) {
		for (int i = 0; i < this.contents.size(); i++) {
			if (!this.contentListener.contains(toAdd)) {
				this.contents.get(i).content.addContentListener(toAdd);
			}
		}
		this.contentListener.add(toAdd);
	}

	public void setCloseable(boolean closeable) {
		this.buttons.get(0).setEnabled(closeable);
	}

	/**
	 * 
	 * @param name
	 * @return <code>null</code>, wenn es kein Objekt mit dem Namen gibt
	 */
	@SuppressWarnings("unchecked")
	public Contentable getContentable(String name) {
		for (DialogPart part : (ArrayList<DialogPart>) this.contents.clone()) {
			if (part.name.equals(name)) {
				return part.content;
			}
		}
		Log.error("Item not found");
		return null;
	}

	@SuppressWarnings("unchecked")
	public void selectContentable(String name) {
		for (DialogPart part : (ArrayList<DialogPart>) this.contents.clone()) {
			if (part.name.equals(name)) {
				if (part.part instanceof InterfaceObject) {
					((InterfaceObject) part.part).makeSelected();
				} else {
					Log.error("Can't select " + part.part);
				}
				return;
			}
		}
		Log.error("Item not found");
		return;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getFunction() == InterfaceFunctions.DIALOG_CLOSE
				&& object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			this.hide();
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nichts tun
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nichts tun
	}

	/**
	 * Deaktiviert alle Dialog (blendet sie aus)
	 */
	public static void disableAll() {
		for (Dialog dialog : Instances) {
			dialog.hide();
		}
	}

	@Override
	protected boolean resize() {
		return this.setHitbox(this.background.getHitbox());
	}

}

class DialogPart {

	public final String name;
	public final InterfacePart part;
	public final Contentable content;

	public DialogPart(Contentable content, String name) {
		this.content = content;
		this.part = this.content.getInterfacePart();
		this.name = name;
	}
}