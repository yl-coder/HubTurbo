package scripting;

import java.util.List;
import java.util.stream.Collectors;

import model.TurboIssue;

public class Issue {
	private String creator;
	private String createdAt;
	private int numOfComments;
	private int id;
	private String title;
	private String description;
	private String cachedDescriptionMarkup;
	private int parentIssue;
	private boolean state;
	private User assignee;
	private Milestone milestone;
	private String htmlUrl;
	private List<Label> labels;
	
	public Issue() {}
	
	public Issue(TurboIssue issue) {
		this.creator = issue.getCreator();
		this.createdAt = issue.getCreatedAt();
		this.numOfComments = issue.getNumOfComments();
		this.id = issue.getId();
		this.title = issue.getTitle();
		this.description = issue.getDescription();
		this.cachedDescriptionMarkup = issue.getDescriptionMarkup();
		this.parentIssue = issue.getParentIssue();
		this.state = issue.getOpen();
		this.assignee = issue.getAssignee() == null ? null : new User(issue.getAssignee());
		this.milestone = issue.getMilestone() == null ? null : new Milestone(issue.getMilestone());
		
		this.htmlUrl = issue.getHtmlUrl();
		this.labels = issue.getLabels().stream().map(label -> new Label(label)).collect(Collectors.toList());
	}
	
	// Getter/setter boilerplate

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(int numOfComments) {
		this.numOfComments = numOfComments;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCachedDescriptionMarkup() {
		return cachedDescriptionMarkup;
	}

	public void setCachedDescriptionMarkup(String cachedDescriptionMarkup) {
		this.cachedDescriptionMarkup = cachedDescriptionMarkup;
	}

	public int getParentIssue() {
		return parentIssue;
	}

	public void setParentIssue(int parentIssue) {
		this.parentIssue = parentIssue;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}
}
