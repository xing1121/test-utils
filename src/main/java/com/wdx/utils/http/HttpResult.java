package com.wdx.utils.http;

public class HttpResult {

	private Integer status;

	private String content;

	public HttpResult() {
	}

	public HttpResult(Integer status, String content) {
		this.status = status;
		this.content = content;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "HttpResult [status=" + status + ", content=" + content + "]";
	}

}
