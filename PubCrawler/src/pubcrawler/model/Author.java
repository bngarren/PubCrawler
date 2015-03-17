package pubcrawler.model;

import java.util.ArrayList;
import java.util.List;

public class Author {

	private String lastName;
	private String firstName;
	private List<String> affiliation;




	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public List<String> getAffiliation(){
		return affiliation;
	}
	public void setAffiliation(String affiliation){
		this.affiliation = new ArrayList<String>();
		String[] items = affiliation.split(";");
		for (int i = 0; i < items.length; i++){
			this.affiliation.add(items[i].trim());
		}
	}

}
