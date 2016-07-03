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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:04:40
 */
@SuppressWarnings("restriction")
public class ZipUtils {

    private static Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    private ZipUtils() {
    }

    /**
     * 
     * 使用gzip进行压缩
     */
    public static String gzip(String primStr) {
        if (StringUtils.isEmpty(primStr))
            return primStr;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;

        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(primStr.getBytes());

        } catch (IOException e) {
            LOG.error("对字符串使用GZIP压缩时异常：" + e.getMessage(), e);

        } finally {
            if (gzip != null) {
                try {
                    gzip.flush();
                    gzip.close();
                    gzip = null;

                } catch (IOException e) {
                    LOG.error("关闭GZIP流时异常：" + e.getMessage(), e);

                }
            }
        }

        return BASE64.getInstance().encode(out.toByteArray());

    }

    public static String gzip(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return "";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;

        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(bytes);

        } catch (IOException e) {
            LOG.error("对字符串使用GZIP压缩时异常：" + e.getMessage(), e);

        } finally {
            if (gzip != null) {
                try {
                    gzip.flush();
                    gzip.close();
                    gzip = null;

                } catch (IOException e) {
                    LOG.error("关闭GZIP流时异常：" + e.getMessage(), e);

                }
            }
        }

        return BASE64.getInstance().encode(out.toByteArray());
    }

    /**
     * 
     * <p>
     * Description:使用gzip进行解压缩
     * </p>
     * 
     * @param compressedStr
     * @return
     */
    public static String gunzip(String compressedStr) {
        if (StringUtils.isEmpty(compressedStr))
            return StringUtils.EMPTY;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;

        try {
            compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);

            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }

            decompressed = out.toString();

        } catch (IOException e) {
            if (e.getMessage() == null || e.getMessage().indexOf("Not in GZIP format") > -1)
                return compressedStr;

            LOG.error("对字符串使用GZIP解压缩时异常：" + e.getMessage(), e);

        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                    ginzip = null;

                } catch (IOException e) {
                    LOG.error("关闭GZIP流时异常：" + e.getMessage(), e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                    in = null;

                } catch (IOException e) {
                    LOG.error("关闭Input流时异常：" + e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;

                } catch (IOException e) {
                    LOG.error("关闭Output流时异常：" + e.getMessage(), e);

                }
            }
        }

        return decompressed;

    }

    /**
     * 
     * <p>
     * Description:使用gzip进行解压缩
     * </p>
     * 
     * @param compressedStr
     * @return
     */
    public static byte[] gunzipToByte(String compressedStr) {
        if (compressedStr == null)
            return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        byte[] decompressed = null;

        try {
            compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);

            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }

            decompressed = out.toByteArray();

        } catch (IOException e) {
            LOG.error("对字符串使用GZIP解压缩时异常：" + e.getMessage(), e);

        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                    ginzip = null;

                } catch (IOException e) {
                    LOG.error("关闭GZIP流时异常：" + e.getMessage(), e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                    in = null;

                } catch (IOException e) {
                    LOG.error("关闭Input流时异常：" + e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;

                } catch (IOException e) {
                    LOG.error("关闭Output流时异常：" + e.getMessage(), e);

                }
            }
        }

        return decompressed;

    }

    /**
     * 使用zip进行压缩
     * 
     * @param str
     *            压缩前的文本
     * @return 返回压缩后的文本
     */
    public static final String zip(String str) {
        if (str == null)
            return null;
        byte[] compressed;
        ByteArrayOutputStream out = null;
        ZipOutputStream zout = null;
        String compressedStr = null;

        try {
            out = new ByteArrayOutputStream();
            zout = new ZipOutputStream(out);
            zout.putNextEntry(new ZipEntry("0"));
            zout.write(str.getBytes());
            zout.closeEntry();
            compressed = out.toByteArray();
            compressedStr = BASE64.getInstance().encodeBuffer(compressed);

        } catch (IOException e) {
            LOG.error("对字符串使用ZIP压缩时异常：" + e.getMessage(), e);
            compressed = null;

        } finally {
            if (zout != null) {
                try {
                    zout.flush();
                    zout.close();
                    zout = null;

                } catch (IOException e) {
                    LOG.error("关闭ZIP流时异常：" + e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;

                } catch (IOException e) {
                    LOG.error("关闭Output流时异常：" + e.getMessage(), e);

                }
            }
        }

        return compressedStr;

    }

    /**
     * 使用zip进行解压缩
     * 
     * @param compressed
     *            压缩后的文本
     * @return 解压后的字符串
     */
    public static final String unzip(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }

        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        ZipInputStream zin = null;
        String decompressed = null;

        try {
            byte[] compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(compressed);
            zin = new ZipInputStream(in);
            zin.getNextEntry();
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = zin.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }

            decompressed = out.toString();

        } catch (IOException e) {
            LOG.error("对字符串使用ZIP解压缩时异常：" + e.getMessage(), e);
            decompressed = null;

        } finally {
            if (zin != null) {
                try {
                    zin.close();
                    zin = null;

                } catch (IOException e) {
                    LOG.error("关闭ZIP流时异常：" + e.getMessage(), e);

                }
            }
            if (in != null) {
                try {
                    in.close();
                    in = null;

                } catch (IOException e) {
                    LOG.error("关闭Input流时异常：" + e.getMessage(), e);

                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;

                } catch (IOException e) {
                    LOG.error("关闭Output流时异常：" + e.getMessage(), e);

                }
            }
        }

        return decompressed;

    }

    public static void main(String[] args) {
        System.out.println(gzip("admin"));

    }

}