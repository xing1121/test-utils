package com.wdx.utils.poi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 描述：java操作excel工具类2007
 * @author 80002888
 * @date   2018年9月10日
 */
public class POIUtils2007 {

	// 获取 带泛型父类的 泛型类型
	// Type type = this.getClass().getGenericSuperclass();
	// ParameterizedType pt = (ParameterizedType) type;
	// Type[] types = pt.getActualTypeArguments();
	// Class clazzType = (Class) types[0];
	private static Logger logger = LoggerFactory.getLogger(POIUtils2007.class);
	
	/**
	 * Excel的第一行转为对象的属性，其他行每行转为一条数据
	 *	@ReturnType	List<T> 
	 *	@Date	2018年9月10日	下午6:54:39
	 *  @Param  @param fieldNames		xlsx中每一列顺序对应实体的属性名，可以为null
	 *  @Param  @param clazz			实体类
	 *  @Param  @param inputStream		输入流
	 *  @Param  @return
	 */
	public static <T> List<T> input2List(List<String> fieldNames, Class<T> clazz, InputStream inputStream) {
		if (inputStream == null || clazz == null) {
			return null;
		}
		try {
			// 返回值
			List<T> res = new ArrayList<>();
			// 读取输入流
			Workbook workbook = new XSSFWorkbook(inputStream);
			// 读取工作表
			Sheet sheet = workbook.getSheetAt(0);
			// 存所有setter方法及其参数类型
			List<Map<String, Object>> setterMethodList = new ArrayList<>();
			// 得到最后一行的索引
			int lastRowIndex = sheet.getLastRowNum();
			// 获取第一行是所有属性名
			Row firstRow = sheet.getRow(0);
			// 每一行单元格数量
			short cellCountInOneRow = firstRow.getLastCellNum();
			// 常量
			String methodKey = "method";
			String typeKey = "type";
			/*************************************************/
			// 第一行转为属性名
			if (fieldNames != null && fieldNames.size() == cellCountInOneRow) {
				for (int i = 0; i < fieldNames.size(); i++) {
					// 文本类型，属性名
					String fieldName = fieldNames.get(i);
					String rightStr = fieldName.substring(1);
					String leftStr = String.valueOf(fieldName.charAt(0)).toUpperCase();
					Field field = clazz.getDeclaredField(fieldName);
					fieldName = leftStr + rightStr;
					// setter方法的参数类型
					Class<?> type = field.getType();
					// setter方法
					Method method = clazz.getDeclaredMethod("set"+fieldName, type);
					// map中存放方法和参数类型
					Map<String, Object> map = new HashMap<>(2);
					map.put(methodKey, method);
					map.put(typeKey, type);
					setterMethodList.add(map);
				}
			} else {
				for (int i = 0; i < cellCountInOneRow; i++) {
					Cell cell = firstRow.getCell(i);
					// 文本类型，属性名
					String cellValue = cell.getStringCellValue();
					String rightStr = cellValue.substring(1);
					String leftStr = String.valueOf(cellValue.charAt(0)).toUpperCase();
					Field field = clazz.getDeclaredField(cellValue);
					cellValue = leftStr + rightStr;
					// setter方法的参数
					Class<?> type = field.getType();
					// setter方法
					Method method = clazz.getDeclaredMethod("set"+cellValue, type);
					// map中存放方法和参数类型
					Map<String, Object> map = new HashMap<>(2);
					map.put(methodKey, method);
					map.put(typeKey, type);
					setterMethodList.add(map);
				}
			}
			/*************************************************/
			// 其余行转为数据
			for (int i = 1; i <= lastRowIndex; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				// 每一行可以转换为一个对象
				T t = clazz.newInstance();
				for (int j = 0; j < cellCountInOneRow; j++) {
					// 每一个单元格，可以转换为对象的一个属性
					Map<String, Object> map = setterMethodList.get(j);
					// 获取对应属性的setter方法
					Method setterMethod = (Method) map.get(methodKey);
					// 获取对应属性的类型
					Class<?> fieldClazz = (Class<?>) map.get(typeKey);
					// 从单元格获取值
					Cell cell = row.getCell(j);
					Object value = getCellValue(fieldClazz, cell);
					if (value != null) {
						setterMethod.invoke(t, value);
					}
				}
				res.add(t);
			}
			/*************************************************/
			return res;
		} catch (Exception e) {
			logger.error("get error->", e);
		}
		return null;
	}
	
