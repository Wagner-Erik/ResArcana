package resarcana.graphics;

/**
 * Fehler, der geworfen wird, wenn es Grafikprobleme gibt, die wahrscheinlich
 * nicht auf Java Ebene gelöst werden können.
 * 
 */
public class GraphicsError extends Error {

	private static final long serialVersionUID = -5309858224099417866L;

	public GraphicsError() {
		super();
	}

	public GraphicsError(Throwable e) {
		super(e);
	}

	public GraphicsError(String description) {
		super(description);
	}
}
