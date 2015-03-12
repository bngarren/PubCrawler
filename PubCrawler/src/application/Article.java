package application;

import application.model.MedlineCitation;

public class Article {

	private MedlineCitation citation;



	@Override
	public String toString() {
		return citation.toString();
	}


	// -------------------------------------------------------------------


	public MedlineCitation getCitation() {
		return citation;
	}


	public void setCitation(MedlineCitation citation) {
		this.citation = citation;
	}
}
