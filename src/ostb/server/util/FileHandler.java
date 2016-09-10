package ostb.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;

import net.minecraft.util.org.apache.commons.io.FileUtils;
import ostb.OSTB;

public class FileHandler {
    public static void checkForUpdates() {
        String path = "/root/resources/";
        for(String plugin : new String [] {"Core.jar", "NPC.jar", "EffectLib.jar", "ProtocolLib.jar", "HolographicDisplays.jar", "HolographicDisplaysPatch.jar"}) {
            File file = new File(path, plugin);
            Bukkit.getLogger().info(file.getAbsolutePath());
            if(file.exists()) {
                File update = new File(OSTB.getInstance().getDataFolder() + "/../" + plugin);
                if(update.exists()) {
                    delete(update);
                }
                copyFile(file, update);
            }
        }
    }

    public static boolean isImage(String url) {
        try {
            return ImageIO.read(new URL(url)) != null;
        } catch(MalformedURLException e) {

        } catch(IOException e) {

        }
        return false;
    }

    public static void downloadImage(String urlString, String path) {
        InputStream is = null;
        OutputStream os = null;
        try {
            URL url = new URL(urlString);
            is = url.openStream();
            os = new FileOutputStream(path);
            byte[] b = new byte[2048];
            int length = 0;
            while((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch(IOException e) {

            } catch(NullPointerException e) {

            }
            try {
                os.close();
            } catch(IOException e) {

            } catch(NullPointerException e) {

            }
        }
    }

    public static boolean copyFile(String source, String target) {
        return copyFile(new File(source), new File(target));
    }

    public static boolean copyFile(File source, File target) {
        try {
            //FileUtil.copy(source, target);
            FileUtils.copyFile(source, target);
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyFolder(String source, String target) {
        return copyFolder(new File(source), new File(target));
    }

    public static boolean copyFolder(File source, File target) {
        try {
            //FileUtil.copy(source, target);
            FileUtils.copyDirectory(source, target);
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void delete(File file) {
        if(file != null && file.exists()) {
            if(file.isDirectory()) {
                if(file.list().length == 0) {
                    file.delete();
                } else {
                    String[] files = file.list();
                    for(String temp : files) {
                        File fileDelete = new File(file, temp);
                        delete(fileDelete);
                    }
                    if(file.list().length == 0) {
                        file.delete();
                    }
                }
            } else {
                file.delete();
            }
        }
    }
}
