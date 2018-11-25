package com.github.vimcmd.abook.commons.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Util {

    private Util() {}

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static String getDomainNameUnchecked(String url) {
        String name = "";
        try {
            name = getDomainName(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return name;
    }

    public static String getMarkdownUrl(String title, String url) {
        return "[" + title + "](" + url + ")";
    }

    // public static byte[] getByteArrayFromUrl(String urlString) {
    //     InputStream in = null;
    //     try {
    //         URL url = new URL(urlString);
    //         in = new BufferedInputStream(url.openStream());
    //         ByteArrayOutputStream out = new ByteArrayOutputStream();
    //         byte[] buf = new byte[1024];
    //         int n = 0;
    //         while (-1 != (n = in.read(buf))) {
    //             out.write(buf, 0, n);
    //         }
    //         out.close();
    //         in.close();
    //         return out.toByteArray();
    //
    //     } catch (IOException e) {
    //         logger.error(e.getLocalizedMessage(), e);
    //         throw new UrlIOException(e);
    //     } finally {
    //         try {
    //             if (in != null) {
    //                 in.close();
    //             }
    //         } catch (IOException e) {
    //             logger.error(e.getLocalizedMessage(), e);
    //         }
    //     }
    // }

    private static class UrlIOException extends RuntimeException {
        UrlIOException(Throwable cause) {
            super(cause);
        }
    }

}