	/**
	 * 输入byte[]，转为对象集合
	 *	@ReturnType	List<T> 
	 *	@Date	2018年9月11日	下午3:33:22
	 *  @Param  @param fieldNames		xlsx中每一列顺序对应实体的属性名，可以为null
	 *  @Param  @param clazz			实体类
	 *  @Param  @param bytes			输入数组
	 *  @Param  @return
	 */
	public static <T> List<T> bytes2List(List<String> fieldNames, Class<T> clazz, byte[] bytes) {
		if (clazz == null || bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			return input2List(fieldNames, clazz, new ByteArrayInputStream(bytes));
		} catch (Exception e) {
			logger.error("get error->", e);
		}
		return null;
	}
	
	/**
	 * 传入普通对象（属性是基本类型和Date）的list集合和一个输出流，自动将list内容写到输出流中
	 *	@ReturnType	void 
	 *	@Date	2018年9月10日	下午6:53:12
	 *  @Param  @param headers				xlsx文件中第一行按顺序显示的列名，可以为null
	 *  @Param  @param sourceList			实体集合
	 *  @Param  @param outputStream			输出流
	 *  @Param  @param propertyNames		属性名称
	 *  @Param  @param keepStatus			属性保留还是去除
	 */
	public static <T> void list2Out(List<String> headers, List<T> sourceList, OutputStream outputStream, List<String> propertyNames, boolean keepStatus) {
		if (outputStream == null || sourceList == null || sourceList.size() == 0) {
			return;
		}
		try {
			// 真实的泛型类型
			Class<? extends Object> clazz = sourceList.get(0).getClass();
			
			// 创建Workbook对像（对应一个xlsx文件）
//			/// TODO SXSSFWorkbook占用内存低，且速度要快大约8倍
//			Workbook workbook = new HSSFWorkbook();
			Workbook workbook = new SXSSFWorkbook(100);
			
			// 全局样式，文字居中
			CellStyle basicCellStyle = workbook.createCellStyle();
			basicCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			// 在Workbook对象中创建Sheet（对应一个Sheet工作表），设置工作表中单元格的默认宽度
			Sheet sheet = workbook.createSheet(clazz.getName());
			sheet.setDefaultColumnWidth(20);
			// 一个属性对应一列，一个cell为行的一个格子
			Field[] fields = clazz.getDeclaredFields();
			List<Field> list = new ArrayList<>();
			
			// 保留还是去除
			if (keepStatus) {
				// 要保留的属性
				for (String propertyName : propertyNames) {
					Field field = null;
					try {
						field = clazz.getDeclaredField(propertyName);
					} catch (Exception e) {
						logger.error("get error->", e);
					}
					if (field != null) {
						list.add(field);
					}
				}
			} else {
				// 要去除的属性
				for (Field field : fields) {
					// 去除serialVersionUID属性
					if (field.getName().equals("serialVersionUID")) {
						continue;
					}
					if (propertyNames != null && propertyNames.contains(field.getName())) {
						continue;
					}
					list.add(field);
				}
			}
			
			if (CollectionUtils.isEmpty(list)) {
				return;
			}
			// list转数组
			fields = (Field[]) Array.newInstance(Field.class, list.size());
			list.toArray(fields);
			/***************************************************************/
			// 创建第一行，放列名字
			Row headRow = sheet.createRow(0);
			if (headers != null && headers.size() == fields.length) {
				for (int i = 0; i < headers.size(); i++) {
					Cell cell = headRow.createCell(i);
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(headers.get(i));
					CellStyle cellStyle = workbook.createCellStyle();
					DataFormat format = workbook.createDataFormat();
					cellStyle.setDataFormat(format.getFormat("@"));
					cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
					cell.setCellStyle(cellStyle);
				}
			} else {
				for (int i = 0; i < fields.length; i++) {
					// 文本类型
					Cell cell = headRow.createCell(i);
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(fields[i].getName().toString());
					CellStyle cellStyle = workbook.createCellStyle();
					DataFormat format = workbook.createDataFormat();
					cellStyle.setDataFormat(format.getFormat("@"));
					cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
					cell.setCellStyle(cellStyle);
				}
			}
			/***************************************************************/
			// 其余的行放数据
			for (int i = 0; i < sourceList.size(); i++) {
				Row bodyRow = sheet.createRow(i + 1);
				for (int j = 0; j < fields.length; j++) {
					// 获取属性
					Field field = fields[j];
					// 属性的真实类型
					Class<?> fieldClass = field.getType();
					
					// 获取属性的值
//					String fieldName = field.getName();
//					String rightStr = fieldName.substring(1);
//					String leftStr = String.valueOf(fieldName.charAt(0)).toUpperCase();
//					fieldName = leftStr + rightStr;
//					Method method = clazz.getDeclaredMethod("get" + fieldName);
//					Object value = method.invoke(sourceList.get(i));
					
					// 获取属性的值
					field.setAccessible(true);
                	Object value = field.get(sourceList.get(i));
					
					// 创建单元格
					Cell cell = bodyRow.createCell(j);
					// 创建单元格样式
					CellStyle cellStyle = workbook.createCellStyle();
					cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
					// 创建数据格式化对象
					DataFormat dataFormat = workbook.createDataFormat();
					// 设置值
					setCellValue(fieldClass, value, cell, cellStyle, dataFormat);
				}
			}
			/***************************************************************/
			// 将POI内容写入输出流
			workbook.write(outputStream);
		} catch (Exception e) {
			logger.error("get error->", e);
		}
	}

