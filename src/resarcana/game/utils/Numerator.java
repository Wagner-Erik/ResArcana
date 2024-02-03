package resarcana.game.utils;

public class Numerator {

	private int curNumber;

	public Numerator(int firstNumber) {
		this.curNumber = firstNumber;
	}

	public Numerator() {
		this(0);
	}

	public int getNextNumber() {
		this.curNumber++;
		return this.curNumber - 1;
	}
}
