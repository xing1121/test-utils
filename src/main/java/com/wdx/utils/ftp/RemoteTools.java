package com.wdx.utils.ftp;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wdx.utils.PropertiesUtils;

/**
 * 描述：远程工具类，ftp/sftp，使用RemoteTools.getRemoteTools()自动获取，使用完毕需要调用logout();
 * @author 80002888
 * @date   2018年4月16日
 */
public abstract class RemoteTools {
	
	private final static Logger logger = LoggerFactory.getLogger(RemoteTools.class);

	public final static String DATE_STR_PATTERN = "yyyyMMddHHmmss";
	
	/**
	 * FTP
	 */
	private final static String FTP = "FTP";
	
	/**
	 * SFTP
	 */
	private final static String SFTP = "SFTP";
	
	/**
	 * 获取远程服务器文件工具类
	 *	@ReturnType	RemoteTools 
	 *	@Date	2018年4月16日	下午4:30:59
	 *  @Param  @return
	 */
	public static RemoteTools getRemoteTools(){
		try {
			String ftpOrSftp = PropertiesUtils.getValue("/properties/application.properties", "ftp_or_sftp");
			if (StringUtils.isBlank(ftpOrSftp)) {
				// 默认为FTP
				ftpOrSftp = FTP;
			}
			// 构建FTP工具类
			if (FTP.equals(ftpOrSftp.trim().toUpperCase())) {
				String ftpHost = PropertiesUtils.getValue("/properties/application.properties", "ftp_url");
				String ftpPort = PropertiesUtils.getValue("/properties/application.properties", "ftp_port");
				if (StringUtils.isBlank(ftpPort)) {
					ftpPort = "21";
				}
				String ftpUsername = PropertiesUtils.getValue("/properties/application.properties", "ftp_username");
				String ftpPassword = PropertiesUtils.getValue("/properties/application.properties", "ftp_password");
				ConnectionInfo connectionInfo = new ConnectionInfo(ftpHost, ftpPort, ftpUsername, ftpPassword);
				return new FTPTools(connectionInfo);
			}
			// 构建SFTP工具类
			if (SFTP.equals(ftpOrSftp.trim().toUpperCase())) {
				String sftpHost = PropertiesUtils.getValue("/properties/application.properties", "sftp_url");
				String sftpPort = PropertiesUtils.getValue("/properties/application.properties", "sftp_port");
				if (StringUtils.isBlank(sftpPort)) {
					sftpPort = "22";
				}
				String sftpUsername = PropertiesUtils.getValue("/properties/application.properties", "sftp_username");
				String sftpPassword = PropertiesUtils.getValue("/properties/application.properties", "sftp_password");
				ConnectionInfo connectionInfo = new ConnectionInfo(sftpHost, sftpPort, sftpUsername, sftpPassword);
				return new SFTPTools(connectionInfo);
			}
		} catch (Exception e) {
			logger.error("get error->",e);
		}
		return null;
	};
	
	/**
	 * 下载远程单个文件（文件名更改），不删除远程
	 *	@ReturnType	String 
	 *	@Date	2018年4月16日	下午4:37:40
	 *  @Param  @param remoteFolderPath		远程文件夹
	 *  @Param  @param localFolderPath		本地文件夹
	 *  @Param  @param fileName				文件名
	 *  @Param  @return						下载文件的全路径
	 */
	public abstract String downloadFile(String remoteFolderPath, String localFolderPath, String fileName);
	
	
	/**
	 * 批量下载（文件名更改），不删除
	 *	@ReturnType	List<String> 
	 *	@Date	2018年4月16日	下午4:39:19
	 *  @Param  @param remoteFolderPath		远程文件夹
	 *  @Param  @param localFolderPath		本地文件夹
	 *  @Param  @param prefix				前缀
	 *  @Param  @param suffix				后缀
	 *  @Param  @return						下载文件的全路径集合
	 */
	public abstract List<String> downloadFiles(String remoteFolderPath, String localFolderPath, String prefix, String suffix);