	/**
	 * 传入普通对象（属性是基本类型和Date）的list集合，转换为byte[]
	 *	@ReturnType	byte[] 
	 *	@Date	2018年9月10日	下午6:52:59
	 *  @Param  @param headers				xlsx文件中第一行按顺序显示的列名，可以为null
	 *  @Param  @param sourceList			实体集合
	 *  @Param  @param propertyNames		属性名称
	 *  @Param  @param keepStatus			属性保留还是去除
	 *  @Param  @return						数组
	 */
	public static <T> byte[] list2Bytes(List<String> headers, List<T> sourceList, List<String> propertyNames, boolean keepStatus) {
		if (sourceList == null || sourceList.size() == 0) {
			return null;
		}
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			list2Out(headers, sourceList, bos, propertyNames, keepStatus);
			return bos.toByteArray();
		} catch (Exception e) {
			logger.error("get error->", e);
		}
		return null;
	}
	
	/**
	 * 向单元格设置值
	 *	@ReturnType	void 
	 *	@Date	2018年9月11日	下午3:53:04
	 *  @Param  @param fieldClass			属性的类型
	 *  @Param  @param value				属性的值
	 *  @Param  @param cell					单元格
	 *  @Param  @param cellStyle			单元格样式
	 *  @Param  @param dataFormat			单元格内容格式
	 */
	private static <E> void setCellValue(Class<E> fieldClass, Object value, Cell cell, CellStyle cellStyle, DataFormat dataFormat){
		if (fieldClass == null || value == null || "".equals(value.toString().trim())) {
			return;
		}
		if (fieldClass.equals(String.class) || fieldClass.equals(Character.class)) {
			// 文本类型
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cellStyle.setDataFormat(dataFormat.getFormat("@"));
			cell.setCellStyle(cellStyle);
			cell.setCellValue(value.toString());
		} else if (fieldClass.equals(Integer.class) 
				|| fieldClass.equals(Long.class)
				|| fieldClass.equals(Byte.class) 
				|| fieldClass.equals(Short.class)) {
			// 普通数值类型
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			// 科学计数法
//			cellStyle.setDataFormat(format.getFormat("#,#0"));
			cellStyle.setDataFormat(dataFormat.getFormat("0"));
			cell.setCellStyle(cellStyle);
			cell.setCellValue(Double.parseDouble(value.toString()));
		} else if (fieldClass.equals(Double.class) || fieldClass.equals(Float.class)) {
			// 小数类型
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			// 科学计数法
//			cellStyle.setDataFormat(format.getFormat("#,##0.00"));
			cellStyle.setDataFormat(dataFormat.getFormat("0.00"));
			cell.setCellStyle(cellStyle);
			cell.setCellValue(Double.parseDouble(value.toString()));
		} else if (fieldClass.equals(Date.class)) {
			// 日期类型
			cellStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd hh:mm:ss;@"));
			cell.setCellStyle(cellStyle);
			cell.setCellValue((Date) (value));
		} else if (fieldClass.equals(Boolean.class)) {
			// 布尔类型
			cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
			cell.setCellValue((Boolean) (value));
		} else {
			// 文本类型
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cellStyle.setDataFormat(dataFormat.getFormat("@"));
			cell.setCellStyle(cellStyle);
			cell.setCellValue(value.toString());
		}
		
	}
	
	/**
	 * 获取单元格的值
	 *	@ReturnType	Object 
	 *	@Date	2018年9月11日	上午9:54:51
	 *  @Param  @param fieldClass		单元格的值要转成的类型
	 *  @Param  @param cell				单元格
	 *  @Param  @return
	 */
	private static <E> E getCellValue(Class<E> fieldClass, Cell cell){
		if (fieldClass == null || cell == null) {
			return null;
		}
		// 返回值
		Object res = null;
		// 单元格类型
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			// 空
			return null;
		case Cell.CELL_TYPE_BOOLEAN:
			// 布尔
			res = cell.getBooleanCellValue();
			if (res == null || "".equals(res.toString().trim())) {
				return null;
			}
			break;
		case Cell.CELL_TYPE_NUMERIC:
			// 数字
			res = cell.getNumericCellValue();
			if (res == null || "".equals(res.toString().trim())) {
				return null;
			}
			double dv = (double)res;
			// 数字转Byte
			if (fieldClass.equals(Byte.class)) {
				res = (byte)dv;
			}
			// 数字转Short
			if (fieldClass.equals(Short.class)) {
				res = (short)dv;
			}
			// 数字转Character
			if (fieldClass.equals(Character.class)) {
				res = (char)dv;
			}
			// 数字转Integer
			if (fieldClass.equals(Integer.class)) {
				res = (int)dv;
			}
			// 数字转Long
			if (fieldClass.equals(Long.class)) {
				res = (long)dv;
			}
			// 数字转Double
			if (fieldClass.equals(Double.class)) {
				res = dv;
			}
			// 数字转Float
			if (fieldClass.equals(Float.class)) {
				res = (float)dv;
			}
			// 数字转Date
			if (fieldClass.equals(Date.class)) {
				return fieldClass.cast(cell.getDateCellValue());
			}
			break;
		default:
			// 字符
			res = cell.getStringCellValue();
			if (res == null || "".equals(res.toString().trim())) {
				return null;
			}
			// 字符转Byte
			if (fieldClass.equals(Byte.class)) {
				res = Byte.parseByte(res.toString());
			}
			// 字符转Short
			if (fieldClass.equals(Short.class)) {
				res = Short.parseShort(res.toString());
			}
			// 字符转Character
			if (fieldClass.equals(Character.class)) {
				res = res.toString().charAt(0);
			}
			// 字符转Integer
			if (fieldClass.equals(Integer.class)) {
				res = Integer.parseInt(res.toString());
			}
			// 字符转Long
			if (fieldClass.equals(Long.class)) {
				res = Long.parseLong(res.toString());
			}
			// 字符转Double
			if (fieldClass.equals(Double.class)) {
				res = Double.parseDouble(res.toString());
			}
			// 字符转Float
			if (fieldClass.equals(Float.class)) {
				res = Float.parseFloat(res.toString());
			}
			// 字符转Date
			if (fieldClass.equals(Date.class)) {
				try {
					res = new SimpleDateFormat("yyyy-MM-dd").parse(res.toString());
				} catch (Exception e) {
					try {
						res = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(res.toString());
					} catch (Exception e1) {
					}
				}
			}
			break;
		}
		if (res == null || "".equals(res.toString().trim())) {
			return null;
		}
		return fieldClass.cast(res);
	}
	
}
