package com.wdx.utils.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wdx.utils.DateUtil;
import com.wdx.utils.FileUtils;

/**
 * 描述：FTP工具类，使用RemoteTools.getRemoteTools()自动获取
 * @author 80002888
 * @date   2018年4月18日
 */
public class FTPTools extends RemoteTools {
	
	private static Logger logger = LoggerFactory.getLogger(FTPTools.class);
	
	private static String encoding = System.getProperty("file.encoding");
	
	private FTPClient ftpClient; 
	private ConnectionInfo connectionInfo;
	
	public FTPTools(ConnectionInfo connectionInfo){
		this.connectionInfo = connectionInfo;
	}
	
	/**
	 * 下载远程单个文件（文件名更改），不删除远程
	 */
	@Override
	public String downloadFile(String remoteFolderPath, String localFolderPath, String fileName) {
		OutputStream os = null;
		try {
			// 参数校验
			if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(localFolderPath) || StringUtils.isBlank(fileName)) {
				return null;
			}
			// 登录
			login();
			// 检查登录状态
			if (!checkLoginStatus()) {
				logger.error("没有登录！");
				return null;
			}
			// 创建目录
			ftpClient.makeDirectory(remoteFolderPath);
            // 转移到FTP服务器目录至指定的目录下  
            ftpClient.changeWorkingDirectory(new String(remoteFolderPath.getBytes(encoding),"iso-8859-1"));  
            // 获取FTP文件列表  
            FTPFile[] fs = ftpClient.listFiles();
            if (fs == null || fs.length == 0) {
				return null;
			}
            // 如果目标文件是文件夹则不下载
            for (FTPFile ftpFile : fs) {
            	boolean directoryStatus = ftpFile.isDirectory() && ftpFile.getName().equals(fileName);
            	if (directoryStatus) {
            		return null;
            	}
			}
            // 创建本地目录
            FileUtils.createDirIfNot(localFolderPath);
            // 远程文件全路径
            String remoteFilePath = remoteFolderPath + "/" + fileName;
			// 时间戳
    		String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN);
    		// 本地文件全路径
            String localFilePath = localFolderPath + "/" + dateStr + "-" + fileName;
            // 创建本地文件
            File localFile = FileUtils.createFile(localFilePath);
            // 获取输出流
            os = new FileOutputStream(localFile); 
        	// 下载
            boolean res = ftpClient.retrieveFile(remoteFilePath, os);
            // 判断是否下载成功
            if (res) {
            	return localFilePath;
			} else{
				return null;
			}
		} catch (Exception e) {
			logger.error("发生异常",e);
		} finally {
			if(null != os){
				try {
					os.close();
				} catch (IOException e) {
				}  
			}
		}
		return null;
	}

	/**
	 * 批量下载（文件名更改），不删除 
	 */
	@Override
	public List<String> downloadFiles(String remoteFolderPath, String localFolderPath, String prefix, String suffix) {
		OutputStream os = null;
		try {
			// 参数校验
			if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(localFolderPath)) {
				return null;
			}
			// 登录
			login();
			// 检查登录状态
			if (!checkLoginStatus()) {
				logger.error("没有登录！");
				return null;
			}
			// 创建目录
			ftpClient.makeDirectory(remoteFolderPath);
			// 转移到FTP服务器目录至指定的目录下  
            ftpClient.changeWorkingDirectory(new String(remoteFolderPath.getBytes(encoding),"iso-8859-1"));  
            // 获取FTP文件列表  
            FTPFile[] fs = ftpClient.listFiles();
            if (fs == null || fs.length == 0) {
				return null;
			}
            // 创建本地目录
            FileUtils.createDirIfNot(localFolderPath);
            // 结果
            List<String> localFilePaths = new ArrayList<>();
            // 找到目标文件并下载
            for (FTPFile ftpFile : fs) {
            	String fileName = ftpFile.getName();
            	if (StringUtils.isBlank(fileName)) {
					continue;
				}
				// 过滤掉文件夹
				boolean directoryStatus = ftpFile.isDirectory();
				if (directoryStatus) {
					continue;
				}
            	// 过滤.开头的文件
            	if (fileName.startsWith(".")) {
					continue;
				}
            	// 前缀过滤
            	if (StringUtils.isNotBlank(prefix) && !fileName.startsWith(prefix)) {
					continue;
				}
            	// 后缀过滤
            	if (StringUtils.isNotBlank(suffix) && !fileName.endsWith(suffix)) {
					continue;
				}
				// 时间戳
	    		String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN);
	    		// 远程文件全路径
            	String remoteFilePath = remoteFolderPath + "/" + fileName;
            	// 本地文件全路径
            	String localFilePath = localFolderPath + "/" + dateStr + "-" + fileName;
            	// 创建本地文件
            	File localFile = FileUtils.createFile(localFilePath);
            	// 获取输出流
            	os = new FileOutputStream(localFile);
            	// 下载
                boolean res = ftpClient.retrieveFile(remoteFilePath, os);
                // 判断是否下载成功
                if (res) {
                	localFilePaths.add(localFilePath);
				}
            }  
            if (CollectionUtils.isEmpty(localFilePaths)) {
				return null;
			}
            return localFilePaths;
		} catch (Exception e) {
			logger.error("发生异常",e);
		} finally {
			if(null != os){
				try {
					os.close();
				} catch (IOException e) {
				}  
			}
		}
		return null;
	}

	/**
	 * 批量下载（文件名更改），备份（文件名更改，为空则不备份，不删除）
	 */
	@Override
	public List<String> downloadFiles(String remoteFolderPath, String localFolderPath, String remoteBackupFolderPath,
			String prefix, String suffix) {
		// 下载
		List<String> downloadFilePaths = downloadFiles(remoteFolderPath, localFolderPath, prefix, suffix);
		if (CollectionUtils.isEmpty(downloadFilePaths)) {
			return null;
		}
		if (StringUtils.isNotBlank(remoteBackupFolderPath)) {
			// 移动（备份）
			List<String> removeBackupFilePaths = moveAll(remoteFolderPath, remoteBackupFolderPath);
			if (CollectionUtils.isEmpty(removeBackupFilePaths)) {
				logger.error("----------FTPTools...downloadFiles 移动（备份）失败！" + remoteFolderPath + "到" + remoteBackupFolderPath);
			}
		}
		return downloadFilePaths;
	}

	/**
	 * 单个上传文件（文件名更改）
	 */
	@Override
	public String uploadFile(String remoteFolderPath, String localFolderPath, String fileName) {
		FileInputStream in = null;
    	try {
    		// 参数校验
    		if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(localFolderPath) || StringUtils.isBlank(fileName)) {
    			return null;
    		}
    		// 登录
    		login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------FTPTools...uploadFile 没有登录！");
    			return null;
			}
    		// 本地文件路径
    		String localFilePath = localFolderPath + "/" + fileName;
			// 时间戳
    		String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN);
    		// 远程文件路径
    		String remoteFilePath = remoteFolderPath + "/" + dateStr + "-" + fileName;
    		// 验证本地文件是否存在
    		File localFile = new File(localFilePath);
    		boolean localFileExist = (localFile.isFile() && localFile.exists());
    		if (!localFileExist) {
    			logger.error("----------FTPTools...uploadFile 本地文件不存在，或者不是文件！" + localFilePath);
    			return null;
			}
    		// 创建远程文件夹
    		ftpClient.makeDirectory(remoteFolderPath);
    		// 转移工作目录
    		ftpClient.changeWorkingDirectory(remoteFolderPath);
    		// 获取输入流
			in = new FileInputStream(localFile);
			// 输入流内容，写入远程文件
			boolean res = ftpClient.storeFile(new String(remoteFilePath.getBytes(encoding), "iso-8859-1"), in);
			if (res) {
				return remoteFilePath;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("----------FTPTools...uploadFile 发生异常："+e.getMessage(),e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
    	return null;
	}

	/**
	 * 批量上传（文件名更改）
	 */
	@Override
	public List<String> uploadFiles(String remoteFolderPath, String localFolderPath) {
		FileInputStream in = null;
    	try {
    		// 参数校验
    		if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(localFolderPath)) {
    			return null;
    		}
    		// 登录
    		login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------FTPTools...uploadFile 没有登录！");
    			return null;
			}
       		// 创建远程文件夹
    		ftpClient.makeDirectory(remoteFolderPath);
    		// 转移工作目录
    		ftpClient.changeWorkingDirectory(remoteFolderPath);
    		// 获取本地文件夹下所有文件
    		List<String> localFileNames = FileUtils.getLocalFileNames(localFolderPath, null, null);
    		if (CollectionUtils.isEmpty(localFileNames)) {
				return null;
			}
    		// 结果
    		List<String> remoteFilePaths = new ArrayList<>();
    		// 循环上传
    		for (String fileName : localFileNames) {
    			// 本地文件路径
    			String localFilePath = localFolderPath + "/" + fileName;
    			// 时间戳
    			String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN);
    			// 远程文件路径
    			String remoteFilePath = remoteFolderPath + "/" + dateStr + "-" + fileName;
    			// 本地文件
    			File localFile = new File(localFilePath);
    			// 验证本地文件是否存在
    			boolean localFileExist = (localFile.isFile() && localFile.exists());
    			if (!localFileExist) {
    				logger.error("----------FTPTools...uploadFile 本地文件不存在，或者不是文件！" + localFilePath);
    				continue;
    			}
    			// 获取输入流
    			in = new FileInputStream(localFile);
    			// 输入流内容，写入远程文件
    			boolean res = ftpClient.storeFile(new String(remoteFilePath.getBytes(encoding), "iso-8859-1"), in);
    			if (res) {
					remoteFilePaths.add(remoteFilePath);
				}
			}
    		if (CollectionUtils.isEmpty(remoteFilePaths)) {
				return null;
			}
    		return remoteFilePaths;
		} catch (Exception e) {
			logger.error("----------FTPTools...uploadFile 发生异常："+e.getMessage(),e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
    	return null;
	}

	/**
	 * 移动文件（文件名更改） 
	 */
	@Override
	public String move(String remoteSourceFolderPath, String remoteDestFolderPath, String fileName) {
		try {
			// 参数校验
			if (StringUtils.isBlank(remoteSourceFolderPath) || StringUtils.isBlank(remoteDestFolderPath) || StringUtils.isBlank(fileName)) {
				return null;
			}
			// 登录
			login();
			// 检查登录状态
			if (!checkLoginStatus()) {
				logger.error("没有登录！");
				return null;
			}
			// 源文件全路径
            String remoteSourceFilePath = remoteSourceFolderPath + "/" + fileName;
            // 时间戳
            String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN); 
            // 目标文件全路径
            String remoteDestFilePath = remoteDestFolderPath + "/" + dateStr + "-" + fileName;
            // 创建远程目的文件夹
            ftpClient.makeDirectory(remoteDestFolderPath);
            // 移动
            ftpClient.rename(new String(remoteSourceFilePath.getBytes(),"iso-8859-1"), new String(remoteDestFilePath.getBytes(encoding),"iso-8859-1"));
            return remoteDestFilePath;
		} catch (Exception e) {
			logger.error("get error->",e);
		} finally {
		}
		return null;
	}

	/**
	 * 批量移动文件（文件名更改） 
	 */
	@Override
	public List<String> moveAll(String remoteSourceFolderPath, String remoteDestFolderPath) {
		try {
			// 参数校验
			if (StringUtils.isBlank(remoteSourceFolderPath) || StringUtils.isBlank(remoteDestFolderPath)) {
				return null;
			}
			// 获取远程文件夹下所有文件名
			List<String> fileNames = getRemoteFileNames(remoteSourceFolderPath, "", "");
			if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
			// 登录
			login();
			// 检查登录状态
			if (!checkLoginStatus()) {
				logger.error("没有登录！");
				return null;
			}
			// 创建远程目的文件夹
            ftpClient.makeDirectory(remoteDestFolderPath);
            // 结果
            List<String> remoteFilePaths = new ArrayList<>();
			// 循环移动
			for (String fileName : fileNames) {
				// 源文件全路径
	            String remoteSourceFilePath = remoteSourceFolderPath + "/" + fileName;
	            // 时间戳
	            String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN); 
	            // 目标文件全路径
	            String remoteDestFilePath = remoteDestFolderPath + "/" + dateStr + "-" + fileName;
	            // 移动
	            boolean res = ftpClient.rename(new String(remoteSourceFilePath.getBytes(),"iso-8859-1"), new String(remoteDestFilePath.getBytes(encoding),"iso-8859-1"));
	            if (res) {
	            	remoteFilePaths.add(remoteDestFilePath);
				}
			}
			if (CollectionUtils.isEmpty(remoteFilePaths)) {
				return null;
			}
			return remoteFilePaths;
		} catch (Exception e) {
			logger.error("get error->",e);
		} finally {
		}
		return null;
	}

	/**
	 * 删除一个文件
	 */
	@Override
	public boolean deleteFile(String remoteFolderPath, String fileName) {
    	try {
    		// 参数校验
    		if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(fileName)) {
				return false;
			}
    		// 登录
    		login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------FTPTools...delete 没有登录！");
    			return false;
			}
    		// 创建目录
    		ftpClient.makeDirectory(remoteFolderPath);
    		// 进入远程文件夹
    		ftpClient.changeWorkingDirectory(remoteFolderPath);
    		// 远程文件全路径
    		String remoteFilePath = remoteFolderPath + "/" + fileName;
    		// 删除远程文件
    		boolean res = ftpClient.deleteFile(remoteFilePath);
			return res;
		} catch (Exception e) {
			logger.error("---------FTPTools...rename 发生异常："+e.getMessage(),e);
		} finally {
		}
    	return false;
	}

	@Override
	public boolean deleteFiles(String remoteFolderPath) {
    	try {
			// 参数校验
			if (StringUtils.isBlank(remoteFolderPath)) {
				return false;
			}
			// 获取远程文件夹下所有文件名
			List<String> remoteFileNames = getRemoteFileNames(remoteFolderPath, null, null);
			if (CollectionUtils.isEmpty(remoteFileNames)) {
				return true;
			}
			// 登录
			login();
			// 验证是否登录
			if (!checkLoginStatus()) {
				logger.error("----------FTPTools...delete 没有登录！");
				return false;
			}
			// 创建目录
			ftpClient.makeDirectory(remoteFolderPath);
			// 进入目录
			ftpClient.changeWorkingDirectory(remoteFolderPath);
			// 循环删除
			for (String fileName : remoteFileNames) {
				ftpClient.deleteFile(fileName);
			}
			return true;
		} catch (Exception e) {
			logger.error("---------FTPTools...delete 发生异常："+e.getMessage(),e);
		} finally {
		}
		return false;
	}

	/**
	 * 获取远程文件夹下所有文件名 
	 */
	@Override
	public List<String> getRemoteFileNames(String remoteFolderPath, String prefix, String suffix) {
		try {
			// 参数校验
			if (StringUtils.isBlank(remoteFolderPath)) {
				return null;
			}
			// 登录
			login();
			// 检查登录状态
			if (!checkLoginStatus()) {
				logger.error("没有登录！");
				return null;
			}
			// 创建目录
			ftpClient.makeDirectory(remoteFolderPath);
			// 进入文件夹
			ftpClient.changeWorkingDirectory(remoteFolderPath);
			// 获取所有文件
			FTPFile[] ftpFiles = ftpClient.listFiles();
			if (ftpFiles == null || ftpFiles.length == 0) {
				return null;
			}
			
			// 结果
			List<String> fileNames = new ArrayList<>();
			// 循环获取
			for (FTPFile ftpFile : ftpFiles) {
				// 过滤掉文件夹
				boolean directoryStatus = ftpFile.isDirectory();
				if (directoryStatus) {
					continue;
				}
				String fileName = ftpFile.getName();
	           	// 过滤.开头的文件
            	if (fileName.startsWith(".")) {
					continue;
				}
            	// 前缀过滤
            	if (StringUtils.isNotBlank(prefix) && !fileName.startsWith(prefix)) {
					continue;
				}
            	// 后缀过滤
            	if (StringUtils.isNotBlank(suffix) && !fileName.endsWith(suffix)) {
					continue;
				}
            	fileNames.add(fileName);
			}
			if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
            return fileNames;
		} catch (Exception e) {
			logger.error("get error->",e);
		} finally {
		}
		return null;
	}

	/*************************************************************************************/
	/*************************************************************************************/
	/*************************************************************************************/
	
	/**
	 * 登录
	 *	@ReturnType	void 
	 *	@Date	2018年4月17日	上午9:16:14
	 *  @Param
	 */
	private void login() {
		// 是否已连接
		if (checkLoginStatus()) {
			return;
		}
		ftpClient = new FTPClient();
		String host = connectionInfo.getHost();
		String port = connectionInfo.getPort();
		String username = connectionInfo.getUsername();
		String password = connectionInfo.getPassword();
		// 默认端口21
		int portIntValue = 21;
		try {
			portIntValue = Integer.parseInt(port);
		} catch (NumberFormatException e) {
		}
		// 成功标识
		boolean successFlag = false;
		// 循环次数
		int count = 1;
		do{
			try {
			    ftpClient.setConnectTimeout(100*1000);
			    ftpClient.setDefaultTimeout(100*1000);
			    ftpClient.setDataTimeout(100*1000);
			    // 连接
			    if(!ftpClient.isConnected()){
			    	ftpClient.connect(host, portIntValue);
			    }
				// 登录
				ftpClient.login(username, password);
				ftpClient.setControlEncoding(encoding);
				// 设置连接模式为被动模式
				ftpClient.enterLocalPassiveMode();
				// 设置文件传输类型为二进制  
	            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  
				// 检验是否连接成功
				int reply = ftpClient.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					successFlag = false;
					logger.info("Connect FTP server fail.");
				} else {
					successFlag = true;
					logger.info("Connect FTP server success.");
				}
			} catch (Exception e) {
				/// 敏感信息
//				logger.error("FTP连接失败,次数:" + count + "，主机：" + host + "，端口" + port + "，用户名：" + username + "，密码：" + password, e);
				logger.error("FTP连接失败,次数:" + count + "，主机：" + host + "，端口" + port, e);
			}
		} while(!successFlag && count++ < 5);
		if (!successFlag) {
			logger.info("FTP登录失败！");
		}
	}
	
	/**
	 * 登出
	 *	@ReturnType	void 
	 *	@Date	2018年4月17日	上午9:16:34
	 *  @Param
	 */
	@Override
	public void logout() {
		try {
			if (!checkLoginStatus()) {
				return;
			}
			if (ftpClient != null) {
				ftpClient.logout();
			}
			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
			logger.info("成功登出！");
		} catch (Exception e) {
			logger.error("----------FTPTools...logout 发生异常：" + e.getMessage(), e);
		}
	}
	
	/**
	 * 检查是否登录
	 *	@ReturnType	boolean 
	 *	@Date	2018年4月17日	上午9:16:58
	 *  @Param  @return
	 */
	public boolean checkLoginStatus() {
    	try {
			boolean loginStatus = (connectionInfo != null && ftpClient != null && ftpClient.isConnected() && FTPReply.isPositiveCompletion(ftpClient.getReplyCode()));
			return loginStatus;
		} catch (Exception e) {
			logger.error("----------FTPTools...checkLoginStatus 发生异常：" + e.getMessage(), e);
		}
    	return false;
	}
	    
}
