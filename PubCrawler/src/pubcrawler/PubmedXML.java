package pubcrawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PubmedXML {



	public Article getArticleById(int id) throws XMLStreamException, IOException{

		Article article = null;
		String nodeContent = null;

		StringBuilder builder = new StringBuilder("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		builder.append("efetch.fcgi");
		builder.append("?db=");
		builder.append("pubmed");
		builder.append("&id=");
		builder.append(String.valueOf(id));
		builder.append("&rettype=fasta&retmode=xml");

		String urlString = builder.toString().trim();
		System.out.println("URL: " + urlString);

		URL url = new URL(urlString);

		URLConnection connection = url.openConnection();

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(new BufferedInputStream(connection.getInputStream()));

		while (reader.hasNext()){

			int event = reader.next();

			switch (event){

			case XMLStreamConstants.START_ELEMENT:

				if (reader.getLocalName().equals("PubmedArticle")){
					article = new Article();
				}

				break;

			case XMLStreamConstants.CHARACTERS:
				nodeContent = reader.getText().trim();
				break;

			case XMLStreamConstants.END_ELEMENT:

				switch (reader.getLocalName()){

				case "PMID":
					//article.setPMID(Integer.valueOf(nodeContent));
					break;

				case "ArticleTitle":
					//article.setTitle(nodeContent);
					break;


				}

				break;

			} // end switch(event)

		} // end while(reader.hasNext())

		return article;
	}

}
