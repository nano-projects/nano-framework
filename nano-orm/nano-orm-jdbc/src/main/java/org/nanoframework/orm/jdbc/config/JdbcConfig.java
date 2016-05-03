/**
 * Copyright 2015 the original author or authors.
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
package org.nanoframework.orm.jdbc.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.annatations.Property;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.orm.jdbc.DataSourceException;

/**
 * @author yanghe
 * @date 2015年9月30日 下午3:10:13
 */
public abstract class JdbcConfig extends BaseEntity {
	private static final long serialVersionUID = -5652080352809590470L;

	/** 数据源名称 */
	@Property(name = "JDBC.environment.id", required = true)
	private String environmentId;
	
	/** 数据源驱动类名 */
	@Property(name = "JDBC.driver", required = true)
	private String driver;
	
	/** 数据库连接字符串 */
	@Property(name = "JDBC.url", required = true)
	private String url;
	
	/** 用户名 */
	@Property(name = "JDBC.username", required = true)
	private String userName;
	
	/** 密码 */
	@Property(name = "JDBC.password", required = true)
	private String passwd;
	
	/** 是否自动提交 */
	@Property(name = "JDBC.autoCommit")
	private Boolean autoCommit;
	
	/** Statement执行超时时间, Default: 30 */
	@Property(name = "JDBC.defaultStatementTimeout")
	private Integer defaultStatementTimeout = 30;
	
	protected void setProperties(Properties properties) {
		List<Field> fields = _getAllFields(new ArrayList<>(), getClass());
		if(!CollectionUtils.isEmpty(fields)) {
			for(Field field : fields) {
				Property property;
				if((property = field.getAnnotation(Property.class)) != null) {
					String value = properties.getProperty(property.name());
					if(StringUtils.isNotBlank(value)) {
						_setAttributeValue(field.getName(), value);
					} else if(property.required()) {
						throw new DataSourceException("属性 [" + property.name() + "] 设置为必选项，这里却获取了无效的属性值");
					}
				}
			}
		}
	}
	
	public String getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public Boolean getAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public Integer getDefaultStatementTimeout() {
		return defaultStatementTimeout;
	}

	public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
		this.defaultStatementTimeout = defaultStatementTimeout;
	}

}
