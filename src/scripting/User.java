package scripting;

import model.TurboUser;

public class User {
	private String githubName = "";
	private String realName;
	private String avatarUrl = "";
	private String alias = "";
	
	public User(TurboUser user) {
		this.githubName = user.getGithubName();
		this.realName = user.getRealName();
		this.avatarUrl = user.getAvatarUrl();
		this.alias = user.getAlias();
	}

	// Getter/setter boilerplate

	public String getGithubName() {
		return githubName;
	}

	public void setGithubName(String githubName) {
		this.githubName = githubName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}	
}
