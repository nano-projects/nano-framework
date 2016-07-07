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
package org.nanoframework.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

import org.nanoframework.commons.crypt.CryptUtil;

/**
 * 系统运行时功能扩展类
 * 
 * @author yanghe
 * @since 1.0
 */
public class RuntimeUtil {

    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    public static final String OSNAME = System.getProperty("os.name");
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() + 1;
    public static final String TOKEN = MD5Utils
            .getMD5String(CryptUtil.encrypt("nanoframework.TOKEN::encrypt:org.nanoframework.commons.util.RuntimeUtil", null));

    /**
     * 杀死当前系统进行
     * 
     * @throws IOException IO异常
     */
    public static void killProcess() throws IOException {
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            String[] cmds = new String[] { "/bin/sh", "-c", "kill -9 " + PID };
            Runtime.getRuntime().exec(cmds);

        } else if (OSNAME.indexOf("Windows") > -1) {
            Runtime.getRuntime().exec("cmd /c taskkill /pid " + PID + " /f ");

        }

    }

    /**
     * 根据进程号杀死对应的进程
     * 
     * @param PID 进程号
     * @throws IOException IO异常
     */
    public static void killProcess(String PID) throws IOException {
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            String[] cmds = new String[] { "/bin/sh", "-c", "kill -9 " + PID };
            Runtime.getRuntime().exec(cmds);

        } else if (OSNAME.indexOf("Windows") > -1) {
            Runtime.getRuntime().exec("cmd /c taskkill /pid " + PID + " /f ");

        }

    }

    /**
     * 根据进程号优雅退出进程
     * 
     * @param PID 进程号
     * @throws IOException IO异常
     */
    public static void exitProcess(String PID) throws IOException {
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            String[] cmds = new String[] { "/bin/sh", "-c", "kill -15 " + PID };
            Runtime.getRuntime().exec(cmds);

        } else if (OSNAME.indexOf("Windows") > -1) {
            Runtime.getRuntime().exec("cmd /c taskkill /pid " + PID + " /f ");

        }

    }

    /**
     * 根据进程号查询该进程是否存在
     * 
     * @param PID 进程号
     * @return 查询结果
     * @throws IOException IO异常
     */
    public static boolean existsProcess(String PID) throws IOException {

        if (PID == null || "".equals(PID))
            return false;

        Process process = null;
        boolean exsits = false;
        String result = null;
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            String[] cmds = new String[] { "/bin/sh", "-c", "ps -f -p " + PID };
            process = Runtime.getRuntime().exec(cmds);

            InputStream in = process.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(in));

            while ((result = input.readLine()) != null) {
                if (StringUtils.isNotEmpty(result) && result.indexOf(PID) > -1) {
                    exsits = true;
                }
            }

        } else if (OSNAME.indexOf("Windows") > -1) {
            process = Runtime.getRuntime().exec("cmd /c Wmic Process where ProcessId=\"" + PID + "\" get ExecutablePath ");

            InputStream in = process.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(in));

            while ((result = input.readLine()) != null) {
                if (StringUtils.isNotEmpty(result) && result.indexOf("No Instance(s) Available") < 0) {
                    exsits = true;
                }
            }

        }

        return exsits;
    }

    /**
     * 判断当前运行的系统是否是Windows.
     * @return boolean
     */
    public static boolean isWindows() {
        if (OSNAME.contains("Windows"))
            return true;
        else
            return false;
    }

    /**
     * 根据Class获取该Class所在的磁盘路径.
     * @param clz 查询的类
     * @return 返回该类的所在位置
     */
    public static String getPath(Class<?> clz) {
        String runJarPath = clz.getProtectionDomain().getCodeSource().getLocation().getPath();
        String tmpPath = runJarPath.substring(0, runJarPath.lastIndexOf('/'));
        if (tmpPath.endsWith("/lib")) {
            tmpPath = tmpPath.replace("/lib", "");
        }

        return tmpPath.substring(isWindows() ? 1 : 0, tmpPath.lastIndexOf('/')) + '/';
    }

    /**
     * 获取运行时中的所有Jar文件
     * @return List
     * @throws IOException if I/O error occur
     */
    public static List<JarFile> classPaths() throws IOException {
        String[] classPaths = System.getProperty("java.class.path").split(":");
        if (classPaths.length > 0) {
            List<JarFile> jars = new ArrayList<>(classPaths.length);
            for (final String classPath : classPaths) {
                if (!classPath.endsWith("jar"))
                    continue;

                JarFile jar = new JarFile(new File(classPath));
                jars.add(jar);

            }

            return jars;
        }

        return Collections.emptyList();
    }

}
