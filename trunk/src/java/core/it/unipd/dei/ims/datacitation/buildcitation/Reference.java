package it.unipd.dei.ims.datacitation.buildcitation;

/**
 * This class handles the Reference object which contains the human-readable
 * reference and the machine-readable reference.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class Reference {

	/**
	 * The human-readable reference.
	 */
	private String reference;

	/**
	 * The machine-readable reference.
	 */
	private String pathReference;

	public Reference(String reference, String pathReference) {
		this.reference = reference;

		this.pathReference = pathReference;
	}

	public Reference() {
		this.reference = null;
		this.pathReference = null;
	}

	/**
	 * Set the human-readable reference
	 * 
	 * @param reference
	 *            the human-readable reference
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * Set the machine-readable reference
	 * 
	 * @param pathReference
	 *            the machine-readable reference
	 */
	public void setPathReference(String pathReference) {
		this.pathReference = pathReference;
	}

	/**
	 * Get the human-readable reference
	 * 
	 * @param reference
	 *            the human-readable reference
	 */
	public String getReference() {
		return this.reference;
	}

	/**
	 * Get the machine-readable reference
	 * 
	 * @param pathReference
	 *            the machine-readable reference
	 */
	public String getPathReference() {
		return this.pathReference;
	}
}
