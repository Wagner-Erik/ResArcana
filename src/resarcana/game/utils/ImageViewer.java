package resarcana.game.utils;

import java.util.ArrayList;

import org.newdawn.slick.Input;

import resarcana.game.utils.userinput.ImageHolder;
import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.HideableContainer;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.ScalableObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.container.SpecialBackgroundContainer;
import resarcana.graphics.gui.objects.ImageButton;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.Slider;
import resarcana.graphics.gui.objects.TextButton;
import resarcana.graphics.utils.ScrollingListener;
import resarcana.graphics.utils.ScrollingManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class ImageViewer extends HideableContainer implements Informable, ContentListener, ScrollingListener {

	private static final int SCALE_BASE = 100;
	private final float baseScale;
	private boolean scrolling;

	private int maxNumberItems, rows, cols;
	private int curScale, newScale;
	private int offset = 0;

	private AdvancedGridContainer mainCon, contentCon;
	private SpecialBackgroundContainer background;
	private Slider scaleSelection;

	private int curCards = 0;

	private ArrayList<ScalableObject> elements = new ArrayList<ScalableObject>();
	private ArrayList<Integer> highlights = new ArrayList<Integer>();
	private boolean scrolled = false;

	public ImageViewer(float scale, int maxNumberItems, int rows, boolean scrolling) {
		super();
		this.baseScale = scale;
		this.scrolling = scrolling;

		TextButton closeButton = new TextButton(InterfaceFunctions.DIALOG_CLOSE, Input.KEY_ESCAPE, "Close", 2);
		closeButton.addInformable(this);

		this.scaleSelection = new Slider(InterfaceFunctions.IMAGEVIEWER_SCALE, 49.5f, 200.5f, 100.0f, 200);
		this.scaleSelection.addContentListener(this);
		this.curScale = SCALE_BASE;
		this.newScale = SCALE_BASE;

		AdvancedGridContainer interaction = new AdvancedGridContainer(1, 2, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, 100, 10);
		interaction.add(this.scaleSelection, 0, 0, AdvancedGridContainer.MODUS_X_RIGHT,
				AdvancedGridContainer.MODUS_DEFAULT);
		interaction.add(closeButton, 0, 1, AdvancedGridContainer.MODUS_X_LEFT, AdvancedGridContainer.MODUS_DEFAULT);

		this.mainCon = new AdvancedGridContainer(2, 1, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, 10, 10);
		this.mainCon.add(interaction, 0, 0);

		this.background = new SpecialBackgroundContainer(this.mainCon, true, true, true, true, 1.f);
		this.add(this.background, Vector.ZERO);

		ScrollingManager.getInstance().addListener(this);

		this.resize(maxNumberItems, rows);
		this.redoContentCon();
	}

	public ImageViewer(float scale, int maxNumberItems, int rows) {
		this(scale, maxNumberItems, rows, false);
	}

	public void addImage(ImageHolder card) {
		this.addImage(card.getImage(), card.getHitbox());
	}

	public void addImage(String image, Rectangle hitbox) {
		this.elements.add(new ImageButton(InterfaceFunctions.NONE, hitbox.scale(this.baseScale), image));
		// this.labels.add(null);
		if (this.scrolling) {
			this.highlights.clear();
			this.highlights.add(new Integer(this.curCards));
		}
		this.curCards++;
		if (this.scrolling) {
			this.advance();
		} else {
			this.redoContentCon();
		}
	}

	public void addHistory(HistoryElement elem) {
		this.elements.add(elem.scale(this.baseScale));
		if (this.scrolling) {
			this.highlights.clear();
			this.highlights.add(new Integer(this.curCards));
		}
		this.curCards++;
		if (this.scrolling) {
			if (!this.advance()) {
				this.redoContentCon();
			}
		} else {
			this.redoContentCon();
		}
	}

	public void highlightElement(int n) {
		this.highlights.add(new Integer(n));
		this.redoContentCon();
	}

	public void resetHighlights() {
		this.highlights.clear();
		this.redoContentCon();
	}

	public void resize(int maxNumberItems, int rows) {
		if (rows > 0 && maxNumberItems > 0) {
			this.rows = rows;
			this.maxNumberItems = maxNumberItems;
			this.cols = (int) Math.ceil((float) this.maxNumberItems / this.rows);
			this.redoContentCon();
		}
	}

	private void redoContentCon() {
		this.mainCon.remove(this.contentCon);
		this.contentCon = new AdvancedGridContainer(this.rows, this.cols, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, 6, 6);
		for (int i = 0; i < this.rows * this.cols && i + this.offset < this.elements.size(); i++) {
			this.contentCon.add(this.elements.get(i + this.offset).scale(this.baseScale * this.newScale / SCALE_BASE)
					.setBorder(this.highlights.contains(new Integer(i + this.offset))), i / this.cols, i % this.cols);
		}
		this.mainCon.add(this.contentCon, 1, 0);
		// Save new scale
		this.curScale = this.newScale;
	}

	public boolean advance() {
		if (this.offset < this.curCards - this.maxNumberItems) {
			this.offset++;
			this.redoContentCon();
			return true;
		}
		return false;
	}

	public boolean back() {
		if (this.offset > 0) {
			this.offset--;
			this.redoContentCon();
			return true;
		}
		return false;
	}

	@Override
	public void poll(Input input, float secounds) {
		super.poll(input, secounds);
		if (this.curScale != this.newScale) {
			this.redoContentCon();
		}
		this.scrolled = false;
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
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void contentChanged(Contentable object) {
		if (object instanceof Slider) {
			if (((Slider) object).getFunction() == InterfaceFunctions.IMAGEVIEWER_SCALE) {
				this.newScale = (int) ((Slider) object).getValue();
			}
		}
	}

	@Override
	protected boolean resize() {
		return this.setHitbox(this.background.getHitbox());
	}

	@Override
	public void mouseWheelMoved(Input input, int change) {
		if (!this.scrolled) {
			this.scrolled = true;
			if (!this.scrolling || input.isKeyDown(Input.KEY_LCONTROL) || input.isKeyDown(Input.KEY_RCONTROL)) {
				if (change > 0) {
					this.scaleSelection.increment();
				} else {
					this.scaleSelection.decrement();
				}
			} else {
				if (!this.scaleSelection.isAcceptingScrollingInput()) {
					if (change > 0) {
						this.advance();
					} else {
						this.back();
					}
				}
			}
		}
	}

	@Override
	public boolean isAcceptingScrollingInput() {
		return this.hasMouseOver() && this.isShown();
	}

}
