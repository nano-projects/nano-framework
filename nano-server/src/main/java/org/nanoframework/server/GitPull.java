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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.StringUtils;

/**
 *
 * @author yanghe
 * @since 1.4.6
 */
public class GitPull {
    private static final String CONF_FILE_PATH = "application.properties";
    private Properties conf;
    private App app;
    private File pullPath;
    private File fullPath;
    private File confPath;

    private GitPull() {
        if (init()) {
            initFullPath();
            initConfPath();
        }
    }

    public static GitPull create() {
        return new GitPull();
    }
    
    public GitPull quickPull() {
        try {
            return dir().pull().copy().clean();
        } catch (final IOException | GitAPIException e) {
            throw new org.nanoframework.server.exception.GitAPIException(e.getMessage(), e);
        }
    }

    private boolean init() {
        conf = PropertiesLoader.load(CONF_FILE_PATH);
        if (conf == null) {
            return false;
        }

        app = App.create(conf);
        return true;
    }

    private void initFullPath() {
        final String host;
        final String confHost = app.getConfHost();
        if (StringUtils.isNotBlank(confHost)) {
            host = confHost;
        } else {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (final UnknownHostException e) {
                throw new org.nanoframework.server.exception.UnknownHostException(e.getMessage());
            }
        }

        final StringBuilder fullPath = new StringBuilder();
        fullPath.append(app.getGitPullPath()).append(File.separatorChar).append(app.getConfPath()).append(File.separatorChar).append(app.getConfEnv())
                .append(File.separatorChar).append(host);
        this.fullPath = new File(fullPath.toString());
    }

    private void initConfPath() {
        final URL url = getClass().getResource("/");
        final String classpath = url.getFile();
        final String confPath = classpath + "../conf";
        this.confPath = new File(confPath);
    }

    public GitPull dir() throws IOException {
        final String gitPullPath = app.getGitPullPath();
        final Matcher matcher = Pattern.compile(".*[a-zA-Z]+.*").matcher(gitPullPath);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("无效的Git pull资源路径");
        }

        pullPath = new File(gitPullPath);
        clean();
        return this;
    }

    public GitPull pull() throws InvalidRemoteException, TransportException, GitAPIException {
        Git.cloneRepository().setURI(app.getGitRepo()).setDirectory(pullPath).call();
        return this;
    }

    public GitPull copy() throws IOException {
        if (fullPath.exists()) {
            if (confPath.exists() && StringUtils.equals(App.REPEAT_POLICY_CLEAN, app.getConfRepeatPolicy())) {
                cleanConf();
            }
            
            FileUtils.copyDirectory(fullPath, confPath);
        }
        
        return this;
    }
    
    private void cleanConf() throws IOException {
        final File[] files = confPath.listFiles((dir, name) -> !StringUtils.equals(name, CONF_FILE_PATH));
        if (ArrayUtils.isNotEmpty(files)) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    FileUtils.deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    public GitPull clean() throws IOException {
        if (pullPath != null && pullPath.exists()) {
            FileUtils.deleteDirectory(pullPath);
        }
        return this;
    }
}
