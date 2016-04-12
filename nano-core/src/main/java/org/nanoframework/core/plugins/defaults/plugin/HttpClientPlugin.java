/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.core.plugins.defaults.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.httpclient.HttpClientFactory;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoaderException;

/**
 * @author yanghe
 * @date 2016年2月7日 下午1:53:07
 * @deprecated deprecated it
 */
@Deprecated
public class HttpClientPlugin implements Plugin {
	public static final String DEFAULT_HTTPCLIENT_PARAMETER_NAME = "httpclient";
	public static final String TIME_TO_LIVE = "context.httpclient.time.to.live";
	public static final String TIME_UNIT = "context.httpclient.timeunit";
	public static final String MAX_TOTAL = "context.httpclient.max.total";
	public static final String MAX_PER_ROUTE = "context.httpclient.default.max.per.route";
	
	private Properties properties;
	private long timeToLive;
	private TimeUnit tunit;
	private int maxTotal;
	private int maxPerRoute;
	
	@Override
	public boolean load() throws Throwable {
		timeToLive = Long.parseLong(properties.getProperty(TIME_TO_LIVE, "-1"));
		tunit = TimeUnit.valueOf(properties.getProperty(TIME_UNIT, "MILLISECONDS"));
		maxTotal = Integer.parseInt(properties.getProperty(MAX_TOTAL, "20"));
		maxPerRoute = Integer.parseInt(properties.getProperty(MAX_PER_ROUTE, "2"));
		
		try {
			Class<?> PoolingHttpClientConnectionManager = Class.forName("org.apache.http.impl.conn.PoolingHttpClientConnectionManager");
			Constructor<?> constructor = PoolingHttpClientConnectionManager.getConstructor(long.class, TimeUnit.class);
			Object manager = constructor.newInstance(timeToLive, tunit);
			Method setMaxTotal = PoolingHttpClientConnectionManager.getMethod("setMaxTotal", int.class);
			setMaxTotal.invoke(manager, maxTotal);
			Method setDefaultMaxPerRoute = PoolingHttpClientConnectionManager.getMethod("setDefaultMaxPerRoute", int.class);
			setDefaultMaxPerRoute.invoke(manager, maxPerRoute);
			HttpClientFactory.create(manager);
		} catch(Throwable e) {
			if(!(e instanceof ClassNotFoundException))
				throw new PluginLoaderException(e.getMessage(), e);
			
			return false;
		}
		
		return true;
	}

	@Override
	public void config(ServletConfig config) throws Throwable {
		String httpclient = config.getInitParameter(DEFAULT_HTTPCLIENT_PARAMETER_NAME);
		if(StringUtils.isNotBlank(httpclient)) {
			try {
				properties = PropertiesLoader.load(httpclient);
			} catch(LoaderException e) {
				properties = new Properties();
			}
		} else 
			properties = new Properties();
		
	}

}
