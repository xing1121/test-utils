package com.wdx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	

	private static final Integer DEL_FAILED_MAX_TIME = 10;
	
	/**
	 * 字符串写入文件（覆盖原有）
	 *	@ReturnType	void 
	 *	@Date	2018年7月27日	下午5:41:08
	 *  @Param  @param content
	 *  @Param  @param pathStr
	 */
	public static void writeContent(String content, String pathStr){
		try {
			Files.write(Paths.get(pathStr), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 强制根据文件路径删除本地文件，若失败会循环10次
	 *	@ReturnType	boolean 
	 *	@Date	2018年4月8日	上午9:38:54
	 *  @Param  @param filePath
	 *  @Param  @return
	 */
	public static boolean forceDeleteLocalFile(String filePath){
		if (StringUtils.isBlank(filePath)) {
			return false;
		}
		File file = new File(filePath);
		if (file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		boolean result = file.delete();
		int tryCount = 0;
		while (!result && tryCount++ < DEL_FAILED_MAX_TIME) {
			System.gc();    //回收资源
			result = file.delete();
		}
		return result;
	}
	
	/**
	 * 根据文件的路径，返回文件中的字符串
	 *	@ReturnType	String 
	 *	@Date	2018年4月8日	下午2:38:07
	 *  @Param  @param filePath
	 *  @Param  @return
	 */
	public static String readContent(String filePath){
		// 把File中的字符都读入message
		StringBuffer message = null;
		// 输入流
		InputStream is = null;
		try {
			File file = new File(filePath);
			if (file == null || !file.exists() || !file.isFile()) {
				return null;
			}
			is = new FileInputStream(file);
			byte[] bs = new byte[1024*100];
			byte[] b2 = null;
			int len = 0;
			while ((len = is.read(bs)) != -1 ) {
				if (message == null) {
					message = new StringBuffer();
				}
				b2 = new byte[len];
				System.arraycopy(bs, 0, b2, 0, len);
				message = message.append(new String(b2));
			}
			if (StringUtils.isBlank(message)) {
				return null;
			}
		} catch (Exception e) {
			logger.error("--------DewellEventProcessImpl...getStringFromFile...尝试处理xml文件（读取内容字符串）时："+filePath+"...发生异常："+e.getMessage(),e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return message.toString();
	}
	
	 /**
     * 获取本地目录下的所有文件名
     *	@ReturnType	List<String> 
     *	@Date	2018年4月16日	下午2:39:44
     *  @Param  @param localFolderPath		本地文件夹
     *  @Param  @return						文件名的集合（不包括路径）
     */
    public static List<String> getLocalFileNames(String localFolderPath, String prefix, String suffix) {
    	List<String> fileNames = null;
    	try {
    		if (StringUtils.isBlank(localFolderPath)) {
				return null;
			}
			File folder = new File(localFolderPath);
			boolean fileDoNotExistOrNotFolder = (!folder.exists() || !folder.isDirectory()); 
			if (fileDoNotExistOrNotFolder) {
				return null;
			}
			File[] filesArray = folder.listFiles();
			if (filesArray == null || filesArray.length == 0) {
				return null;
			}
			List<File> files = new ArrayList<>(Arrays.asList(filesArray));
			if (CollectionUtils.isEmpty(files)) {
				return null;
			}
			String ponitStr = ".";
			files.removeIf((file)->{
				String fileName = file.getName();
				if (StringUtils.isBlank(fileName)) {
					return true;
				}
				if (fileName.startsWith(ponitStr)) {
					return true;
				}
				if (StringUtils.isNotBlank(prefix) && !fileName.startsWith(prefix)) {
					return true;
				}
				if (StringUtils.isNotBlank(suffix) && !fileName.endsWith(suffix)) {
					return true;
				}
				return false;
			});
			if (CollectionUtils.isEmpty(files)) {
				return null;
			}
			fileNames = files.stream().map(File::getName).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(fileNames)) {
				return null;
			}
			return fileNames;
		} catch (Exception e) {
			logger.error("---------FileUtils...getLocalFileNames 发生异常："+e.getMessage(),e);
		}
    	return null;
    } 
	
    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *	@ReturnType	boolean 
     *	@Date	2018年7月27日	下午5:48:08
     *  @Param  @param sPath
     *  @Param  @return	删除成功返回 true，否则返回 false
     */
	public static boolean deleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 删除单个文件
	 *	@ReturnType	boolean 
	 *	@Date	2018年7月27日	下午5:47:18
	 *  @Param  @param sPath	被删除文件的路径
	 *  @Param  @return	删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			try{
				file.delete();
			}catch (Exception e) {
				e.printStackTrace();
			}
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录下的文件和目录
	 *	@ReturnType	boolean 
	 *	@Date	2018年7月27日	下午5:46:58
	 *  @Param  @param sPath
	 *  @Param  @return	目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		
		return flag;
	}

	/**
	 * 获取文件内容的总行数
	 *	@ReturnType	int 
	 *	@Date	2018年4月17日	下午1:39:38
	 *  @Param  @param file
	 *  @Param  @return
	 *  @Param  @throws IOException
	 */
	public static int getTotalLines(File file) throws IOException {  
        FileReader in = new FileReader(file);  
        LineNumberReader reader = new LineNumberReader(in);  
        int lines = 0;  
        String s = reader.readLine(); 
        while (s != null) {  
            lines++;  
            s = reader.readLine();  
        }  
        reader.close();  
        in.close();  
        return lines;  
    }
	
	/**
	 * 判断文件是否包含指定的字符串
	 *	@ReturnType	boolean 
	 *	@Date	2018年4月17日	下午1:49:34
	 *  @Param  @param file
	 *  @Param  @param str
	 *  @Param  @return
	 *  @Param  @throws IOException
	 */
	public static boolean checkIfFileContainStr(File file,String str) throws Exception {  
		boolean result = false;
        FileReader in = null;
		LineNumberReader reader = null;
		String s = null; 
		try {
			in = new FileReader(file);  
			reader = new LineNumberReader(in);  
			while (StringUtils.isNotBlank(s = reader.readLine()) && s.contains(str)) {  
		    	result = true;
		    	break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				reader.close();  
				in.close();
			} catch (Exception e) {
			}  
		}
        return result;  
    }
	
	/**
	 * 创建目录
	 *	@ReturnType	void 
	 *	@Date	2018年4月17日	下午1:49:38
	 *  @Param  @param dir
	 */
	public static void createDirIfNot(String dir){
		if(StringUtils.isEmpty(dir)){
			return;
		}
		File path = new File(dir);
		if(!path.exists()){
			path.mkdirs();
		}
	}
	
	/**
	 * 创建文件
	 *	@ReturnType	File 
	 *	@Date	2018年4月17日	下午1:49:46
	 *  @Param  @param filePath
	 *  @Param  @return
	 */
	public static File createFile(String filePath){
		File file = new File(filePath);
		if (file.exists()) {
			return file;
		}
		File parent = file.getParentFile();
		if(!parent.exists()){
			parent.mkdirs();
		}
		return file;
	}
	
}
