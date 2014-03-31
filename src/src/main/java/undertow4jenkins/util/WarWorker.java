package undertow4jenkins.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import undertow4jenkins.Launcher;

public class WarWorker {

    // private static final Logger log = LoggerFactory
    // .getLogger("undertow4jenkins.util.WarClassLoader");

    public static ClassLoader createJarsClassloader(String warfile, String warDir)
            throws IOException {
        final String relativeLibPath = "WEB-INF/lib/";
        File libDir = new File(warDir + relativeLibPath);

        List<URL> jarUrls = new ArrayList<URL>();
        for (File file : libDir.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                jarUrls.add(file.toURI().toURL());
            }
        }

        return new URLClassLoader(
                jarUrls.toArray(new URL[jarUrls.size()]),
                Launcher.class.getClassLoader());
    }

    public static void extractFilesFromWar(String warfilePath, String pathToTmpDir)
            throws IOException {
        byte buffer[] = new byte[8192];

        JarFile warArchive = new JarFile(warfilePath);
        for (Enumeration<JarEntry> e = warArchive.entries(); e.hasMoreElements();) {
            JarEntry element = (JarEntry) e.nextElement();
            if (element.isDirectory())
                continue;

            File outFile = new File(pathToTmpDir + element.getName());

            // TODO check if the file in WAR is not new version of file (check timestamp)
            if (!outFile.exists()) {
                copyFile(buffer, warArchive, element, outFile);
            }
        }
        warArchive.close();
    }

    private static void copyFile(byte[] buffer, JarFile warArchive, JarEntry element, File outFile)
            throws IOException, FileNotFoundException {
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
}
