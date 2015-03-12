package application;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

public class PubTest1 {

	private PubmedXML pmXML = new PubmedXML();
	private PubmedXMLDOM pmXMLDOM = new PubmedXMLDOM();

	public static void main(String[] args) throws IOException, XMLStreamException{

		PubTest1 pt = new PubTest1();

		Scanner scanner = new Scanner(System.in);

		String input;

		while (scanner.hasNext()){

			input = scanner.nextLine();

			String[] ids = input.split(" ");
			int[] intIds = new int[ids.length];

			for (int i = 0; i < ids.length; i++){
				intIds[i] = Integer.valueOf(ids[i].trim());
			}

			pt.getArticlesByPMIDS(intIds);

		}





	}

	public List<Article> getArticlesByPMIDS(int[] ids) throws XMLStreamException, IOException{

		long startTime = System.nanoTime();

		List<Article> articles = new ArrayList<Article>();
		try {

			articles = pmXMLDOM.getArticlesByIds(ids);

		} catch (ParserConfigurationException | SAXException e) {

			e.printStackTrace();
		}

		long delta = System.nanoTime() - startTime;
		System.out.println(Duration.ofNanos(delta).toMillis() + "ms");
		System.out.println("******************************************************************************************************");

		return articles;
	}

}
