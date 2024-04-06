package dev.jwall64.mcdl.util;

import dev.jwall64.mcdl.Task;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;

import java.io.File;

public class MidnightProxyUtil {

    private static final String LATEST_API = "https://api.github.com/repos/jwall64/MidnightProxy/releases/latest";
    private static final String ARTIFACT_URL = "https://github.com/jwall64/MidnightProxy/releases/download/v%s/midnightproxy-proxy-%s.jar";

    public static Task.Result downloadMidnightProxy(String version, File output) {
        if(!DownloadUtil.downloadBytes(ARTIFACT_URL.formatted(version, version), output)) {
            return Task.Result.error("Unable to download MidnightProxy!");
        }
        return Task.Result.success();
    }
    public static String getLatestProxyVersion() {
        ConfigObject obj = DownloadUtil.downloadJSON(LATEST_API);
        if(obj == null || !obj.isSection()) return null;

        String tagName = obj.asSection().getOrDefault("tag_name", (String) null);
        if(tagName != null) {
            return tagName.substring(1); // Cut off the 'v'
        }
        return null;
    }

    public static final Task DOWNLOAD = queue -> {

        ConfigSection config = queue.getConfig();

        String serverVersion = config.getString("version");
        File serverJar = new File(config.getString("jarName"));

        if(serverVersion.equals("latest")) {
            serverVersion = getLatestProxyVersion();
        }

        queue.getLogger().info(String.format("Downloading MidnightProxy version %s...", serverVersion));
        Task.Result dlRes = downloadMidnightProxy(serverVersion, serverJar);
        if(dlRes.isError()) return dlRes;

        return Task.Result.success();
    };

    public static final Task CONFIGURE = queue -> {

        ConfigSection config = queue.getConfig();
        ConfigSection out = new ConfigSection();

        // Port
        if (config.has("port")) {
            try {
                int port = Integer.parseInt(config.getString("port"));
                if(port < 1 || port > Short.MAX_VALUE) {
                    return Task.Result.error("Server port out of range [1,65535]!");
                }
                out.set("port", port);
            } catch (NumberFormatException ex) {
                return Task.Result.error("Unable to parse port number!");
            }
        }

        FileWrapper<ConfigObject> wrapper = new FileWrapper<>(ConfigContext.INSTANCE, JSONCodec.fileCodec(), new File(FileUtil.getWorkingDir(queue.getConfig()), "config.json"));
        wrapper.setRoot(out);
        wrapper.save();

        return Task.Result.success();
    };

}
