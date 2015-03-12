package application.model;

public class Biblio {

	private String issn;
	private String title;
	private String contributor;
	private String language;
	private String publisher;



	@Override
	public String toString() {
		return "Biblio [issn=" + issn + ", title=" + title + ", contributor="
				+ contributor + ", language=" + language + ", publisher="
				+ publisher + "]";
	}



	public String getIssn() {
		return issn;
	}
	public void setIssn(String issn) {
		this.issn = issn;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContributor() {
		return contributor;
	}
	public void setContributor(String contributor) {
		this.contributor = contributor;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}






}
