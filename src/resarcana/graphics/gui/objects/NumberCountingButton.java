package resarcana.graphics.gui.objects;

import java.util.ArrayList;

import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.InterfacePart;
import resarcana.math.Rectangle;

public class NumberCountingButton extends ImageButton implements Informable, Contentable {

	private int min, max, step;
	private int curNumber;

	private ArrayList<ContentListener> listener = new ArrayList<ContentListener>();

	public NumberCountingButton(Rectangle box, String image, int min, int max, int start, int step) {
		super(InterfaceFunctions.INTERFACE_NUMBER_COUNTER, box, image, Input.KEY_ENTER, "" + start);
		this.curNumber = start;
		this.min = min;
		this.max = max;
		this.step = step;
		this.addInformable(this);
	}

	public NumberCountingButton(NumberCountingButton button, float scale) {
		super(button, scale);
		this.curNumber = button.curNumber;
		this.min = button.min;
		this.max = button.max;
		this.step = button.step;
		this.addInformable(this);
	}

	private void moveBack() {
		if (this.curNumber > this.min) {
			this.curNumber = this.curNumber - this.step;
			this.updateText();
		}
	}

	private void updateText() {
		this.setText(this.curNumber + "");
		for (ContentListener cL : this.listener) {
			cL.contentChanged(this);
		}
	}

	private void moveForth() {
		if (this.curNumber < this.max) {
			this.curNumber = this.curNumber + this.step;
			this.updateText();
		}
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object == this) {
			if (this.getStatus() == Mousestatus.STATUS_LEFT_PRESSED) {
				this.moveForth();
			}
			if (this.getStatus() == Mousestatus.STATUS_RIGHT_PRESSED) {
				this.moveBack();
			}
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}

	public int getValue() {
		return this.curNumber;
	}

	@Override
	public void setContent(String newContent) {
		this.curNumber = (int) (Float.parseFloat(newContent.trim()));
		this.updateText();
	}

	@Override
	public void addContentListener(ContentListener listener) {
		this.listener.add(listener);
	}

	@Override
	public String getContent() {
		return this.curNumber + "";
	}

	public void setMaximum(int nmax) {
		if (nmax >= this.min) {
			this.max = nmax;
			if (this.curNumber > nmax) {
				this.curNumber = nmax;
				this.updateText();
			}
		}
	}

	public void setMinimum(int nmin) {
		if (nmin <= this.max) {
			this.min = nmin;
			if (this.curNumber < nmin) {
				this.curNumber = nmin;
				this.updateText();
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
			this.updateText();
		} else {
			Log.warn("Trying to change selector out of bounds: " + number + " !in [" + this.min + "," + this.max + "]");
		}
	}

	@Override
	public InterfacePart getInterfacePart() {
		return this;
	}
}
