package space.mmty.exception;

public class Action extends RuntimeException {
	private Object bring;
	private String url;
	public Action(String url) {
		this.url = url;
	}
	public Action bring(Object bring) {
		this.bring = bring;
		return this;
	}

	public Object getBring() {
		return bring;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
