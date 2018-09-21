package com.wdx.utils.ftp;

/**
 * 描述：远程连接信息类（SFTP、FTP）
 * 
 * @author 80002888
 * @date 2018年4月16日
 */
public class ConnectionInfo {

	/**
	 * 主机
	 */
	private String host;

	/**
	 * 端口
	 */
	private String port;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 密码
	 */
	private String password;
	
	public ConnectionInfo() {
	}

	public ConnectionInfo(String host, String port, String username, String password) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "RemoteConnectionInfo [host=" + host + ", port=" + port + ", username=" + username + ", password="
				+ password + "]";
	}

}
