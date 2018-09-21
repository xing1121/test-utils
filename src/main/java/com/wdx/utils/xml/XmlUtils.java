package com.wdx.utils.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.wdx.utils.FileUtils;

/**
 * 描述：xml工具类
 * @author 80002888
 * @date   2018年7月27日
 */
@SuppressWarnings("unchecked")
public class XmlUtils {

	private static Logger logger = LoggerFactory.getLogger(XmlUtils.class);
	
	public static void main(String[] args) {
		// 读取xml文件为字符串内容
		String xml = FileUtils.readContent("src/main/java/com/wdx/utils/xml/bks.xml");
		System.out.println(xml);
		System.out.println("-----------------------");
		
		// 字符串转为对象
		Bookstore bookStore = convertToJavaBean(xml, Bookstore.class);
		System.out.println(bookStore);
		System.out.println("-----------------------");
		
		// 对象转字符串
		String s = convertFromJavaBean(bookStore);
		System.out.println(s);
		System.out.println("-----------------------");
		
		// 字符串写入xml文件
		FileUtils.writeContent(s, "src/main/java/com/wdx/utils/xml/bks2.xml");
	}
	
	/**
	 * java实体对象转为xml字符串
	 *	@ReturnType	String 
	 *	@Date	2018年7月27日	下午2:42:40
	 *  @Param  @param t
	 *  @Param  @return
	 */
	public static <T> String convertFromJavaBean(T t){
		logger.info("XmlUtils...convertToJavaBean 开始转换" + t + "对象为字符串！");
		String xmlStr = null;
		try {
			Class<?> clazz = t.getClass();
			JAXBContext context = JAXBContext.newInstance(clazz);
			Marshaller marshaller = context.createMarshaller();
			SAXParserFactory sax = SAXParserFactory.newInstance();
			sax.setNamespaceAware(false);
			StringWriter w = new StringWriter();
			marshaller.marshal(t, w);
			xmlStr = w.toString();
		} catch (Exception e) {
			logger.error("XmlUtils...convertFromJavaBean 转换" + t + "对象为字符串发生异常："+e.getMessage(),e);
		}
		logger.info("XmlUtils...convertFromJavaBean 完成转换对象为xml字符串！");
		return xmlStr;
	}
	
	/**
	 * xml字符串转为java实体对象
	 *	@ReturnType	T 
	 *	@Date	2018年7月27日	下午2:36:57
	 *  @Param  @param xmlStr
	 *  @Param  @param c
	 *  @Param  @return
	 */
	protected static <T> T convertToJavaBean(String xmlStr, Class<T> clazz) {
		logger.info("XmlUtils...convertToJavaBean 开始解析xml的字符串内容为" + clazz + "对象！");
		T t = null;
		try {
			JAXBContext context = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			SAXParserFactory sax = SAXParserFactory.newInstance();
			sax.setNamespaceAware(false);
			XMLReader xmlReader = sax.newSAXParser().getXMLReader();
			SAXSource source = new SAXSource(xmlReader, new InputSource(new StringReader(xmlStr)));
			t = (T) unmarshaller.unmarshal(source);
		} catch (Exception e) {
			logger.error("XmlUtils...convertToJavaBean 解析xml的字符串内容为" + clazz + "对象发生异常："+e.getMessage(),e);
		}
		logger.info("XmlUtils...convertToJavaBean 完成解析xml的字符串内容为对象！");
		return t;
	}
	
}
