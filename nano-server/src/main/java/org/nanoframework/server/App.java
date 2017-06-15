/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.server;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.ObjectCompare;
import org.nanoframework.commons.util.UUIDUtils;

/**
 *
 * @author yanghe
 * @since 1.4.6
 */
public class App extends BaseEntity {
    public static final String REPEAT_POLICY_CLEAN = "CLEAN";
    public static final String REPEAT_POLICY_REPLACE = "REPLACE";

    private static final long serialVersionUID = -6160267822834183082L;
    private static final String ENABLED = "application.enabled";
    private static final String GIT_REPO = "application.git.repo";
    private static final String GIT_REPO_BRANCH = "application.git.repo.branch";
    private static final String CONF_PATH = "application.conf.path";
    private static final String CONF_ENV = "application.conf.env";
    private static final String CONF_HOST = "application.conf.host";
    private static final String CONF_REPEAT_POLICY = "application.conf.repeat.policy";
    private static final String[] REPEAT_POLICY = { REPEAT_POLICY_CLEAN, REPEAT_POLICY_REPLACE };

    private Boolean enabled;
    private String gitRepo;
    private String gitRepoBranch;
    private String gitPullPath;
    private String confPath;
    private String confEnv;
    private String confHost;
    private String confRepeatPolicy;

    private App(final Properties conf) {
        enabled = Boolean.valueOf(conf.getProperty(ENABLED, "false"));
        gitRepo = conf.getProperty(GIT_REPO);
        gitRepoBranch = conf.getProperty(GIT_REPO_BRANCH);
        gitPullPath = UUIDUtils.create();
        confPath = conf.getProperty(CONF_PATH);
        confEnv = conf.getProperty(CONF_ENV);
        confHost = conf.getProperty(CONF_HOST);
        confRepeatPolicy = conf.getProperty(CONF_REPEAT_POLICY, REPEAT_POLICY_CLEAN);

        if (!valid()) {
            throw new IllegalArgumentException("无效的配置中心参数设置");
        }
    }

    public static App create(final Properties conf) {
        return new App(conf);
    }

    private boolean valid() {
        if (!StringUtils.isNoneBlank(gitRepo, gitPullPath, confPath, confEnv)) {
            return false;
        }

        if (!ObjectCompare.isInList(confRepeatPolicy, REPEAT_POLICY)) {
            return false;
        }

        return true;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getGitRepo() {
        return gitRepo;
    }

    public String getGitRepoBranch() {
        return gitRepoBranch;
    }

    public String getGitPullPath() {
        return gitPullPath;
    }

    public String getConfPath() {
        return confPath;
    }

    public String getConfEnv() {
        return confEnv;
    }

    public String getConfHost() {
        return confHost;
    }

    public String getConfRepeatPolicy() {
        return confRepeatPolicy;
    }

}
