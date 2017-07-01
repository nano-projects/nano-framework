/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.extension.shiro;

import java.lang.reflect.Constructor;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoaderException;

/**
 * 适用于非web工程的项目，如果是web项目请使用ShiroFilter进行相关配置.
 * @author yanghe
 */
public class ShiroPlugin implements Plugin {
    private Logger LOG = LoggerFactory.getLogger(ShiroPlugin.class);

    public static final String SHIRO_INI = "shiro-ini";
    private String shiroIni;

    @Override
    public boolean load() throws Throwable {
        if (StringUtils.isNotBlank(shiroIni)) {
            Class<?> IniSecurityManagerFactory = null;
            Class<?> SecurityUtils = null;
            Class<?> SecurityManager = null;

            Class<?> EnumConverter = null;
            try {
                IniSecurityManagerFactory = Class.forName("org.apache.shiro.config.IniSecurityManagerFactory");
                SecurityManager = Class.forName("org.apache.shiro.mgt.SecurityManager");
                SecurityUtils = Class.forName("org.apache.shiro.SecurityUtils");

                EnumConverter = Class.forName("org.nanoframework.extension.shiro.util.EnumConverter");
            } catch (Exception e) {
                LOG.warn("未加载shiro api");
            }

            if (IniSecurityManagerFactory != null) {
                try {
                    EnumConverter.getMethod("register").invoke(EnumConverter);
                    Constructor<?> constructor = IniSecurityManagerFactory.getConstructor(String.class);
                    Object factory = constructor.newInstance(shiroIni);
                    Object manager = IniSecurityManagerFactory.getMethod("getInstance").invoke(factory);
                    SecurityUtils.getMethod("setSecurityManager", SecurityManager).invoke(SecurityUtils, manager);
                } catch (Exception e) {
                    throw new PluginLoaderException("加载ShiroPlugin异常: " + e.getMessage());
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {
        shiroIni = config.getInitParameter(SHIRO_INI);
    }

}
