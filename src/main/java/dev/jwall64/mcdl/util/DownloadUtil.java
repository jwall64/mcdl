package dev.jwall64.mcdl.util;

import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.codec.JSONCodec;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUtil {

    public static ConfigObject downloadJSON(String url) {

        try {
            URL actualUrl = new URL(url);
            URLConnection conn = actualUrl.openConnection();

            return JSONCodec.loadConfig(conn.getInputStream());

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean downloadBytes(String url, File output) {

        try {
            URL actualUrl = new URL(url);
            URLConnection conn = actualUrl.openConnection();
            FileOutputStream outputStream = new FileOutputStream(output);

            int bytesRead;
            byte[] buffer = new byte[1024];
            while((bytesRead = conn.getInputStream().read(buffer, 0, 1024)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            return true;

        } catch (IOException ex) {

            ex.printStackTrace();
            return false;
        }

    }
}
