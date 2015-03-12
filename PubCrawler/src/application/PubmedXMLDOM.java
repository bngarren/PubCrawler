package application;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import application.model.Abstract;
import application.model.Author;
import application.model.MedlineCitation;

public class PubmedXMLDOM {

	private Map<String, Node> articleNodes;
	private MedlineCitation medlineCitation;

	public PubmedXMLDOM(){


	}

	public List<Article> getArticlesByIds(int[] ids) throws IOException, ParserConfigurationException, SAXException{

		StringBuilder idBuilder = new StringBuilder(String.valueOf(ids[0]));
		for (int i = 1; i < ids.length; i ++){
			idBuilder.append("," + ids[i]);
		}


		List<Article> articles = new ArrayList<Article>();
		Article article = null;

		StringBuilder builder = new StringBuilder("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		builder.append("efetch.fcgi");
		builder.append("?db=");
		builder.append("pubmed");
		builder.append("&id=");
		builder.append(String.valueOf(idBuilder.toString()));
		builder.append("&rettype=fasta&retmode=xml");

		String urlString = builder.toString().trim();
		//System.out.println("URL: " + urlString);
		//System.out.println("Searching PubMed for PMID " + id);

		URL url = new URL(urlString);

		URLConnection connection = url.openConnection();

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;

		docBuilder = builderFactory.newDocumentBuilder();

		Document document = docBuilder.parse(connection.getInputStream());

		Element rootElement = document.getDocumentElement();

		NodeList nodesOfRoot = rootElement.getChildNodes();

		for (int i = 0; i < nodesOfRoot.getLength(); i++){
			Node node = nodesOfRoot.item(i);

			if (node instanceof Element && node.getNodeName().equals("PubmedArticle")){
				//System.out.println("Found a PubmedArticle!");
				article = new Article();
				List<Node> nodesOfArticle = getNodesOfArticle(node);

				if (articleNodes.containsKey("MedlineCitation")){
					article.setCitation(getMedlineCitation(articleNodes.get("MedlineCitation")));
				}

				articles.add(article);
			}

		}


		return articles;
	}

	private List<Node> getNodesOfArticle(Node articleNode){
		List<Node> nodes = new ArrayList<Node>();
		this.articleNodes = new HashMap<String, Node>();

		NodeList nodeList = articleNode.getChildNodes();

		for (int i=0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if (node instanceof Element){
				//System.out.println("Adding " + node.getNodeName() + " to list of nodes for " + articleNode.getNodeName());
				nodes.add(node);
				//System.out.printf("Added <'%s', %s> to articleNodes hashmap\n", node.getNodeName(), "node");
				this.articleNodes.put(node.getNodeName(), node);
			}
		}

		return nodes;
	}

	private MedlineCitation getMedlineCitation(Node source){

		MedlineCitation citation = new MedlineCitation();

		NodeList nodeList = source.getChildNodes();

		for (int i=0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if (node instanceof Element){

				Element e = (Element) node;

				switch (e.getNodeName()){

				case "PMID":
					citation.setPmid(Integer.valueOf(e.getTextContent()));
					break;

				case "Article":

					NodeList nodeListArticle = e.getChildNodes();

					for (int j=0; j < nodeListArticle.getLength(); j++){
						Node nodeOfArticle = nodeListArticle.item(j);
						if (nodeOfArticle instanceof Element){
							Element el = (Element) nodeOfArticle;

							switch (el.getNodeName()){

							case "Journal":
								com.model.Journal journal = new com.model.Journal();
								journal.setTitle(getValueOfChildElement(el, "Title"));
								journal.setIssn(getValueOfChildElement(el, "ISSN"));
								journal.setAbbreviation(getValueOfChildElement(el, "ISOAbbreviation"));
								citation.setJournal(journal);
								break;

							case "ArticleTitle":
								citation.setArticleTitle(el.getTextContent());
								break;

							case "Abstract":
								Abstract a = getAbstract(el);
								citation.setArticleAbstract(a);

							case "AuthorList":
								List<Author> authors = getAuthorList(el);
								if (!authors.isEmpty())
									citation.setFirstAuthor(authors.get(0));
								break;

							}
						}

					}

					break;

				}
			}

		} // end for each Node in MedlineCitation node

		if (citation != null){
			this.medlineCitation = citation;
		}

		return citation;

	}

	private Abstract getAbstract(Node source){
		Abstract result = new Abstract();

		NodeList nodeList = source.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if (node instanceof Element){
				Element e = (Element) node;
				if (e.getNodeName().equals("AbstractText")){

					if (e.getAttributes().getLength() == 0 ){
						result.setSummary(e.getTextContent());
					}

					switch (e.getAttribute("Label")){
					case "OBJECTIVE":
						result.setObjective(e.getTextContent());
						break;
					case "METHODS":
						result.setMethods(e.getTextContent());
						break;
					case "RESULTS":
						result.setResults(e.getTextContent());
						break;
					case "CONCLUSIONS":
						result.setConclusions(e.getTextContent());
						break;
					}
				}
			}
		}
		return result;
	}

	private List<Author> getAuthorList(Node authorList){

		List<Author> authors = new ArrayList<Author>();

		NodeList nodeList = authorList.getChildNodes();

		for (int j=0; j < nodeList.getLength(); j++){
			Node node = nodeList.item(j);
			if (node instanceof Element){
				Element e = (Element) node;
				if (e.getNodeName().equals("Author")){
					Author author = new Author();
					author.setFirstName(getValueOfChildElement(e, "ForeName"));
					author.setLastName(getValueOfChildElement(e, "LastName"));

					NodeList nodesOfAuthor = e.getChildNodes();

					for (int k = 0; k < nodesOfAuthor.getLength(); k++){
						node = nodesOfAuthor.item(k);
						if (node instanceof Element){
							e = (Element) node;

							if (e.getNodeName().equals("AffiliationInfo")){
								author.setAffiliation(getValueOfChildElement(e, "Affiliation"));
							}
						}
					}

					authors.add(author);
				}
			}
		}
		return authors;
	}

	/**
	 * @param source - The node for whose children will be searched for the given element name
	 * @param elementName - The element to search for and whose text content will be returned
	 * @return String representation of the element's text content. The child element must NOT have any child nodes, only text content.
	 */
	private String getValueOfChildElement(Node source, String elementName){
		NodeList nodeList = source.getChildNodes();

		for (int j=0; j < nodeList.getLength(); j++){
			Node node = nodeList.item(j);
			if (node instanceof Element){
				Element e = (Element) node;
				if (e.getNodeName().equals(elementName)){
					return e.getTextContent();
				}
			}
		}
		return null;
	}

}
