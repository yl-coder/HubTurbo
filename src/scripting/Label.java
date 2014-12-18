package scripting;

import model.TurboLabel;

public class Label {
	private String name;
	private String colour;
	private String group;
	private boolean isExclusive;
	
	public Label(TurboLabel label) {
		this.name = label.getName();
		this.colour = label.getColour();
		this.group = label.getGroup();
		this.isExclusive = label.isExclusive();
	}

	// Getter/setter boilerplate

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public boolean isExclusive() {
		return isExclusive;
	}

	public void setExclusive(boolean isExclusive) {
		this.isExclusive = isExclusive;
	}	
}
