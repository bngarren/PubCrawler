package pubcrawler;

import com.db.DAOFactory;
import com.db.JournalDAO;

public class DAOTest {

	public static void main(String[] args){

		DAOFactory daoFactory = DAOFactory.getInstance("litterbox");
		JournalDAO journalDAO = daoFactory.getJournalDAO();



	}

}
