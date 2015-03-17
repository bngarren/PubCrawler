package pubcrawler;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.xml.sax.SAXException;

import pubcrawler.model.Biblio;
import pubcrawler.model.MedlineCitation;

import com.db.DAOFactory;
import com.db.JournalDAO;
import com.model.Journal;

public class RootViewController implements Initializable {

	@FXML
	private ListView<Journal> listView;
	@FXML TextField txtSearch;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		// Set the listView's cell factory, i.e. how each cell is displayed
		listView.setCellFactory((Callback<ListView<Journal>, ListCell<Journal>>) callback -> {
			return new JournalListCell();
		});

	}

	@FXML
	private void btnStartAction(ActionEvent e){
		try {
			int pmidSearch = -1;
			int numberToScan = 50;
			int cycles = 4;

			String search = txtSearch.textProperty().getValue();
			if (search != null && !search.trim().isEmpty()){
				pmidSearch = Integer.valueOf(search);
				numberToScan = 1;
				cycles = 1;
				txtSearch.textProperty().set("");
			}

			startNewJournalSearch(pmidSearch, numberToScan, cycles);

		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}
	}

	@FXML
	private void btnVerifyAndSaveAction(ActionEvent e){
		SelectionModel<Journal> selectionModel = listView.getSelectionModel();
		Journal selectedJournal = selectionModel.getSelectedItem();

		// First let's check if journal already exists in the database and if it has already been verified by ISSN

		DAOFactory factory = DAOFactory.getInstance("litterbox");
		JournalDAO journalDAO = factory.getJournalDAO();

		boolean existsInDatabase = false;
		boolean isVerified = false;

		Journal testJournal = journalDAO.find(selectedJournal.getIssn());

		if (testJournal != null){
			// Journal exists in the database
			existsInDatabase = true;

			if (testJournal.isVerified()) // Has this journal already been verified through WorldCat.org?
				isVerified = true;
		}

		if (!isVerified){

			WorldCatXMLDOM wc = new WorldCatXMLDOM();
			Biblio biblio = null;
			try {

				biblio = wc.getBiblioByIssn(selectedJournal.getIssn());

			} catch (IOException | ParserConfigurationException | SAXException e1) {
				e1.printStackTrace();
			}

			if (biblio != null){
				System.out.println("Biblio for ISSN " + biblio.getIssn() + " created successfully!");
				System.out.println(biblio.toString());

				// Update journal with information from Biblio
				//selectedJournal.setTitle(biblio.getTitle() != null ? biblio.getTitle() : "");
				selectedJournal.setContributor(biblio.getContributor() != null ? biblio.getContributor() : "");
				selectedJournal.setPublisher(biblio.getPublisher() != null ? biblio.getPublisher() : "");
				selectedJournal.setLanguage(biblio.getLanguage() != null ? biblio.getLanguage() : "");
				selectedJournal.setVerified(true);
			}
		}


		if (existsInDatabase){
			System.out.println("The journal with ISSN " + selectedJournal.getIssn() + " is already in the database");
			// journalDAO.update(selectedJournal);

		} else {
			System.out.println("The journal with ISSN " + selectedJournal.getIssn() + " is not yet in the database");
			selectedJournal.setId(0);
			journalDAO.create(selectedJournal);
		}



	}

	/**
	 * @param numberToScan - Number of PMIDs to scan per cycle
	 * @param cycles - Total number of cycles to perform. Total scans = numberToScan * cycle
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void startNewJournalSearch(int PMID, int numberToScan, int cycles) throws IOException, ParserConfigurationException, SAXException{

		Map<Journal, Integer> finalJournalCount = new HashMap<Journal, Integer>();

		PubmedXMLDOM pmXMLDOM = new PubmedXMLDOM();


		int startPMID = getRandomPmid();
		if (PMID > 0)
			startPMID = PMID;
		int pmidsPerRequest = numberToScan / 4;
		if (pmidsPerRequest < 1)
			pmidsPerRequest = 1;
		int maxCycles = cycles;
		int cycleCount = 1;

		int pmidsScanned = 0;

		while (cycleCount <= maxCycles){

			Map<String, Integer> journalCount = new HashMap<String, Integer>();
			Map<String, Journal> journalTitleMap = new HashMap<String, Journal>();

			Bag<String> journals = new HashBag<String>();


			int count = 1;
			int i;

			System.out.println("--------- Starting CYCLE #" + cycleCount + " -----------");

			for (i = startPMID; i < startPMID + numberToScan; i += pmidsPerRequest){

				System.out.println("scanning " + pmidsPerRequest + " PMIDs for articles...");

				int[] ids = new int[pmidsPerRequest];
				for (int j = 0; j < ids.length; j++){
					ids[j] = i + j;
					pmidsScanned++;
				}

				List<Article> articles = pmXMLDOM.getArticlesByIds(ids);
				MedlineCitation citation = null;

				if (!articles.isEmpty()){

					for(Article article : articles){
						if (article == null){
							System.out.println(count + " - " + "No article found");
						} else {

							citation = article.getCitation();

							if (citation != null) {
								if (citation.getJournal() != null){

									Journal j = citation.getJournal();
									j.setTitle(j.getTitle());

									journals.add(j.getTitle());
									journalTitleMap.put(j.getTitle(), j);

								} else {
									System.out.println("No journal title!");
								}
							} else {
								System.out.println("No citation!");
							}

						}

						count++;

					}
				} else {
					System.out.println("No articles found from these PMIDs");
				}

			} // end for ARTICLES REQUEST

			startPMID = getRandomPmid();

			// For each journal in the bag holding the results from the last articles request
			System.out.println("Bag of journals for this request, size = " + journals.size() + ", unique # " + journals.uniqueSet().size());
			for (String s : journals.uniqueSet()){
				int jCount = journals.getCount(s);
				System.out.println("\t" + s + " count " + jCount);

				if (!journalCount.containsKey(s)){
					//System.out.println("putting '" + s + "' into journalCount");
					journalCount.put(s, jCount);

				} else {
					System.out.println("'journals' already contains the above title");
					journalCount.replace(s, journalCount.get(s) + jCount);
				}

			}

			StringValueComparator comparator = new StringValueComparator(journalCount);
			Map<String, Integer> sortedMap = new TreeMap<String, Integer> (comparator);
			sortedMap.putAll(journalCount);

			System.out.println("----END PMID " + startPMID + "--------- CYCLE #"+cycleCount+" COUNT --scanned "+numberToScan+"-----------------------");


			// Get the top 5 results from the journalCount and put add it the the final journal count
			int t = 1;
			for (Entry<String, Integer> e : sortedMap.entrySet()){

				Journal j = journalTitleMap.get(e.getKey());

				if (t <= 5){

					if (finalJournalCount.containsKey(j)){
						finalJournalCount.replace(j, finalJournalCount.get(j) + e.getValue());
					} else {
						finalJournalCount.put(j, e.getValue());
					}
					System.out.print(j.getTitle() + " (" + e.getValue() + ")");
					System.out.print(" #" + t);
					System.out.println("");
					t++;

				}

			}

			cycleCount++;

		} // end while cycle

		JournalValueComparator comparator2 = new JournalValueComparator(finalJournalCount);
		Map<Journal, Integer> sortedMap2 = new TreeMap<Journal, Integer> (comparator2);
		sortedMap2.putAll(finalJournalCount);

		System.out.println("\n\n-------------********* FINAL COUNT *********------scanned " + pmidsScanned + " PMIDs-------------------");
		sortedMap2.forEach((s, in) -> {
			System.out.println(s + " (" + in + ")");
		});

		setListView(FXCollections.observableArrayList(sortedMap2.keySet()));
	}


	private int getRandomPmid(){
		return (int) (Math.random()*25517348+10000000);
	}



	private void setListView(ObservableList<Journal> obsList){
		this.listView.setItems(obsList);
	}



	// comparators ----------------------------------------------------

	static class StringValueComparator implements Comparator<String> {

		Map<String, Integer> base;
		public StringValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.
		@Override
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

	static class JournalValueComparator implements Comparator<Journal> {

		Map<Journal, Integer> base;
		public JournalValueComparator(Map<Journal, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.
		@Override
		public int compare(Journal a, Journal b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

}
