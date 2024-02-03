package resarcana.graphics.gui;

import java.util.ArrayList;

public abstract class ContentableObject extends InterfaceObject implements Contentable {

	private ArrayList<ContentListener> listeners = new ArrayList<ContentListener>();

	public ContentableObject(InterfaceFunction function) {
		super(function);
	}

	public ContentableObject(InterfaceFunction function, int key) {
		super(function, key);
	}

	protected void informContentListeners() {
		for (ContentListener cL : this.listeners) {
			cL.contentChanged(this);
		}
	}

	@Override
	public void addContentListener(ContentListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public InterfacePart getInterfacePart() {
		return this;
	}
}
