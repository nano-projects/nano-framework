/*
 * Copyright © 2015-2017 the original author or authors.
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
package org.nanoframework.extension.elasticjob.tests.job;

import org.nanoframework.extension.elasticjob.AbstractScriptJob;
import org.nanoframework.extension.elasticjob.ElasticJob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * @author wangtong
 * @since 1.4.11
 */
@ElasticJob("3")
public class TestScriptJob extends AbstractScriptJob {
    public static volatile boolean completed;

    @Override
    public String execute() throws IOException {
        completed = true;
        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return Paths.get(TestScriptJob.class.getResource("/script/demo.bat").getPath().substring(1)).toString();
        }
        Path result = Paths.get(TestScriptJob.class.getResource("/script/demo.sh").getPath());
        Files.setPosixFilePermissions(result, PosixFilePermissions.fromString("rwxr-xr-x"));
        return result.toString();
    }
}
