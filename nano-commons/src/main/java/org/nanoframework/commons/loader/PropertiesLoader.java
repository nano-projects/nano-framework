/**
 * Copyright 2015- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.commons.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.io.ClassPathResource;
import org.nanoframework.commons.io.Resource;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.ResourceUtils;

/**
 * 
 * @ClassName: PropertiesLoader 
 * @Description: 属性文件操作公有类，负责对属性文件进行读写操作
 * @author yanghe
 * @date 2015年6月5日 上午8:36:50 
 *
 */
public class PropertiesLoader {

	private static Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);
	
	public static Map<String, Properties> PROPERTIES = new HashMap<>();
	public static final String CONTEXT = "context";
	
	public static final Properties load(String path) {
		try {
			Resource resource = new ClassPathResource(path);
			InputStream input = resource.getInputStream();
			
			Properties properties;
			if(input != null) 
				properties = PropertiesLoader.load(input);
			else 
				properties = PropertiesLoader.load(ResourceUtils.getFile(path));
			
			return properties;
		} catch(IOException e) {
			throw new LoaderException("加载属性文件异常: " + e.getMessage());
		}
	}
	
	/**
	 * 通过输入流加载属性文件
	 * 
	 * @param input 文件输入流，例如：xxx.class.getResourceAsStream("")
	 * @return 返回加载后的Properties
	 * @throws LoaderException Loader异常
	 * @throws IOException IO异常
	 */
	public static final Properties load(InputStream input) {

		if(input == null)
			throw new LoaderException("输入流为空");
		
		try {
			Properties prop = new Properties();
			prop.load(input);
			return prop;
		} catch(IOException e) {
			throw new LoaderException("加载属性文件异常: " + e.getMessage());
		}
	}
	
	/**
	 * 通过文件加载属性文件
	 * @param file 输入文件
	 * @return 返回加载后的Properties
	 * @throws LoaderException Loader异常
	 * @throws IOException IO异常
	 */
	public static final Properties load(File file) throws LoaderException, IOException {

		if(file == null)
			throw new LoaderException("文件对象为空");
		
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		
		return prop;
		
	}
	
	/**
	 * 加载属性文件
	 * 
	 * @param contextPath 文件相对路径
	 * @Param stream context属性流
	 * @param loadContext 是否加载context
	 * @throws LoaderException 加载异常
	 * @throws IOException IO异常
	 */
	public static final void load(String contextPath, InputStream stream, boolean loadContext) throws LoaderException, IOException {
		Properties prop = load(stream);
		prop.forEach((key, value) -> System.setProperty((String) key, (String) value));
		PROPERTIES.put(contextPath, prop);
		
		if(loadContext) {
			String _ctx = prop.getProperty(CONTEXT);
			if(StringUtils.isNotEmpty(_ctx)) {
				String[] ctxs = _ctx.split(";");
				if(ctxs.length > 0) {
					for (String ctx : ctxs) {
						InputStream input = PropertiesLoader.class.getResourceAsStream(ctx);
						if(input != null) {
							Properties tmp = load(input);
							if (tmp == null) {
								LOG.error(ctx + ": 无法加载此属性文件!");
							} else {
								PROPERTIES.put(ctx, tmp);
							}
						} else {
							LOG.error(ctx + ": 无法加载此属性文件!");
							
						}
					}
					
				}
			}
		}
	}
	
}
