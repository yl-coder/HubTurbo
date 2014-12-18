package scripting;

import model.TurboMilestone;

public class Milestone {
	private int number;
	private String title;
	private String state;
	private String description;
	private String dueOnString;
	private int closed;
	private int open;
	
	@Override
	public String toString() {
		return String.format("Milestone %s", title); 
	}

	// Getter/setter boilerplate

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDueOnString() {
		return dueOnString;
	}

	public void setDueOnString(String dueOnString) {
		this.dueOnString = dueOnString;
	}

	public int getClosed() {
		return closed;
	}

	public void setClosed(int closed) {
		this.closed = closed;
	}

	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	public Milestone(TurboMilestone milestone) {
		this.number = milestone.getNumber();
		this.title = milestone.getTitle();
		this.state = milestone.getState();
		this.description = milestone.getDescription();
		this.dueOnString = milestone.getDueOnString();
		this.closed = milestone.getClosed();
		this.open = milestone.getOpen();
	}
}
