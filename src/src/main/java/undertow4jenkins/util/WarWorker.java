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

public class WarWorker {

    public static ClassLoader createJarsClassloader(String warfile,
            String commonLib, String warDir, ClassLoader parentClassLoader)
            throws IOException {
        List<URL> jarUrls = new ArrayList<URL>();

        // Add lib dir to ClassLoader
        final String relativeLibPath = "WEB-INF/lib/";
        File libDir = new File(warDir + relativeLibPath);
        if (libDir.exists()) {
            for (File file : libDir.listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    jarUrls.add(file.toURI().toURL());
                }
            }
        }

        // Add classes to ClassLoader
        File classesDir = new File(warDir + "WEB-INF/classes");
        if (classesDir.exists()) {
            jarUrls.add(classesDir.toURI().toURL());
        }

        // Add common lib to ClassLoader
        File commonLibDir = new File(commonLib);
        if (commonLibDir.exists() && commonLibDir.isDirectory()) {
            for (File file : commonLibDir.listFiles()) {
                if (file.getName().endsWith(".jar") ||
                        file.getName().endsWith(".zip")) {
                    jarUrls.add(file.toURI().toURL());
                }
            }
        }

        return new URLClassLoader(
                jarUrls.toArray(new URL[jarUrls.size()]), parentClassLoader);
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

    public static String createWebApplicationRoot(String warfile, String webroot) 
            throws IOException {
        if (warfile != null) {
            File warfileFile = new File(warfile);
            if(! warfileFile.isFile() )
                throw new IOException("Specified warfile does not exists!");
            
            String targetWebrootDir = webroot;
            if(webroot == null)
                targetWebrootDir = createAbstractTempDir(warfileFile.getName());
            else   
                deleteDirectory(new File(targetWebrootDir));
            
            extractFilesFromWar(warfile, targetWebrootDir);
            
            return targetWebrootDir;
        }
        else {
            if(!new File(webroot).exists())
                throw new IOException("Webroot directory does not exists!");
            else
                return webroot;
        }
    }

    private static String createAbstractTempDir(String warfileName) throws IOException {
        File tmpFile = File.createTempFile("tmp", "tmp");
        File tmp = new File(tmpFile.getParent(), "undertow4jenkins_temp_" + warfileName);
        tmpFile.delete();
        
        return tmp.getAbsolutePath() + "/";
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                }
                else
                    f.delete();
            }
            directory.delete();
        }
    }
}
