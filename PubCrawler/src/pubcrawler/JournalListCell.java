package pubcrawler;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import com.model.Journal;


public class JournalListCell extends ListCell<Journal> {

	@Override
	protected void updateItem(Journal j, boolean b) {

		super.updateItem(j, b);

		if (b){
			setText(null);
			setGraphic(null);
		} else {

			if (j != null){

				Label label = new Label(j.getTitle() + " - " + j.getIssn());

				setGraphic(label);
			}
		}
	}



}