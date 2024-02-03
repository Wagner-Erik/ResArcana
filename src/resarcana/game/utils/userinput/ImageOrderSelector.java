package resarcana.game.utils.userinput;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.Log;

import resarcana.game.core.Artifact;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.objects.ImageButton;
import resarcana.graphics.gui.objects.Label;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.math.Rectangle;
import resarcana.utils.UtilFunctions;

public class ImageOrderSelector<T extends ImageHolder> extends Selector {

	public static final Rectangle TAPPABLE_SELECTOR_HITBOX = Artifact.ARTIFACT_HITBOX;

	private AdvancedGridContainer container;
	private final ArrayList<ImageButton> buttons = new ArrayList<ImageButton>();
	private final ArrayList<Label> labels = new ArrayList<Label>();
	private final ArrayList<T> images;

	private ArrayList<ImageButton> cur;

	public ImageOrderSelector(Selecting selecting, T[] images, String description) {
		super(selecting, description);
		if (images.length == 0) {
			Log.error("No tappables for selection");
		}
		this.images = new ArrayList<T>();
		for (int i = 0; i < images.length; i++) {
			this.images.add(images[i]);
		}
		this.setup();
	}

	public ImageOrderSelector(Selecting selecting, ArrayList<T> images, String description) {
		super(selecting, description);
		this.images = images;
		this.setup();
	}

	private void setup() {
		this.container = new AdvancedGridContainer(2, this.images.size());
		ImageButton b;
		Label l;
		for (int i = 0; i < this.images.size(); i++) {
			l = new Label("X", 3, Color.black);
			b = new ImageButton(InterfaceFunctions.IMAGESELECTOR_SELECT, this.images.get(i).getHitbox(),
					this.images.get(i).getImage(), i + 2);
			b.addInformable(this);
			this.container.add(l, 0, i);
			this.container.add(b, 1, i);
			this.labels.add(l);
			this.buttons.add(b);
		}
		this.cur = new ArrayList<ImageButton>(); // No selection at start
	}

	public ArrayList<T> getResult() {
		ArrayList<T> result = new ArrayList<T>();
		for (int i = 0; i < this.cur.size(); i++) {
			result.add(this.images.get(this.buttons.indexOf(this.cur.get(i))));
		}
		return result;
	}

	@Override
	protected InterfaceContainer getSelectionInterface() {
		return this.container;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getFunction() == InterfaceFunctions.IMAGESELECTOR_SELECT
				&& object.getStatus() == Mousestatus.STATUS_LEFT_PRESSED) {
			if (object instanceof ImageButton) {
				if (this.buttons.contains(object)) {
					if (this.cur.contains(object)) {
						this.resetOrder();
					} else {
						this.cur.add((ImageButton) object);
						this.labels.get(this.buttons.indexOf(object)).setText("" + this.cur.size());
					}
					Log.info("New order: " + UtilFunctions.ListToString(this.cur));
				}
			}
		}
		super.mouseButtonAction(object);
	}

	private void resetOrder() {
		this.cur.clear();
		for (Label label : this.labels) {
			label.setText("X");
		}
	}

	@Override
	protected boolean isSelectionDone() {
		return this.cur.size() == this.buttons.size();
	}

}
