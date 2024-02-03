package resarcana.graphics.gui.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.ContentableObject;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.ScrollingListener;
import resarcana.graphics.utils.ScrollingManager;
import resarcana.graphics.utils.Timer;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

public class Slider extends ContentableObject implements ScrollingListener {

	private static final Rectangle SLIDER_BOX = new Rectangle(Vector.ZERO, 32, 32);
	private static final int SCROLL_INTERVALS = 20;

	private final float length;
	private final Rectangle barHitbox;

	private float min, max, value, lastValue;
	private Vector lastMousePos = Vector.ZERO;
	private boolean grabbed = false;
	private Timer input_timer = new Timer(Parameter.GUI_TEXTFIELD_DELAY);
	private boolean incremented = false;

	public Slider(InterfaceFunctions function, float min, float max, float start, float length) {
		super(function);
		this.length = length;
		this.min = min;
		this.max = max;
		this.value = start;
		this.lastValue = start;
		this.setHitbox(new Rectangle(Vector.ZERO, this.length + SLIDER_BOX.width, SLIDER_BOX.height));
		this.barHitbox = new Rectangle(Vector.ZERO, this.length, SLIDER_BOX.height / 10);
		
		ScrollingManager.getInstance().addListener(this);
	}

	public Slider(InterfaceFunctions function, float min, float max, float start) {
		this(function, min, max, start, 200);
	}

	private Rectangle getSliderHitbox() {
		return SLIDER_BOX.modifyCenter(this.length * ((this.value - this.min) / (this.max - this.min) - 0.5f), 0);
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		GraphicUtils.translate(g, this.getCenter());
		GraphicUtils.fill(g, this.barHitbox, new Color(0, 0, 50));
		GraphicUtils.drawImage(g, this.getSliderHitbox(),
				ResourceManager.getInstance().getImage("interface-icons/slider.png"));
		g.popTransform();
	}

	@Override
	public void poll(Input input, float secounds) {
		this.incremented = false;
		super.poll(input, secounds);
		this.input_timer.poll(input, secounds);
		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
			Vector mousePos = new Vector(input.getMouseX(), input.getMouseY());
			if (!this.grabbed) {
				this.lastMousePos = mousePos;
				this.lastValue = this.value;
				if (this.getSliderHitbox().moveBy(this.getCenter()).isPointInThis(mousePos)) {
					this.grabbed = true;
				}
			} else {
				this.value = Math.max(this.min, Math.min(this.max,
						this.lastValue + (mousePos.x - this.lastMousePos.x) / this.length * (this.max - this.min)));
				if (!this.input_timer.isRunning()) {
					this.input_timer.restart();
					this.informContentListeners();
				}
			}
		} else {
			// Make sure the last position is reported to the listeners when letting go of
			// the slider
			if (this.grabbed) {
				this.informContentListeners();
			}
			this.grabbed = false;
		}
	}

	@Override
	public String getContent() {
		return "" + this.value;
	}

	public float getValue() {
		return this.value;
	}

	@Override
	public void setContent(String newContent) {
		this.setValue(Float.parseFloat(newContent));
	}

	public void setMaximum(float nmax) {
		if (nmax >= this.min) {
			this.max = nmax;
			if (this.value > nmax) {
				this.value = nmax;
				this.informContentListeners();
			}
		}
	}

	public void setMinimum(float nmin) {
		if (nmin <= this.max) {
			this.min = nmin;
			if (this.value < nmin) {
				this.value = nmin;
				this.informContentListeners();
			}
		}
	}

	public float getMinimum() {
		return this.min;
	}

	public float getMaximum() {
		return this.max;
	}

	public void setValue(float number) {
		this.value = number < this.min ? this.min : (number > this.max ? this.max : number);
		this.informContentListeners();
	}

	public void increment() {
		if (!this.incremented) {
			this.incremented = true;
			this.setValue(this.value + (this.max - this.min) / SCROLL_INTERVALS);
		}
	}

	public void decrement() {
		if (!this.incremented) {
			this.incremented = true;
			this.setValue(this.value - (this.max - this.min) / SCROLL_INTERVALS);
		}
	}

	@Override
	public void mouseWheelMoved(Input input, int change) {
		if (change > 0) {
			this.increment();
		} else {
			this.decrement();
		}
	}

	@Override
	public boolean isAcceptingScrollingInput() {
		return this.getStatus() == Mousestatus.STATUS_MOUSE_OVER && this.isShown();
	}
}
