package org.nanoframework.commons.util;

import java.nio.charset.Charset;

import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * @author yanghe
 * @date 2016年3月6日 下午1:03:10
 */
public final class Charsets {
	/**
     * Seven-bit ASCII. ISO646-US. The Basic Latin block of the Unicode character set.
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * Eight-bit UCS Transformation Format.
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Sixteen-bit UCS Transformation Format, big-endian byte order.
     */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    /**
     * Sixteen-bit UCS Transformation Format, little-endian byte order.
     */
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

    /**
     * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark.
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");

    /**
     * Returns a Charset, if possible the Charset for the specified {@code charsetName}, otherwise (if the specified
     * {@code charsetName} is {@code null} or not supported) this method returns the platform default Charset.
     *
     * @param charsetName
     *            name of the preferred charset or {@code null}
     * @return a Charset, not null.
     */
    public static Charset getSupportedCharset(final String charsetName) {
        return getSupportedCharset(charsetName, Charset.defaultCharset());
    }

    /**
     * Returns a Charset, if possible the Charset for the specified {@code charsetName}, otherwise (if the specified
     * {@code charsetName} is {@code null} or not supported) this method returns the platform default Charset.
     *
     * @param charsetName
     *            name of the preferred charset or {@code null}
     * @param defaultCharset
     *            returned if {@code charsetName} is null or is not supported.
     * @return a Charset, never null.
     */
    public static Charset getSupportedCharset(final String charsetName, final Charset defaultCharset) {
        Charset charset = null;
        if (charsetName != null && Charset.isSupported(charsetName)) {
            charset = Charset.forName(charsetName);
        }
        
        if (charset == null) {
            charset = defaultCharset;
            if (charsetName != null) {
                LoggerFactory.getLogger(Charsets.class).error("Charset " + charsetName + " is not supported for layout, using " + charset.displayName());
            }
        }
        
        return charset;
    }

    private Charsets() {
    }
	
}
