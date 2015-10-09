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
package org.nanoframework.core.component.scan;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.component.exception.ComponentServiceRepeatException;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;

/**
 * 扫描组件，并返回符合要求的集合
 * @author yanghe
 * @date 2015年6月5日 上午9:01:37 
 *
 */
public class ComponentScan {

	private static final Logger LOG = LoggerFactory.getLogger(ComponentScan.class);
	
	private static Set<Class<?>> classes;
	
	private static final FileFilter fileFilter = (file) -> file.isDirectory() || file.getName().endsWith(".class");
	
	/**
	 * 返回所有带有参数中的注解的类
	 * @param annotationClass 注解类
	 * @return 过滤后的类
	 */
	public static Set<Class<?>> filter(Class<? extends Annotation> annotationClass) {
		if(classes == null) 
			classes = new LinkedHashSet<>();
		
		if(classes.size() > 0) {
			Set<Class<?>> annClasses = new LinkedHashSet<>();
			classes.stream().filter(clz -> clz.isAnnotationPresent(annotationClass)).forEach(clz -> annClasses.add(clz));
			return annClasses;
			
		}
		
		return Collections.emptySet();
		
	}
	
	/**
	 * 返回带有RequestMapping的所有传入对象的方法
	 * 
	 * @param obj 已实例化的对象
	 * @param methods 该对象中的方法
	 * @param annotationClass 方法级别的注解，并且必须继承自RequestMapping
	 * @return 返回方法组
	 */
	public static Map<String, Map<RequestMethod, RequestMapper>> filter(Object obj, Method[] methods, Class<? extends RequestMapping> annotationClass, String componentURI) {
		if(methods == null)
			return Collections.emptyMap();
		
		Map<String, Map<RequestMethod, RequestMapper>> methodMaps = new HashMap<>();
		for(Iterator<Map<String, Map<RequestMethod, RequestMapper>>> iter = Arrays.asList(methods).stream().filter(method -> method.isAnnotationPresent(annotationClass)).filter(method -> {
			RequestMapping mapping = method.getAnnotation(annotationClass);
			if(mapping != null && !"".equals(mapping.value()))
				return true;
			else {
				if(LOG.isDebugEnabled())
					LOG.debug("无效的URI Mapper定义: " + obj.getClass().getName() + "." + method.getName() + ":" + (componentURI + mapping.value()));
				
				return false;
			}
		}).map(method -> {
			Map<String, Map<RequestMethod, RequestMapper>> methodMap = new HashMap<>();
			RequestMapping mapping = method.getAnnotation(annotationClass);
			RequestMapper mapper = RequestMapper.create().setObject(obj).setClz(obj.getClass()).setMethod(method).setRequestMethods(mapping.method());
			Map<RequestMethod, RequestMapper> mappers = new HashMap<>();
			for(RequestMethod _method : mapper.getRequestMethods()) mappers.put(_method, mapper);
			methodMap.put((componentURI + mapping.value()).toLowerCase(), mappers);
			if(LOG.isDebugEnabled())
				LOG.debug("URI Mapper定义: " + obj.getClass().getName() + "." + method.getName() + ":" + (componentURI + mapping.value()));
			
			return methodMap;
		}).iterator(); iter.hasNext();) {
			Map<String, Map<RequestMethod, RequestMapper>> methodMap = iter.next();
			String uri = null;
			if(!CollectionUtils.isEmpty(methodMap) && methodMaps.containsKey(uri = methodMap.keySet().iterator().next())) {
				Set<RequestMethod> before = methodMap.get(uri).keySet();
				Set<RequestMethod> after = methodMaps.get(uri).keySet();
				if(!isIntersectionRequestMethod(before, after)) {
					methodMap.forEach((_uri, _methodMapper) -> {
						Map<RequestMethod, RequestMapper> methodMapper;
						if((methodMapper = methodMaps.get(_uri)) == null) {
							methodMaps.put(_uri, _methodMapper);
						} else {
							methodMapper.putAll(_methodMapper);
							methodMaps.put(_uri, methodMapper);
						}
					});
				} else
					throw new ComponentServiceRepeatException("组件服务重复: " + uri);
				
			} else {
				methodMap.forEach((_uri, _methodMapper) -> {
					Map<RequestMethod, RequestMapper> methodMapper;
					if((methodMapper = methodMaps.get(_uri)) == null) {
						methodMaps.put(_uri, _methodMapper);
					} else {
						methodMapper.putAll(_methodMapper);
						methodMaps.put(_uri, methodMapper);
					}
				});
			}
		}
		
		return methodMaps;
	}
	
	public static boolean isIntersectionRequestMethod(Set<RequestMethod> before, Set<RequestMethod> after) {
		Assert.notEmpty(before);
		Assert.notEmpty(after);
		for(RequestMethod beforeMethod : before) {
			for(RequestMethod afterMethod : after) {
				if(beforeMethod == afterMethod)
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 获取指定包路径下的所有类
	 * @param packageName
	 * @return
	 */
	public static void scan(String packageName) {
		if(StringUtils.isEmpty(packageName)) {
			LOG.warn("没有设置packageName, 跳过扫描");
			return ;
		}
		
		if(classes == null)
			classes = new HashSet<>();
		
		classes.addAll(getClasses(packageName));
		
		/** 现在采用Mybatis的包扫描实现

		String packageDirName = packageName.replace('.', '/');
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findAndAddClassesInPackageByFile(packageName, filePath);
					
				} else if ("jar".equals(protocol)) {
					findAndClassesInPackageByJar(url, packageDirName, packageName);
					
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new ComponentScanException(e.getMessage(), e);
			
		}
		*/
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName 包名
	 * @param packagePath 包路径
	 * @param recursive ?
	 * @param classes 类集合
	 * @throws ClassNotFoundException 
	 */
	@Deprecated
	static void findAndAddClassesInPackageByFile(String packageName, String packagePath) throws ClassNotFoundException {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		
		File[] dirfiles = dir.listFiles(fileFilter);
		
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath());
				
			} else {
				String className = file.getName().substring(0, file.getName().length() - 6);
				classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				
			}
		}
	}
	
	/**
	 * 以jar包的形式来获取包下的所有Class
	 * 
	 * @param url jar包所在URL
	 * @param packageDirName 包目录名
	 * @param packageName 包名
	 * @throws IOException IO异常
	 * @throws ClassNotFoundException 未找到类异常
	 */
	@Deprecated
	static void findAndClassesInPackageByJar(URL url, String packageDirName, String packageName) throws IOException, ClassNotFoundException {
		JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
		Enumeration<JarEntry> entries = jar.entries();

		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.charAt(0) == '/') {
				name = name.substring(1);
			}
			
			if (name.startsWith(packageDirName)) {
				int idx = name.lastIndexOf('/');
				if (idx != -1) {
					packageName = name.substring(0, idx).replace('/', '.');
				}
				
				if (name.endsWith(".class") && !entry.isDirectory()) {
					String className = name.substring(packageName.length() + 1, name.length() - 6);
					classes.add(Class.forName(packageName + '.' + className));
				}
			}
		}
	}
	
	/**
     * Return a set of all classes contained in the given package.
     *
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(String packageName) {
        return getClasses(new ResolverUtil.IsA(Object.class), packageName);
    }

    /**
     * Return a set of all classes contained in the given package that match with
     * the given test requirement.
     *
     * @param test the class filter on the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(ResolverUtil.Test test, String packageName) {
        Assert.notNull(test, "Parameter 'test' must not be null");
        Assert.notNull(packageName, "Parameter 'packageName' must not be null");
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }
}
