package scripting;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class JSIssue {

//	private String title;
	
	public JSIssue(String t) {
		setTitle(t);
	}
	public JSIssue() {
	}
	private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {
    	return title.get();
//    	return title;
    }
    public final void setTitle(String value) {
    	title.set(value);
//    	title = value;
    }
    public StringProperty titleProperty() {
    	return title;
    }
}