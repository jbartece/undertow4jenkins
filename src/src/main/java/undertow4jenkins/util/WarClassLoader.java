package undertow4jenkins.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.Launcher;

public class WarClassLoader {

    private static final Logger log = LoggerFactory
            .getLogger("undertow4jenkins.util.WarClassLoader");

    public static ClassLoader createJarsClassloader(String warfile) {
        byte buffer[] = new byte[8192];
        try {
            JarFile warArchive = new JarFile(warfile);

            List<URL> jarUrls = new ArrayList<URL>();

            for (Enumeration<JarEntry> e = warArchive.entries(); e.hasMoreElements();) {
                JarEntry element = (JarEntry) e.nextElement();

                if (element.getName().startsWith("WEB-INF/lib/") &&
                        element.getName().endsWith(".jar")) {
                    log.trace("Jar entry: " + element.getName());

                    File outFile = new File("/tmp/undertow4jenkins/" + element.getName());

                    if (!outFile.exists()) {
                        outFile.getParentFile().mkdirs();
                        // Copy out the extracted file
                        InputStream inContent = warArchive.getInputStream(element);
                        OutputStream outStream = new FileOutputStream(outFile);
                        int readBytes = inContent.read(buffer);
                        while (readBytes != -1) {
                            outStream.write(buffer, 0, readBytes);
                            readBytes = inContent.read(buffer);
                        }
                        inContent.close();
                        outStream.close();
                    }

                    jarUrls.add(outFile.toURI().toURL());
                }

            }
            warArchive.close();

            log.trace("Jar URLs: " + jarUrls.toString());
            // URL[] warFileURL = new URL[] { new File(options.warfile + File.separator
            // + "WEB-INF/lib/stapler-1.223.jar").toURI().toURL() };

            // this.jenkinsWarClassLoader = new URLClassLoader(warFileURL,
            // getClass().getClassLoader());
            // log.debug("Created ClassLoader for jenkins.war: " + warFileURL[0].toString());
            return new URLClassLoader(
                    jarUrls.toArray(new URL[jarUrls.size()]),
                    Launcher.class.getClassLoader());
        } catch (MalformedURLException e) {
            log.error("Bad path to jenkins.war file!", e);
        } catch (IOException e) {
            log.error("War archive", e);
        }
        return null;
    }
}