	/**
	 * 批量下载（文件名更改），备份（文件名更改，为空则不备份，不删除）
	 *	@ReturnType	List<String> 
	 *	@Date	2018年4月16日	下午5:03:11
	 *  @Param  @param remoteFolderPath				远程文件夹
	 *  @Param  @param localFolderPath				本地文件夹
	 *  @Param  @param remoteBackupFolderPath		远程备份文件夹
	 *  												若不为null，则下载完会将文件备份到备份文件夹
	 *  												若为null，则不备份，也不删除
	 *  @Param  @param prefix						前缀
	 *  @Param  @param suffix						后缀
	 *  @Param  @return
	 */
	public abstract List<String> downloadFiles(String remoteFolderPath, String localFolderPath, String remoteBackupFolderPath, String prefix, String suffix);
	
	/**
	 * 上传本地单个文件到远程
	 *	@ReturnType	String 
	 *	@Date	2018年4月16日	下午4:55:59
	 *  @Param  @param remoteFolderPath			远程文件夹
	 *  @Param  @param localFolderPath			本地文件夹
	 *  @Param  @param fileName					文件名
	 *  @Param  @return							远程文件全路径
	 */
    public abstract String uploadFile(String remoteFolderPath, String localFolderPath, String fileName);
	
	/**
	 * 上传本地文件夹下所有文件到远程
	 *	@ReturnType	List<String> 
	 *	@Date	2018年4月16日	下午4:56:59	
	 *  @Param  @param remoteFolderPath			远程文件夹
	 *  @Param  @param localFolderPath			本地文件夹
	 *  @Param  @return							远程文件全路径集合
	 */
	public abstract List<String> uploadFiles(String remoteFolderPath, String localFolderPath);
	
	/**
	 * 移动远程单个文件
	 *	@ReturnType	String 
	 *	@Date	2018年4月16日	下午4:40:15
	 *  @Param  @param remoteSourceFolderPath		远程源文件夹
	 *  @Param  @param remoteDestFilePath			远程目的文件夹
	 *  @Param  @param fileName						文件名
	 *  @Param  @return								移动后的文件全路径
	 */
	public abstract String move(String remoteSourceFolderPath, String remoteDestFolderPath, String fileName);
	
	/**
	 * 移动远程文件夹下所有文件
	 *	@ReturnType	List<String> 
	 *	@Date	2018年4月16日	下午4:43:03
	 *  @Param  @param remoteSourceFolderPath		远程源文件夹
	 *  @Param  @param remoteDestFolderPath			远程目的文件夹
	 *  @Param  @return								移动后的文件全路径集合
	 */
	public abstract List<String> moveAll(String remoteSourceFolderPath, String remoteDestFolderPath);
	
	/**
	 * 删除远程单个文件
	 *	@ReturnType	boolean 
	 *	@Date	2018年4月16日	下午4:44:14
	 *  @Param  @param remoteFolderPath				远程文件夹
	 *  @Param  @param fileName						文件名
	 *  @Param  @return								是否删除成功
	 */
	public abstract boolean deleteFile(String remoteFolderPath, String fileName); 
	
	/**
	 * 删除远程文件夹下所有文件
	 *	@ReturnType	boolean 
	 *	@Date	2018年4月16日	下午4:44:49
	 *  @Param  @param remoteFolderPath				远程文件夹
	 *  @Param  @return								是否删除成功
	 */
	public abstract boolean deleteFiles(String remoteFolderPath);
	
	/**
	 * 获取远程文件夹下所有文件名
	 *	@ReturnType	List<String> 
	 *	@Date	2018年4月16日	下午4:46:02
	 *  @Param  @param remoteFolderPath			远程文件夹
	 *  @Param  @param prefix					前缀
	 *  @Param  @param suffix					后缀
	 *  @Param  @return							文件名的集合（不包括路径）
	 */
	public abstract List<String> getRemoteFileNames(String remoteFolderPath, String prefix, String suffix);
	
	/**
	 * 登出
	 *	@ReturnType	void 
	 *	@Date	2018年4月18日	下午2:46:16
	 *  @Param
	 */
	public abstract void logout();
	
}
