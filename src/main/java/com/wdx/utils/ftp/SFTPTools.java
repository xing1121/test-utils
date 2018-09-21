package com.wdx.utils.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.wdx.utils.DateUtil;
import com.wdx.utils.FileUtils;

/**
 * 描述：SFTP工具类，使用RemoteTools.getRemoteTools()自动获取
 * @author 80002888
 * @date   2018年4月16日
 */
public class SFTPTools extends RemoteTools{

	private static Logger logger = LoggerFactory.getLogger(SFTPTools.class);
	
	private ConnectionInfo connectionInfo;
	
	private JSch jsch = new JSch();
    private Session session = null;
    private ChannelSftp channel = null;
    
	public SFTPTools(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
	
	/**
	 * 下载单个文件（文件名更改），不删除
	 */
	@Override
	public String downloadFile(String remoteFolderPath, String localFolderPath, String fileName) {
		FileOutputStream os = null;
    	try {
    		// 参数校验
    		if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(localFolderPath) || StringUtils.isBlank(fileName)) {
    			return null;
    		}
    		// 登录
    		login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------SFTPTools...downloadFile 没有登录！");
    			return null;
			}
    		// 时间戳备份
    		String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN); 
    		String localFilePath = localFolderPath + "/" + dateStr + "-" + fileName;
    		String remoteFilePath = remoteFolderPath + "/" + fileName;
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
			}
    		// 验证远程文件路径是否是文件夹，若是文件夹，则直接返回
    		boolean directoryStatus = isDirectory(remoteFilePath);
    		if (directoryStatus) {
				return null;
			}
    		// 验证本地文件夹是否存在，不存在则创建
    		FileUtils.createDirIfNot(localFolderPath);
    		// 若本地文件已经存在，下载失败
    		File localFile = new File(localFilePath);
    		if (localFile.exists()) {
    			logger.error("----------SFTPTools...downloadFile 本地文件已经存在！" + localFilePath);
    			return null;
			}
    		// 获取输出流
    		os = new FileOutputStream(new File(localFilePath));
    		// 远程文件内容，写入输出流
			try {
				channel.get(remoteFilePath, os);
			} catch (Exception e) {
				try {
					channel.get(remoteFilePath, os);
				} catch (Exception e1) {
					channel.get(remoteFilePath, os);
				}
			}
            return localFilePath;
    	} catch (Exception e) {
    		logger.error("---------SFTPTools...downloadFile 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
    	} finally {
    		if (os != null) {
    			try {
					os.close();
				} catch (Exception e) {
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
		FileOutputStream os = null;
    	try {
    		// 参数校验
    		if (StringUtils.isBlank(remoteFolderPath) || StringUtils.isBlank(localFolderPath)) {
				return null;
			}
    		// 获取远程目录所有文件名
			List<String> fileNames = getRemoteFileNames(remoteFolderPath, prefix, suffix);
			if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
			// 登录
			login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------SFTPTools...downloadDirFiles 没有登录！");
    			return null;
			}
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
			}
			// 下载
			List<String> localFilePaths = new ArrayList<>();
			for (String fileName : fileNames) {
				// 时间戳
	    		String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN); 
	    		String localFilePath = localFolderPath + "/" + dateStr + "-" + fileName;
	    		String remoteFilePath = remoteFolderPath + "/" + fileName;
	    		// 验证远程文件路径是否是文件夹
	    		boolean directoryStatus = isDirectory(remoteFilePath);
	    		if (directoryStatus) {
					continue;
				}
	    		// 验证本地文件夹是否存在，不存在则创建
	    		File localFolder = new File(localFolderPath);
	    		if (!localFolder.exists()) {
					localFolder.mkdirs();
				}
	    		// 若本地文件已经存在，下载失败
	    		File localFile = new File(localFilePath);
	    		if (localFile.exists()) {
	    			logger.error("----------SFTPTools...downloadFile 本地文件已经存在！" + localFilePath);
	    			continue;
				}
	    		// 获取输出流
	    		os = new FileOutputStream(new File(localFilePath));
	    		// 远程文件内容，写入输出流
	            try {
					channel.get(remoteFilePath, os);
				} catch (Exception e) {
					try {
						channel.get(remoteFilePath, os);
					} catch (Exception e1) {
						channel.get(remoteFilePath, os);
					}
				}
				if (StringUtils.isNotBlank(localFilePath)) {
					localFilePaths.add(localFilePath);
				}
			}
			if (CollectionUtils.isEmpty(localFilePaths)) {
				return null;
			}
			return localFilePaths;
		} catch (Exception e) {
			logger.error("---------SFTPTools...downloadDirFiles 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
		} finally {
    		if (os != null) {
    			try {
					os.close();
				} catch (Exception e) {
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
				logger.error("----------SFTPTools...downloadFiles 移动（备份）失败！" + remoteFolderPath + "到" + remoteBackupFolderPath);
			}
		}
		return downloadFilePaths;
	}

	/**
	 * 单个上传文件（文件名更改），不删除本地
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
    			logger.error("----------SFTPTools...uploadFile 没有登录！");
    			return null;
			}
    		String localFilePath = localFolderPath + "/" + fileName;
			// 时间戳
    		String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN); 
    		String remoteFilePath = remoteFolderPath + "/" + dateStr + "-" + fileName;
    		// 验证本地文件是否存在
    		File localFile = new File(localFilePath);
    		boolean localFileExist = (localFile.isFile() && localFile.exists());
    		if (!localFileExist) {
    			logger.error("----------SFTPTools...uploadFile 本地文件不存在，或者不是文件！" + localFilePath);
    			return null;
			}
    		// 获取输入流
			in = new FileInputStream(localFile);
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
			}
			// 输入流内容，写入远程文件
			try {
				channel.put(in, remoteFilePath);
			} catch (Exception e) {
				try {
					channel.put(in, remoteFilePath);
				} catch (Exception e1) {
					channel.put(in, remoteFilePath);
				}
			}
			return remoteFilePath;
		} catch (Exception e) {
			logger.error("----------SFTPTools...uploadFile 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
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
	 * 批量上传文件（文件名更改）
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
    			logger.error("----------SFTPTools...uploadDirFiles 没有登录！");
    			return null;
			}
    		// 获取目录下所有文件
    		List<String> fileNames = FileUtils.getLocalFileNames(localFolderPath, "", "");
			if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
			}
			// 上传
			List<String> remoteFilePaths = new ArrayList<>();
			for (String fileName : fileNames) {
				// 时间戳
				String dateStr = DateUtil.dateFomat(new Date(), DATE_STR_PATTERN); 
				String localFilePath = localFolderPath + "/" + fileName;
	    		String remoteFilePath = remoteFolderPath + "/" + dateStr + "-" + fileName;
	    		// 验证本地文件是否存在
	    		File localFile = new File(localFilePath);
	    		boolean localFileExist = (localFile.isFile() && localFile.exists());
	    		if (!localFileExist) {
	    			logger.error("----------SFTPTools...uploadFile 本地文件不存在，或者不是文件！" + localFilePath);
	    			continue;
				}
	    		// 获取输入流
				in = new FileInputStream(localFile);
				// 输入流内容，写入远程文件
				try {
					channel.put(in, remoteFilePath);
				} catch (Exception e) {
					try {
						channel.put(in, remoteFilePath);
					} catch (Exception e1) {
						channel.put(in, remoteFilePath);
					}
				}
				if (StringUtils.isNotBlank(remoteFilePath)) {
					remoteFilePaths.add(remoteFilePath);
				}
			}
			if (CollectionUtils.isEmpty(remoteFilePaths)) {
				return null;
			}
			return remoteFilePaths;
		} catch (Exception e) {
			logger.error("---------SFTPTools...uploadDirFiles 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
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
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------SFTPTools...rename 没有登录！");
    			return null;
			}
    		// 时间戳
    		String dateStr = DateUtil.dateFomat(new Date(), "yyyyMMddHHmmss");
    		String remoteSourceFilePath = remoteSourceFolderPath + "/" + fileName;
    		String remoteDestFilePath = remoteDestFolderPath + "/" + dateStr + "-" + fileName;
    		// 验证远程源文件夹是否存在，不存在则创建
			try {
				channel.stat(remoteSourceFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteSourceFolderPath);
			}
    		// 验证远程目的文件夹是否存在，不存在则创建
			try {
				channel.stat(remoteDestFolderPath);
			} catch (Exception e) {
				String remoteDestFolderPathToMk = remoteDestFolderPath;
				if (remoteDestFolderPathToMk.endsWith("/")) {
					remoteDestFolderPathToMk = remoteDestFolderPathToMk.substring(0, remoteDestFolderPath.length() - 1);
				}
				if (StringUtils.isNotBlank(remoteDestFolderPathToMk)) {
					channel.mkdir(remoteDestFolderPathToMk);
				}
			}
    		// 移动
			channel.rename(remoteSourceFilePath, remoteDestFilePath);
			return remoteDestFilePath;
		} catch (Exception e) {
			logger.error("---------SFTPTools...rename 远程源文件夹路径:"+ remoteSourceFolderPath +"，远程目的文件夹路径："+ remoteDestFolderPath +"发生异常："+e.getMessage(),e);
		} finally{
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
    		// 获取远程源文件夹下所有文件名
    		List<String> fileNames = getRemoteFileNames(remoteSourceFolderPath, "", "");
			if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
    		// 登录
    		login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------SFTPTools...moveRemoteFiles 没有登录！");
    			return null;
			}
			// 时间戳
			String dateStr = DateUtil.dateFomat(new Date(), "yyyyMMddHHmmss");
			// 结果
			List<String> remoteDestFilePaths = new ArrayList<>();
			// 验证远程源文件夹是否存在，不存在则创建
			try {
				channel.stat(remoteSourceFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteSourceFolderPath);
			}
			// 验证远程目的文件夹是否存在，不存在则创建
			try {
				channel.stat(remoteDestFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteDestFolderPath);
			}
			// 循环移动
			for (String fileName : fileNames) {
				String remoteSourceFilePath = remoteSourceFolderPath + "/" + fileName;
				String remoteDestFilePath = remoteDestFolderPath + "/" + dateStr + "-" + fileName;
	    		channel.rename(remoteSourceFilePath, remoteDestFilePath);
				remoteDestFilePaths.add(remoteDestFilePath);
			}
			if (CollectionUtils.isEmpty(remoteDestFilePaths)) {
				return null;
			}
			return remoteDestFilePaths;
		} catch (Exception e) {
			logger.error("---------SFTPTools...moveRemoteFiles 远程源文件夹路径:"+ remoteSourceFolderPath +"，远程目的文件夹路径："+ remoteDestFolderPath +"发生异常："+e.getMessage(),e);
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
    			logger.error("----------SFTPTools...delete 没有登录！");
    			return false;
			}
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
			}
    		// 进入文件夹
			channel.cd(remoteFolderPath);
			// 删除远程文件
			channel.rm(fileName);
			return true;
		} catch (Exception e) {
			logger.error("---------SFTPTools...rename 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
		} finally {
		}
    	return false;
	}

	/**
	 * 批量删除
	 */
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
				logger.error("----------SFTPTools...delete 没有登录！");
				return false;
			}
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
			}
			// 循环删除
			channel.cd(remoteFolderPath); 
			for (String fileName : remoteFileNames) {
				channel.rm(fileName);
			}
			return true;
		} catch (Exception e) {
			logger.error("---------SFTPTools...delete 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
		} finally {
		}
		return false;
	}

	/**
	 * 获取远程文件夹下所有文件名
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getRemoteFileNames(String remoteFolderPath, String prefix, String suffix) {
		List<String> fileNames = null;
    	try {
    		// 参数校验
    		if (StringUtils.isBlank(remoteFolderPath)) {
				return null;
			}
    		// 登录
    		login();
    		// 验证是否登录
    		if (!checkLoginStatus()) {
    			logger.error("----------SFTPTools...getRemoteDirFileNames 没有登录！");
    			return null;
			}
    		// 验证远程文件夹是否存在，不存在则创建
    		try {
				channel.stat(remoteFolderPath);
			} catch (Exception e) {
				this.mkDirLoop(remoteFolderPath);
				return null;
			}
    		// 获取目录下所有文件
			Vector<LsEntry> vs = channel.ls(remoteFolderPath);
			if (CollectionUtils.isEmpty(vs)) {
			    return null;
			} 
			List<ChannelSftp.LsEntry> list = new ArrayList<>(vs);
	    	// 过滤
	    	fileNames = list.stream().filter((lsEntry)->{
	    		String fileName = lsEntry.getFilename();
	    		if (StringUtils.isBlank(fileName)) {
					return false;
				}
				// 过滤掉文件夹
	    		boolean directoryStatus = isDirectory(remoteFolderPath + "/" + fileName);
	    		if (directoryStatus) {
					return false;
				}
	    		// 去掉.开头的文件
	    		if (fileName.startsWith(".")) {
					return false;
				}
	    		// 前缀过滤
	    		if (StringUtils.isNotBlank(prefix)) {
					if (!fileName.startsWith(prefix)) {
						return false;
					}
				}
	    		// 后缀过滤
	    		if (StringUtils.isNotBlank(suffix)) {
					if (!fileName.endsWith(suffix)) {
						return false;
					}
				}
	    		return true;
	    	}).map(LsEntry::getFilename).collect(Collectors.toList());
	    	if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
	    	return fileNames;
		} catch (Exception e) {
			logger.error("---------SFTPTools...getRemoteDirFileNames 远程文件夹路径："+ remoteFolderPath +"发生异常："+e.getMessage(),e);
		} finally {
		}
    	return null;
	}
	
	/**
	 * 登录
	 *	@ReturnType	void 
	 *	@Date	2018年4月16日	下午7:17:50
	 *  @Param
	 */
    private void login(){
		// 如果已经登录，则返回
		if (checkLoginStatus()) {
			return;
		}
		if (connectionInfo == null) {
			logger.error("-----------SFTPTools...login 连接信息为空！");
			return;
		}
		// 基础信息
		String host = connectionInfo.getHost();
		String port = connectionInfo.getPort();
		String username = connectionInfo.getUsername();
		String password = connectionInfo.getPassword();
		// 默认端口22
		int portIntValue = 22;
		try {
			portIntValue = Integer.parseInt(port);
		} catch (NumberFormatException e) {
		}
		// 成功标识
		boolean successFlag = false;
		// 循环次数
		int count = 1;
		do {
			try {
				// 根据主机，端口，用户名，获取session
				session = jsch.getSession(username, host, portIntValue);
				// 设置密码
				session.setPassword(password);
				// 设置配置
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
				// 建立连接，设置超时时间：单位ms
				session.connect(60000);
				// 获取channel
				channel = (ChannelSftp) session.openChannel("sftp");
				// 超时时间：单位ms
				channel.connect(60000);
				if (channel.isConnected()) {
					successFlag = true;
					logger.info("Connect SFTP server success.");
				} else {
					successFlag = false;
					logger.info("Connect SFTP server fail.");
				}
			} catch (Exception e) {
				logger.error("SFTP连接失败,次数:" + count + "，主机：" + host + "，端口" + port, e);
			}
		} while (!successFlag && count++ < 5);
		if (!successFlag) {
			logger.info("SFTP登录失败！");
		}
    }
	
	/**
	 * 登出
	 *	@ReturnType	void 
	 *	@Date	2018年4月16日	下午7:17:39
	 *  @Param
	 */
    @Override
    public void logout(){
		try {
			if (!checkLoginStatus()) {
				return;
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			logger.info("成功登出!");
		} catch (Exception e) {
			logger.error("----------SFTPTools...logout 发生异常："+e.getMessage(),e);
		}
    }
	
    /**
     * 检查是否登录
     *	@ReturnType	boolean 
     *	@Date	2018年4月16日	下午7:18:01
     *  @Param  @return
     */
    private boolean checkLoginStatus(){
    	try {
			boolean loginStatus = (connectionInfo != null && session != null && channel != null && session.isConnected() && channel.isConnected());
			return loginStatus;
		} catch (Exception e) {
			logger.error("----------SFTPTools...checkLoginStatus 发生异常：" + e.getMessage(), e);
		}
    	return false;
    }
    
    /**
     * 判断FTP上的文件路径是否文件夹
     *	@ReturnType	boolean 
     *	@Date	2018年6月4日	下午1:41:06
     *  @Param  @param path
     *  @Param  @return
     */
    private boolean isDirectory(String path){
    	if (StringUtils.isBlank(path)) {
			return false;
		}
    	try {
			channel.cd(path);
		} catch (Exception e) {
			return false;
		}
    	return true;
    }
    
    /**
     * 在FTP服务器上循环创建文件夹
     *	@ReturnType	void 
     *	@Date	2018年6月4日	下午4:13:22
     *  @Param  @param remoteFolderPath
     */
    private void mkDirLoop(String remoteFolderPath){
    	try {
			if (StringUtils.isBlank(remoteFolderPath)) {
				return;
			}
			String[] folders = remoteFolderPath.split("/");
			if (folders == null || folders.length == 0) {
				return;
			}
			String basePath = "";
			for (String folderPath : folders) {
				if (StringUtils.isBlank(folderPath)) {
					continue;
				}
				basePath += "/" + folderPath;
				try {
					channel.stat(basePath);
				} catch (Exception e) {
					channel.mkdir(basePath);
				}
			}
		} catch (Exception e) {
			logger.error("----------SFTPTools...mkDirLoop 将要创建文件夹路径："+ remoteFolderPath +"发生异常：" + e.getMessage(), e);
		}
    }

}