package com.wdx.util.collection;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wdx.utils.collection.CollectionUtils;

/**
 * 
 * 描述：CollectionUtilsTest



 * @author wdx
 * @date   2020年5月25日
 */
public class CollectionUtilsTest {

	private static final Logger logger = LoggerFactory.getLogger(CollectionUtilsTest.class);
    
	@Test
	public void splitListExecuteAsyncTest() {
		List<String> nos = createDatas();
		CollectionUtils.splitListExecuteAsync(nos, 200, (t)->{
			for (String no : t) {
				logger.info(no);
			}
		});
	}
	
    @Test
    public void splitListExecuteTest() {
    	List<String> nos = createDatas();
    	CollectionUtils.splitListExecute(nos, 200, (t)->{
			for (String no : t) {
				logger.info(no);
			}
    	});
	}

    /**
     * 创建数据
     *	@ReturnType	List<String> 
     *	@Date	2020年5月25日	下午5:01:49
     *  @Param  @return
     */
	private static List<String> createDatas() {
		List<String> nos = new ArrayList<>();
		for (int i = 0; i < 3003; i++) {
			nos.add("8000" + (Long.valueOf(88880000) + i));
		}
		return nos;
	}
    
}
