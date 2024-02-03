package resarcana.graphics;

/**
 * Wird geworfen, wenn ein Fehler beim Laden von Resourcen auftritt und kein
 * Ersatz verfügbar ist.
 * 
 */
public class ResourceError extends Error {

	private static final long serialVersionUID = 3117915741962126340L;

	public ResourceError() {
		super();
	}

	public ResourceError(String description) {
		super(description);
	}

	public ResourceError(Throwable cause) {
		super(cause);
	}
}
