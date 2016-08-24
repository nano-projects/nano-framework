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
package org.nanoframework.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.context.ApplicationContext.JettyRedisSession;
import org.nanoframework.server.exception.JettyServerException;
import org.nanoframework.server.exception.ReadXMLException;
import org.nanoframework.server.session.redis.RedisSessionIdManager;
import org.nanoframework.server.session.redis.RedisSessionManager;

/**
 * Jetty Server
 * 
 * @author yanghe
 * @since 1.0
 */
public class JettyCustomServer extends Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyCustomServer.class);

    private static String DEFAULT_RESOURCE_BASE = "./webRoot";

    private static String DEFAULT_WEB_XML_PATH = DEFAULT_RESOURCE_BASE + "/WEB-INF/web.xml";

    private static String WEB_DEFAULT = DEFAULT_RESOURCE_BASE + "/WEB-INF/webdefault.xml";

    private static String DEFAULT_JETTY_CONFIG = DEFAULT_RESOURCE_BASE + "/WEB-INF/jetty.xml";
    
    private static final String JETTY_PID_FILE = "jetty.pid";
    
    private Properties context;
    
    private JettyCustomServer() {
        super();
        loadContext(ApplicationContext.MAIN_CONTEXT);
        init(DEFAULT_JETTY_CONFIG, context.getProperty(ApplicationContext.CONTEXT_ROOT), null, null, null);
    }

    private JettyCustomServer(final String contextPath) {
        super();
        loadContext(contextPath);
        init(DEFAULT_JETTY_CONFIG, context.getProperty(ApplicationContext.CONTEXT_ROOT), null, null, null);
    }
    
    public static JettyCustomServer server() {
        return new JettyCustomServer();
    }
    
    public static JettyCustomServer server(final String contextPath) {
        return new JettyCustomServer(contextPath);
    }

    protected void loadContext(final String contextPath) {
        Assert.hasLength(contextPath, "无效的context属性文件路径");
        context = PropertiesLoader.load(contextPath);
    }
    
    protected void init(final String xmlConfigPath, final String contextPath, final String resourceBase, final String webXmlPath,
            final String warPath) {
        if (StringUtils.isNotBlank(xmlConfigPath)) {
            DEFAULT_JETTY_CONFIG = xmlConfigPath;
            readXmlConfig(xmlConfigPath);
        }

        if (StringUtils.isNotEmpty(warPath) && StringUtils.isNotEmpty(contextPath)) {
            applyHandle(contextPath, warPath);
        } else {
            if (StringUtils.isNotEmpty(resourceBase)) {
                DEFAULT_RESOURCE_BASE = resourceBase;
            }

            if (StringUtils.isNotEmpty(webXmlPath)) {
                DEFAULT_WEB_XML_PATH = webXmlPath;
            }

            if (StringUtils.isNotBlank(contextPath)) {
                applyHandle(contextPath, warPath);
            }
        }
    }

    private void readXmlConfig(final String configPath) {
        try {
            final XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(configPath));
            configuration.configure(this);
        } catch (final Throwable e) {
            throw new ReadXMLException(e.getMessage(), e);
        }
    }

    private void applyHandle(final String contextPath, final String warPath) {
        final ContextHandlerCollection handler = new ContextHandlerCollection();
        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        webapp.setDefaultsDescriptor(WEB_DEFAULT);
        if (StringUtils.isEmpty(warPath)) {
            webapp.setResourceBase(DEFAULT_RESOURCE_BASE);
            webapp.setDescriptor(DEFAULT_WEB_XML_PATH);
        } else {
            webapp.setWar(warPath);
        }
        
        applySessionHandler(webapp);
        
        handler.addHandler(webapp);
        super.setHandler(handler);
    }
    
    protected void applySessionHandler(final WebAppContext webapp) {
        final String jettyCluster = context.getProperty(JettyRedisSession.JETTY_CLUSTER);
        if (StringUtils.isNotBlank(jettyCluster)) {
            setSessionIdManager(createRedisSessionIdManager(jettyCluster));
            webapp.setSessionHandler(new SessionHandler(createRedisSessionManager(jettyCluster)));
        }
    }
    
    protected RedisSessionIdManager createRedisSessionIdManager(final String jettyCluster) {
        final RedisSessionIdManager sessionIdManager = new RedisSessionIdManager(this, jettyCluster);
        
        final String workerName = context.getProperty(JettyRedisSession.JETTY_CLUSTER_WORKER_NAME, JettyRedisSession.DEFAULT_JETTY_CLUSTER_WORKER_NAME);
        sessionIdManager.setWorkerName(workerName);
        
        final long scavengerInterval = Long.parseLong(context.getProperty(JettyRedisSession.JETTY_SESSION_SCAVENGER_INTERVAL, JettyRedisSession.DEFAULT_SCAVENGER_INTERVAL));
        sessionIdManager.setScavengerInterval(scavengerInterval);
        
        return sessionIdManager;
    }
    
    protected RedisSessionManager createRedisSessionManager(final String jettyCluster) {
        final RedisSessionManager sessionManager = new RedisSessionManager(jettyCluster);
        
        final long saveInterval = Long.parseLong(context.getProperty(JettyRedisSession.JETTY_SESSION_SAVE_INTERVAL, JettyRedisSession.DEFAULT_SESSION_SAVE_INTERVAL));
        sessionManager.setSaveInterval(saveInterval);
        
        return sessionManager;
    }

    protected void startServer() {
        try {
            writePid2File();
            super.start();
            LOGGER.info("Current thread: {} | Idle thread: {}", super.getThreadPool().getThreads(), super.getThreadPool().getIdleThreads());
            super.join();
        } catch (final Throwable e) {
            if (e instanceof JettyServerException) {
                throw (JettyServerException) e;
            }

            throw new JettyServerException(e.getMessage(), e);
        }
    }

    /**
     * 根据PID优雅停止进程
     * 
     * @since 1.2.15
     */
    protected void stopServer() {
        try {
            final String pid = readPidFile();
            if (StringUtils.isNotBlank(pid)) {
                if (RuntimeUtil.existsProcess(pid)) {
                    RuntimeUtil.exitProcess(pid);
                    return;
                }

                return;
            }

            throw new JettyServerException("Not found jetty.pid");
        } catch (Throwable e) {
            if (e instanceof JettyServerException) {
                throw (JettyServerException) e;
            }

            throw new JettyServerException("Stop Server error: " + e.getMessage());
        } finally {
            final File file = new File(JETTY_PID_FILE);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    protected void startServerDaemon() {
        Executors.newFixedThreadPool(1, (runnable) -> {
            final Thread jetty = new Thread(runnable);
            jetty.setName("Jetty Server Deamon: " + System.currentTimeMillis());
            return jetty;
        }).execute(() -> startServer());
    }

    protected void writePid2File() {
        try {
            final String pid = RuntimeUtil.PID;
            final File file = new File(JETTY_PID_FILE);
            final Mode mode = mode(false);
            if (file.exists()) {
                if (mode == Mode.PROD) {
                    LOGGER.error("服务已启动或异常退出，请先删除jetty.pid文件后重试");
                    System.exit(1);
                } else {
                    file.delete();
                }
            }
            
            file.createNewFile();
            file.deleteOnExit();
            watcherPid(file);

            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(pid);
                writer.flush();
            }
        } catch (Throwable e) {
            if (e instanceof JettyServerException) {
                throw (JettyServerException) e;
            }

            throw new JettyServerException(e.getMessage(), e);
        }
    }
    
    protected void watcherPid(final File jettyPidFile) throws IOException {
        final WatchService watcher = FileSystems.getDefault().newWatchService();
        final Path path = Paths.get(".");
        path.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
        
        Executors.newFixedThreadPool(1, (runnable) -> {
            final Thread jetty = new Thread(runnable);
            jetty.setName("Jetty PID Watcher: " + System.currentTimeMillis());
            return jetty;
        }).execute(() -> {
            try {
                for (;;) {
                    final WatchKey watchKey = watcher.take();
                    final List<WatchEvent<?>> events = watchKey.pollEvents();
                    for(WatchEvent<?> event : events) {
                        final String fileName = ((Path) event.context()).toFile().getAbsolutePath();
                        if (jettyPidFile.getAbsolutePath().equals(fileName)) {
                            LOGGER.info("jetty.pid已被删除，应用进入退出流程");
                            System.exit(0);
                        }
                    }
                    
                    watchKey.reset();
                }
            } catch (final InterruptedException e) {
                LOGGER.info("Stoped File Wather");
            }
        });
    }

    protected String readPidFile() {
        try {
            final File file = new File(JETTY_PID_FILE);
            if (file.exists()) {
                try (final InputStream input = new FileInputStream(file); final Scanner scanner = new Scanner(input)) {
                    final StringBuilder builder = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        builder.append(scanner.nextLine());
                    }

                    return builder.toString();
                }
            }

            return StringUtils.EMPTY;
        } catch (Throwable e) {
            throw new JettyServerException("Read PID file error: " + e.getMessage());
        }
    }

    public final void bootstrap(String... args) {
        if (args.length > 0) {
            final Mode mode = mode(true);
            final Commands cmd = cmd(args, mode);
            switch (cmd) {
                case START:
                    startServerDaemon();
                    break;
                case STOP:
                    stopServer();
                    break;
                case VERSION:
                    version();
                    break;
                case HELP:
                    usage();
                    break;
            }
            
        } else {
            usage();
        }
    }
    
    protected Commands cmd(final String[] args, final Mode mode) {
        Commands cmd;
        try {
            cmd = Commands.valueOf(args[0].toUpperCase());
        } catch (final Throwable e) {
            if (mode == Mode.DEV) {
                cmd = Commands.START;
            } else {
                throw new JettyServerException("Unknown command in args list");
            }
        }
        
        return cmd;
    }
    
    protected Mode mode(final boolean output) {
        Mode mode;
        try {
            mode = Mode.valueOf(context.getProperty(ApplicationContext.MODE, Mode.PROD.name()));
            if (output) {
                switch (mode) {
                    case DEV:
                        System.out.println("Please set <context.mode> in context.properties to set 'PROD' mode.");
                        break;
                    case PROD:
                        System.out.println("Please set <context.mode> in context.properties to set 'DEV' mode.");
                        break;
                }
            }
        } catch (final Throwable e) {
            if (output) {
                System.out.println("Unknown Application Mode, setting default Mode: 'PROD'.");
                System.out.println("Please set <context.mode> in context.properties to set 'DEV' or 'PROD' mode.");
            }
            
            mode = Mode.PROD;
        }
        
        return mode;
    }
    
    protected void version() {
        final StringBuilder versionBuilder = new StringBuilder();
        versionBuilder.append("NanoFramework Version: ");
        versionBuilder.append(ApplicationContext.FRAMEWORK_VERSION);
        versionBuilder.append('\n');
        
        final String appContext = context.getProperty(ApplicationContext.CONTEXT_ROOT, "");
        final String appVersion = context.getProperty(ApplicationContext.VERSION, "0.0.0");
        versionBuilder.append("Application[");
        versionBuilder.append(appContext);
        versionBuilder.append("] Version: ");
        versionBuilder.append(appVersion);
        versionBuilder.append('\n');
        System.out.println(versionBuilder.toString());
    }
    
    protected void usage() {
        final StringBuilder usageBuilder = new StringBuilder();
        usageBuilder.append("Usage: \n\n");
        usageBuilder.append("    ./bootstrap.sh command\n\n");
        usageBuilder.append("The commands are: \n");
        usageBuilder.append("    start        Start Application on Jetty Server\n");
        usageBuilder.append("    stop         Stop Application\n");
        usageBuilder.append("    version      Show the NanoFramwork and Application version\n\n");
        usageBuilder.append("Use \"./bootstrap.sh help\" for more information about a command.\n");
        System.out.println(usageBuilder.toString());
    }
}