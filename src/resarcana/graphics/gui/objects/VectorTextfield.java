package resarcana.graphics.gui.objects;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfacePart;
import resarcana.graphics.gui.container.GridContainer;
import resarcana.math.Vector;

public class VectorTextfield extends GridContainer implements Contentable, ContentListener {

	private TextField xField, yField;

	private ArrayList<ContentListener> listener = new ArrayList<ContentListener>();

	public VectorTextfield() {
		super(2, 2);
		this.xField = new TextField(InterfaceFunctions.INTERFACE_TEXTFIELD_VECTOR_X);
		this.yField = new TextField(InterfaceFunctions.INTERFACE_TEXTFIELD_VECTOR_Y);
		this.add(new Label("X:", 1, Color.black), 0, 0, MODUS_X_LEFT, MODUS_DEFAULT);
		this.add(new Label("Y:", 1, Color.black), 1, 0, MODUS_X_LEFT, MODUS_DEFAULT);
		this.add(this.xField, 0, 1, MODUS_X_RIGHT, MODUS_DEFAULT);
		this.add(this.yField, 1, 1, MODUS_X_RIGHT, MODUS_DEFAULT);
		this.xField.addContentListener(this);
		this.yField.addContentListener(this);
	}

	@Override
	public String getContent() {
		return this.xField.getContent() + "|" + this.yField.getContent();
	}

	@Override
	public void setContent(String newContent) {
		try {
			Vector content = Vector.parseVector(newContent);
			this.xField.setContent("" + content.x);
			this.yField.setContent("" + content.y);
		} catch (NumberFormatException e) {
			Log.warn("VectorTextfield: Ungültiger Content: " + newContent);
		}
	}

	@Override
	public void addContentListener(ContentListener listener) {
		this.listener.add(listener);
	}

	@Override
	public void contentChanged(Contentable object) {
		for (ContentListener cL : this.listener) {
			cL.contentChanged(this);
		}
	}

	@Override
	public InterfacePart getInterfacePart() {
		return this;
	}
}
