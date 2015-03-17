package pubcrawler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pubcrawler.model.Biblio;

public class WorldCatXMLDOM {

	public static final String WS_KEY =
			"KZpsoeUHFs7SMr0qIazph2DWnaufj9yTqgRKTYPZuLRroJHBfTz0ihBKPpoIZvDhoo56Pvrzc4f9JCXI";


	public Biblio getBiblioByIssn(String issn) throws IOException, ParserConfigurationException, SAXException{

		Biblio biblio = null;

		StringBuilder builder = new StringBuilder("http://www.worldcat.org/webservices/catalog/content/issn/");
		builder.append(issn);
		builder.append("?recordSchema=info%3Asrw%2Fschema%2F1%2Fdc");
		builder.append("&&wskey=");
		builder.append(WS_KEY);

		String urlString = builder.toString().trim();

		URL url = new URL(urlString);

		URLConnection connection = url.openConnection();

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;

		docBuilder = builderFactory.newDocumentBuilder();

		Document document = docBuilder.parse(connection.getInputStream());

		Element rootElement = document.getDocumentElement();

		NodeList nodesOfRoot = rootElement.getChildNodes();

		if (nodesOfRoot != null){
			biblio = new Biblio();
			biblio.setIssn(issn);
		}

		for (int i = 0; i < nodesOfRoot.getLength(); i++){
			Node node = nodesOfRoot.item(i);

			if (node instanceof Element){
				Element e = (Element) node;

				switch (e.getNodeName()){

				case "dc:contributor":
					biblio.setContributor(cleanText(e.getTextContent()));
					break;

				case "dc:language":
					biblio.setLanguage(cleanText(e.getTextContent()));
					break;

				case "dc:publisher":
					biblio.setPublisher(cleanText(e.getTextContent()));
					break;

				case "dc:title":
					biblio.setTitle(cleanText(cleanTitle(e.getTextContent())));
					break;

				}
			}

		}

		return biblio;
	}

	private String cleanText(String text){

		if (text.trim().endsWith(".")){
			text = text.substring(0, text.length()-1);
		}

		return text;

	}

	private String cleanTitle(String title){

		if (title.contains("(Online)")){
			title = title.replace("(Online)", "");
		}
		return title.trim();
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException{
		WorldCatXMLDOM test = new WorldCatXMLDOM();
		Biblio b = test.getBiblioByIssn("1097-6833"); // Journal of pediatrics
		System.out.println("contributor: " + b.getContributor());
	}

}
