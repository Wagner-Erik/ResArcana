package resarcana.graphics.gui.objects;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.InterfacePart;
import resarcana.graphics.gui.container.GridContainer;
import resarcana.utils.Parameter;

/**
 * Eine Zahlenauswahl für Dialoge u.Ä.
 * 
 * @author e.wagner
 */
public class NumberSelection extends GridContainer implements Informable, Contentable {

	private static final int DEFAULT_STEP = Parameter.GUI_NUMBERSELECTION_DEFAULTSTEP;

	private TextField textField;
	private int curNumber;
	private int min, max, step;

	private ArrayList<ContentListener> listener = new ArrayList<ContentListener>();

	public NumberSelection(int min, int max, int start, int step) {
		super(1, 3);
		this.min = min;
		this.max = max;
		if (step > 0) { // Keine negativen Steps und nicht 0
			this.step = step;
		} else {
			this.step = DEFAULT_STEP;
		}
		this.curNumber = start;
		SquareButton a = new SquareButton(InterfaceFunctions.INTERFACE_NUMBER_SELECTION_BACK,
				"interface-icons/back-arrow.png");
		SquareButton b = new SquareButton(InterfaceFunctions.INTERFACE_NUMBER_SELECTION_FORTH,
				"interface-icons/forth-arrow.png");
		a.addInformable(this);
		b.addInformable(this);
		this.textField = new TextField(InterfaceFunctions.INTERFACE_TEXTFIELD);
		this.textField.setWriteable(false);
		this.textField.addInformable(this);
		this.textField.setMinimumText("00");
		this.add(a, 0, 0, MODUS_X_LEFT, MODUS_DEFAULT);
		this.add(b, 0, 2, MODUS_X_RIGHT, MODUS_DEFAULT);
		this.add(this.textField, 0, 1);
		this.updateTextField();
	}

	public NumberSelection(int min, int max) {
		this(min, max, min, DEFAULT_STEP);
	}

	private void moveBack() {
		if (this.curNumber > this.min) {
			this.curNumber = this.curNumber - this.step;
			this.updateTextField();
		}
	}

	private void moveForth() {
		if (this.curNumber < this.max) {
			this.curNumber = this.curNumber + this.step;
			this.updateTextField();
		}
	}

	private void updateTextField() {
		this.textField.setContent("" + this.curNumber);
		for (ContentListener cL : this.listener) {
			cL.contentChanged(this);
		}
	}

	private void transmitTextFieldContent(InterfaceObject object) {
		String content = this.textField.getContent();
		int number = 0;
		if (content.length() > 0) {
			try {
				number = Integer.parseInt(content);
			} catch (NumberFormatException e) {
				// Nichts tun
			}
		}
		if (number >= this.min && number <= this.max) {
			this.curNumber = number;
		}
		this.updateTextField();
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (object.getFunction() == InterfaceFunctions.INTERFACE_NUMBER_SELECTION_FORTH) {
				this.moveForth();
			} else if (object.getFunction() == InterfaceFunctions.INTERFACE_NUMBER_SELECTION_BACK) {
				this.moveBack();
			}
		}
		if (object.getFunction() == InterfaceFunctions.INTERFACE_TEXTFIELD) {
			this.transmitTextFieldContent(object);
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		if (object.getFunction() == InterfaceFunctions.INTERFACE_TEXTFIELD) {
			this.transmitTextFieldContent(object);
		}
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		if (object.getFunction() == InterfaceFunctions.INTERFACE_TEXTFIELD) {
			this.transmitTextFieldContent(object);
		}
	}

	@Override
	public String getContent() {
		return "" + this.curNumber;
	}

	public int getValue() {
		return this.curNumber;
	}

	@Override
	public void setContent(String newContent) {
		this.curNumber = (int) (Float.parseFloat(newContent.trim()));
		this.updateTextField();
	}

	@Override
	public void addContentListener(ContentListener listener) {
		this.listener.add(listener);
	}

	public void setMaximum(int nmax) {
		if (nmax >= this.min) {
			this.max = nmax;
			if (this.curNumber > nmax) {
				this.curNumber = nmax;
				this.updateTextField();
			}
		}
	}

	public void setMinimum(int nmin) {
		if (nmin <= this.max) {
			this.min = nmin;
			if (this.curNumber < nmin) {
				this.curNumber = nmin;
				this.updateTextField();
			}
		}
	}

	public int getMinimum() {
		return this.min;
	}

	public int getMaximum() {
		return this.max;
	}

	public void setStep(int nstep) {
		if (nstep > 0) {
			this.step = nstep;
		}
	}

	public void setValue(int number) {
		if (number >= this.min && number <= this.max) {
			this.curNumber = number;
			this.updateTextField();
		} else {
			Log.warn("Trying to change selector out of bounds: " + number + " !in [" + this.min + "," + this.max + "]");
		}
	}

	@Override
	public InterfacePart getInterfacePart() {
		return this;
	}
}
