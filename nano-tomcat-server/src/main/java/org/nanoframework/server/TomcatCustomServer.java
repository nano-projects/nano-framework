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

import static org.nanoframework.server.cfg.ConnectorConf.TOMCAT_CONNECTOR;
import static org.nanoframework.server.cfg.ExecutorConf.TOMCAT_EXECUTOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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

import javax.servlet.ServletException;

import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.server.cfg.ConnectorConf;
import org.nanoframework.server.cfg.ExecutorConf;
import org.nanoframework.server.cmd.Commands;
import org.nanoframework.server.cmd.Mode;
import org.nanoframework.server.exception.TomcatServerException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class TomcatCustomServer extends Tomcat {
    public static final String READY = "org.apache.catalina.startup.READY";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatCustomServer.class);

    private static final String DEFAULT_TOMCAT_BASE_TEMP_DIR = "tomcat-base";
    
    private static final String TOMCAT_PID_FILE = "tomcat.pid";
    
    private String resourceBase = "./webRoot";
    
    private String defaultWebXmlPath = resourceBase + "/WEB-INF/default.xml";
    
    private Properties context;
    
    static {
        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
    }
    
    private TomcatCustomServer() throws Throwable {
        super();
        loadContext(ApplicationContext.MAIN_CONTEXT);
        init(context.getProperty(ApplicationContext.CONTEXT_ROOT), null);
    }
    
    private TomcatCustomServer(final String contextPath) throws Throwable {
        super();
        loadContext(contextPath);
        init(context.getProperty(ApplicationContext.CONTEXT_ROOT), null);
    }
    
    public static TomcatCustomServer server() throws Throwable {
        return new TomcatCustomServer();
    }
    
    public static TomcatCustomServer server(final String contextPath) throws Throwable {
        return new TomcatCustomServer(contextPath);
    }
    
    protected void loadContext(final String contextPath) {
        Assert.hasLength(contextPath, "无效的context属性文件路径");
        context = PropertiesLoader.load(contextPath);
    }
    
    protected void init(final String contextRoot, final String resourceBase) throws ServletException, IOException {
        final Path basePath = Files.createTempDirectory(DEFAULT_TOMCAT_BASE_TEMP_DIR);
        setBaseDir(basePath.toString());
        
        if (StringUtils.isNotEmpty(resourceBase)) {
            this.resourceBase = resourceBase;
        }
        
        initExecutor();
        initConnector();
        
        final ContextConfig conf = new ContextConfig();
        final StandardContext ctx = (StandardContext) this.addWebapp(getHost(), contextRoot, new File(this.resourceBase).getAbsolutePath(), conf);
        conf.setDefaultWebXml(defaultWebXmlPath);
        for (LifecycleListener listen : ctx.findLifecycleListeners()) {
            if (listen instanceof DefaultWebXmlListener) {
                ctx.removeLifecycleListener(listen);
            }
        }
        
        ctx.setParentClassLoader(TomcatCustomServer.class.getClassLoader());
        
        //Disable TLD scanning by default
        if (System.getProperty(Constants.SKIP_JARS_PROPERTY) == null && System.getProperty(Constants.SKIP_JARS_PROPERTY) == null) {
            LOGGER.debug("disabling TLD scanning");
            StandardJarScanFilter jarScanFilter = (StandardJarScanFilter) ctx.getJarScanner().getJarScanFilter();
            jarScanFilter.setTldSkip("*");
        }
    }
    
    protected void initExecutor() {
        final TypeReference<ExecutorConf> type = new TypeReference<ExecutorConf>() { };
        final ExecutorConf conf = new ExecutorConf(JSON.parseObject(context.getProperty(TOMCAT_EXECUTOR), type)); 
        LOGGER.debug("{}", conf.toString());
        final Executor executor = conf.init();
        getService().addExecutor(executor);
    }
    
    @SuppressWarnings("rawtypes")
    protected void initConnector() {
        final TypeReference<ConnectorConf> type = new TypeReference<ConnectorConf>() { };
        final ConnectorConf conf = new ConnectorConf(JSON.parseObject(context.getProperty(TOMCAT_CONNECTOR), type));
        LOGGER.debug("{}", conf.toString());
        final Connector connector = conf.init();
        final Service service = getService();
        final Executor executor = service.getExecutor(conf.getExecutor());
        ((AbstractProtocol) connector.getProtocolHandler()).setExecutor(executor);
        setConnector(connector);
        service.addConnector(connector);
    }
    
    protected void startServer() {
        try {
            writePid2File();
            this.start();
            System.setProperty(READY, "true");
            this.getServer().await();
        } catch (final Throwable e) {
            LOGGER.error("Bootstrap server error: {}", e.getMessage());
            System.exit(1);
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

            throw new TomcatServerException("Not found tomcat.pid");
        } catch (Throwable e) {
            if (e instanceof TomcatServerException) {
                throw (TomcatServerException) e;
            }

            throw new TomcatServerException("Stop Server error: " + e.getMessage());
        } finally {
            final File file = new File(TOMCAT_PID_FILE);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    protected void startServerDaemon() {
        Executors.newFixedThreadPool(1, (runnable) -> {
            final Thread tomcat = new Thread(runnable);
            tomcat.setName("Tomcat Server Deamon: " + System.currentTimeMillis());
            return tomcat;
        }).execute(() -> startServer());
    }

    protected void writePid2File() {
        try {
            final String pid = RuntimeUtil.PID;
            final File file = new File(TOMCAT_PID_FILE);
            final Mode mode = mode(false);
            if (file.exists()) {
                if (mode == Mode.PROD) {
                    LOGGER.error("服务已启动或异常退出，请先删除tomcat.pid文件后重试");
                    System.exit(1);
                } else if (RuntimeUtil.existsProcess(readPidFile())) {
                    LOGGER.error("服务已启动，请先停止原进程");
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
            if (e instanceof TomcatServerException) {
                throw (TomcatServerException) e;
            }

            throw new TomcatServerException(e.getMessage(), e);
        }
    }
    
    protected void watcherPid(final File pidFile) throws IOException {
        final WatchService watcher = FileSystems.getDefault().newWatchService();
        final Path path = Paths.get(".");
        path.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
        
        Executors.newFixedThreadPool(1, (runnable) -> {
            final Thread tomcat = new Thread(runnable);
            tomcat.setName("Tomcat PID Watcher: " + System.currentTimeMillis());
            return tomcat;
        }).execute(() -> {
            try {
                for (;;) {
                    final WatchKey watchKey = watcher.take();
                    final List<WatchEvent<?>> events = watchKey.pollEvents();
                    for(WatchEvent<?> event : events) {
                        final String fileName = ((Path) event.context()).toFile().getAbsolutePath();
                        if (pidFile.getAbsolutePath().equals(fileName)) {
                            LOGGER.info("tomcat.pid已被删除，应用进入退出流程");
                            System.exit(0);
                        }
                    }
                    
                    watchKey.reset();
                }
            } catch (final InterruptedException e) {
                LOGGER.info("Stoped File Watcher");
            }
        });
    }

    protected String readPidFile() {
        try {
            final File file = new File(TOMCAT_PID_FILE);
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
            throw new TomcatServerException("Read PID file error: " + e.getMessage());
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
                throw new TomcatServerException("Unknown command in args list");
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
        usageBuilder.append("    start        Start Application on Tomcat Server\n");
        usageBuilder.append("    stop         Stop Application\n");
        usageBuilder.append("    version      Show the NanoFramwork and Application version\n\n");
        usageBuilder.append("Use \"./bootstrap.sh help\" for more information about a command.\n");
        System.out.println(usageBuilder.toString());
    }
}
