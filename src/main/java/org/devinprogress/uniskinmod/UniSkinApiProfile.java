package org.devinprogress.uniskinmod;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Universal Skin API
 */
public class UniSkinApiProfile {
    private static Logger log = LogManager.getLogger("UniSkinAPI");

    private class ProfileJSON {
        public String player_name;
        public long last_update;
        public List<String> model_preference;
        public Map<String, String> skins;
        public String cape;
    }

    public boolean hasProfile = false;
    private int errorNo;
    private String root = null;

    private boolean checkExist(String url) {
        try {
//            log.info("Fetching URL: " + url);
            HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();//.openConnection(Minecraft.getMinecraft().getProxy());
            conn.setReadTimeout(1000 * 5);
            conn.setConnectTimeout(1000 * 5);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
//            log.info("Connect URL: " + url);
            errorNo = conn.getResponseCode();
            if (errorNo > 299 || errorNo < 200) {
                log.error("rspCode not 2xx: " + url);
                return false;
            }
//            log.info("Fetched URL: " + url);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String getDirectUrl(String link) {
        String resultUrl = link;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(link).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String locationUrl = connection.getHeaderField("Location");

                if (locationUrl != null && locationUrl.trim().length() > 0) {
                    IOUtils.close(connection);
                    if(locationUrl.startsWith("http")) {
                        resultUrl = getDirectUrl(locationUrl);
                    }
                    else if(locationUrl.startsWith("/")) {
                        resultUrl = getDirectUrl(root + locationUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.error("error getDirectUrl", e);
        } finally {
            IOUtils.close(connection);
        }
        return resultUrl;
    }

    private static final String URL_SKIN_FMT = "{root}/MinecraftSkins/{player_name}.png";
    private static final String URL_CLOAK_FMT = "{root}/MinecraftCloaks/{player_name}.png";
    private String skin = null, cape = null, model = null;
    private long update = 0;

    public static UniSkinApiProfile getProfile(final String name, final String Root) {
        UniSkinApiProfile prof = new UniSkinApiProfile(name, Root);
        return prof.hasProfile ? prof : null;
    }

    private UniSkinApiProfile(String name, String root) {
        this.root = root;
        String skinURL = URL_SKIN_FMT.replace("{root}", root).replace("{player_name}", name);
        String capeURL = URL_CLOAK_FMT.replace("{root}", root).replace("{player_name}", name);

        if (checkExist(skinURL)) {
            hasProfile = true;
            skin = getDirectUrl(skinURL);
            log.info("getDirectUrl: " + skin);
        }

        if (checkExist(capeURL)) {
            hasProfile = true;
            cape = getDirectUrl(capeURL);
            log.info("getDirectUrl: " + cape);
        }
    }

    public String getSkinURL() {
        return skin;
    }

    public String getCapeURL() {
        return cape;
    }

    public String getModel() {
        return model;
    }

    public long lastUpdate() {
        return update;
    }
}