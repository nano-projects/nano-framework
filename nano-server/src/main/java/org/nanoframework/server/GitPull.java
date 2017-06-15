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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.ObjectCompare;
import org.nanoframework.commons.util.StringUtils;

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.4.6
 */
public class GitPull {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitPull.class);
    private static final String CONF_FILE_PATH = "application.properties";
    private static final String[] HOST_FILTERS = { "localhost", "127.0.0.1" };
    private Properties conf;
    private App app;
    private File pullPath;
    private List<File> fullPath = Lists.newArrayList();
    private File confPath;
    private boolean enabled;

    private GitPull() {
        if (init()) {
            initFullPath();
            initConfPath();
        }
    }

    public static GitPull create() {
        return new GitPull();
    }

    public GitPull quickPull(String... args) {
        if (!ArrayUtils.isEmpty(args)) {
            for (final String arg : args) {
                if (StringUtils.equals("stop", StringUtils.lowerCase(arg))) {
                    return this;
                }
            }
        }

        if (enabled) {
            try {
                return dir().pull().copy().clean();
            } catch (final IOException | GitAPIException e) {
                throw new org.nanoframework.server.exception.GitAPIException(e.getMessage(), e);
            }
        }

        return this;
    }

    private boolean init() {
        try {
            conf = PropertiesLoader.load(CONF_FILE_PATH);
        } catch (final Throwable e) {
            // ignore
        }

        if (conf == null) {
            return false;
        }

        app = App.create(conf);
        enabled = app.getEnabled();
        if (!enabled) {
            LOGGER.warn("未启用配置中心功能，配置将从本地进行加载");
        }

        return true;
    }

    private void initFullPath() {
        final String[] hosts;
        final String confHost = app.getConfHost();
        if (StringUtils.isNotBlank(confHost)) {
            hosts = new String[] { confHost };
        } else {
            hosts = getHostAddresses();
        }

        final StringBuilder fullPath = new StringBuilder();
        fullPath.append(app.getGitPullPath()).append(File.separatorChar).append(app.getConfPath()).append(File.separatorChar).append(app.getConfEnv())
                .append(File.separatorChar);
        for (final String host : hosts) {
            this.fullPath.add(new File(fullPath.toString() + host));
        }
    }

    private String[] getHostAddresses() {
        try {
            final List<String> hostAddrs = Lists.newArrayList();
            final Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                final NetworkInterface ni = nis.nextElement();
                final Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    final InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address) {
                        final String hostAddr = addr.getHostAddress();
                        if (!ObjectCompare.isInList(hostAddr, HOST_FILTERS)) {
                            hostAddrs.add(hostAddr);
                        }
                    }
                }
            }

            return hostAddrs.toArray(new String[hostAddrs.size()]);
        } catch (final SocketException e) {
            throw new org.nanoframework.server.exception.UnknownHostException(e.getMessage());
        }
    }

    private void initConfPath() {
        final URL url = getClass().getResource("/");
        final String classpath = url.getFile();
        final String confPath = classpath + "../conf";
        this.confPath = new File(confPath);
    }

    public GitPull dir() throws IOException {
        if (enabled) {
            pullPath = new File(app.getGitPullPath());
            clean();
        }

        return this;
    }

    public GitPull pull() throws GitAPIException {
        if (enabled) {
            Git.cloneRepository().setURI(app.getGitRepo()).setDirectory(pullPath).setBranch(app.getGitRepoBranch()).call();
        }

        return this;
    }

    public GitPull copy() throws IOException {
        if (enabled) {
            for (final File path : fullPath) {
                if (path.exists()) {
                    if (confPath.exists() && StringUtils.equals(App.REPEAT_POLICY_CLEAN, app.getConfRepeatPolicy())) {
                        cleanConf();
                    }

                    FileUtils.copyDirectory(path, confPath);
                    break;
                }
            }
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
        if (enabled) {
            if (pullPath != null && pullPath.exists()) {
                FileUtils.deleteDirectory(pullPath);
            }
        }

        return this;
    }
}
