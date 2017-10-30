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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.nanoframework.commons.exception.ZipException;

/**
 * @author yanghe
 * @since 1.0
 */
public final class ZipUtils {
    private static final int BUFFER_SIZE = 1024;

    private ZipUtils() {

    }

    public static String gzip(final String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        return gzip(value.getBytes(Charsets.UTF_8));
    }

    public static String gzip(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return StringUtils.EMPTY;
        }

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); final GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            return new String(Base64.getEncoder().encode(out.toByteArray()), Charsets.UTF_8);
        } catch (final IOException e) {
            throw new ZipException(e.getMessage(), e);
        }
    }

    public static String gunzip(final String value) {
        if (value == null) {
            return value;
        }

        return new String(gunzipToByte(value), Charsets.UTF_8);
    }

    public static byte[] gunzipToByte(final String value) {
        if (value == null) {
            return null;
        }

        final byte[] compressed = Base64.getDecoder().decode(value);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final GZIPInputStream ginzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {

            final byte[] buffer = new byte[BUFFER_SIZE];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }

            return out.toByteArray();
        } catch (final IOException e) {
            throw new ZipException(e.getMessage(), e);
        }
    }

    public static final String zip(final String value) {
        if (value == null) {
            return null;
        }

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); final ZipOutputStream zout = new ZipOutputStream(out)) {
            zout.putNextEntry(new ZipEntry("0"));
            zout.write(value.getBytes());
            zout.closeEntry();
            return new String(Base64.getEncoder().encode(out.toByteArray()), Charsets.UTF_8);
        } catch (final IOException e) {
            throw new ZipException(e.getMessage(), e);
        }
    }

    public static final String unzip(final String value) {
        if (value == null) {
            return null;
        }

        final byte[] compressed = Base64.getDecoder().decode(value);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(compressed))) {
            zin.getNextEntry();
            final byte[] buffer = new byte[BUFFER_SIZE];
            int offset = -1;
            while ((offset = zin.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }

            return out.toString(Charsets.UTF_8.name());
        } catch (final IOException e) {
            throw new ZipException(e.getMessage(), e);
        }
    }

}