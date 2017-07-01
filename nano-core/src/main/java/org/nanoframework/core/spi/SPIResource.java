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
package org.nanoframework.core.spi;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nanoframework.commons.entity.BaseEntity;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
public class SPIResource extends BaseEntity {
    public static final SPIResource EMPTY = SPIResource.create(Collections.emptyList(), Collections.emptyMap());
    private static final long serialVersionUID = 2184606147032384544L;

    private final List<File> files;
    private final Map<String, List<InputStream>> streams;

    private SPIResource(final List<File> files, final Map<String, List<InputStream>> streams) {
        this.files = files;
        this.streams = streams;
    }

    public static SPIResource create(final List<File> files, final Map<String, List<InputStream>> streams) {
        return new SPIResource(files, streams);
    }

    public List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public Map<String, List<InputStream>> getStreams() {
        return Collections.unmodifiableMap(streams);
    }
}
