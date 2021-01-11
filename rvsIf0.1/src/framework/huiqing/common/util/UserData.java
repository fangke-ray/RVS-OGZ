package framework.huiqing.common.util;

import java.io.Serializable;
import java.util.Set;

public class UserData implements Serializable {

	private static final long serialVersionUID = 2826195828414141429L;

	private String userid;
	private Set<Integer> privacies;

	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public Set<Integer> getPrivacies() {
		return privacies;
	}
	public void setPrivacies(Set<Integer> privacies) {
		this.privacies = privacies;
	}

}
