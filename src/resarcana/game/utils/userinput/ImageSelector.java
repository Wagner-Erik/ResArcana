package resarcana.game.utils.userinput;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Artifact;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.objects.ImageButton;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.math.Rectangle;

public class ImageSelector<T extends ImageHolder> extends Selector {

	public static final Rectangle TAPPABLE_SELECTOR_HITBOX = Artifact.ARTIFACT_HITBOX;

	public static final int MAXIMUM_NUMBER_PER_ROW = 5;
	public static final int MAXIMUM_ROWS = 3;

	private AdvancedGridContainer container;
	private final ArrayList<ImageButton> buttons = new ArrayList<ImageButton>();
	private final ArrayList<T> images;

	private ImageButton cur;

	public ImageSelector(Selecting selecting, T[] images, String description) {
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

	@SuppressWarnings("unchecked")
	public ImageSelector(Selecting selecting, ArrayList<T> images, String description) {
		super(selecting, description);
		this.images = (ArrayList<T>) images.clone();
		this.setup();
	}

	private void setup() {
		int rows = Math.min((int) Math.ceil((double) this.images.size() / MAXIMUM_NUMBER_PER_ROW), MAXIMUM_ROWS);
		int cols = (int) Math.ceil((double) this.images.size() / rows);
		this.container = new AdvancedGridContainer(rows, cols);
		ImageButton b;
		for (int i = 0; i < this.images.size(); i++) {
			b = new ImageButton(InterfaceFunctions.IMAGESELECTOR_SELECT, this.images.get(i).getHitbox(),
					this.images.get(i).getImage(), i + 2);
			b.addInformable(this);
			this.container.add(b, i / cols, i % cols);
			this.buttons.add(b);
		}
		this.cur = null; // No selection at start
	}

	public T getResult() {
		return this.images.get(this.buttons.indexOf(this.cur));
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
					if (this.cur != null) {
						this.cur.setBorder(false);
					}
					this.cur = (ImageButton) object;
					this.cur.setBorder(true);
				}
			}
		}
		super.mouseButtonAction(object);
	}

	@Override
	protected boolean isSelectionDone() {
		return this.cur != null;
	}

}
