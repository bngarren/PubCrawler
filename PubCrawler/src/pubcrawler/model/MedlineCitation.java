package pubcrawler.model;

import com.model.Journal;

public class MedlineCitation {

	private int pmid;
	private String articleTitle;
	private Journal journal;
	private Author firstAuthor;
	private Abstract articleAbstract;



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("---------------------------------\n");
		if (pmid > 0)
			builder.append("PMID ").append(pmid).append("\n");
		if (articleTitle != null)
			builder.append("\tTitle: ").append(articleTitle).append("\n");
		if (journal != null)
			builder.append("\tJournal: ").append(journal.getTitle()).append(" ISSN " + journal.getIssn()).append("\n");
		if (firstAuthor != null) {
			builder.append("\t1st Author: ").append(firstAuthor.getLastName()+", "+firstAuthor.getFirstName()).append("\n");
			if (firstAuthor.getAffiliation() != null){
				firstAuthor.getAffiliation().forEach(a -> builder.append("\t\t").append(a).append("\n"));
			}
		}
		if (articleAbstract != null){

			builder.append("\tAbstract: ");

			if (articleAbstract.getSummary() != null){
				builder.append(articleAbstract.getSummary());
			} else {
				builder.append("\n\tOBJECTIVE: ").append(articleAbstract.getObjective());
				builder.append("\n\tMETHODS: ").append(articleAbstract.getMethods());
				builder.append("\n\tRESULTS: ").append(articleAbstract.getResults());
				builder.append("\n\tCONCLUSIONS: ").append(articleAbstract.getConclusions());
			}

		}
		builder.append("\n---------------------------------");

		return builder.toString();
	}


	// -----------------------------------------------------------
	public int getPmid() {
		return pmid;
	}
	public void setPmid(int pmid) {
		this.pmid = pmid;
	}
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public Journal getJournal() {
		return this.journal;
	}
	public void setJournal(Journal j) {
		this.journal = j;
	}
	public Author getFirstAuthor() {
		return firstAuthor;
	}
	public void setFirstAuthor(Author firstAuthor) {
		this.firstAuthor = firstAuthor;
	}


	public Abstract getArticleAbstract() {
		return articleAbstract;
	}


	public void setArticleAbstract(Abstract articleAbstract) {
		this.articleAbstract = articleAbstract;
	}



}
