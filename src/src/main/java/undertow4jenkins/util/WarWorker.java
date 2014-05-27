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

/**
 * Utility class to processing war archives, creating classloader, etc.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class WarWorker {

    /**
     * Created ClassLoader for all libraries and classes in extracted war archive.
     * 
     * Loads also libraries from additional commonLib directory
     * 
     * @param commonLib Path to additional directory with libraries
     * @param warDir Path to directory with extracted war archive
     * @param parentClassLoader Parent ClassLoader
     * @return New instance of classloader with loaded all libraries from specified sources
     * @throws IOException Thrown if some directory does not exists or some other IO violation occurs
     */
    public static ClassLoader createJarsClassloader(
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

    /**
     * Extras all files from war archive to specified directory
     * @param warfilePath Path to war file
     * @param targetDir Target directory for extraction of war file
     * @throws IOException Thrown if some directory or war file does not exists or some other IO violation occurs
     */
    public static void extractFilesFromWar(String warfilePath, String targetDir)
            throws IOException {
        byte buffer[] = new byte[8192];

        JarFile warArchive = new JarFile(warfilePath);
        for (Enumeration<JarEntry> e = warArchive.entries(); e.hasMoreElements();) {
            JarEntry element = (JarEntry) e.nextElement();
            if (element.isDirectory())
                continue;

            File outFile = new File(targetDir + element.getName());

            if (!outFile.exists()) {
                copyFile(buffer, warArchive, element, outFile);
            }
        }
        warArchive.close();
    }

    /**
     * Copies file from one destination to other
     * @param buffer Buffer to store copied bytes of file
     * @param warArchive War archive instance
     * @param element Element of war archive
     * @param outFile Abstract target file
     * @throws IOException Thrown if some IO violation occurs
     * @throws FileNotFoundException Thrown if some specified file does not exists
     */
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

    /**
     * Prepares root directory for web application
     * One of parameters has to be specified
     * 
     * 
     * @param warfile Path to warfile if specified. May be null.
     * @param webroot Path to webroot if specified. May be null.
     * @return Path to webroot directory
     * @throws IOException Thrown if some IO violation occurs or specified file does not exists
     */
    public static String createWebApplicationRoot(String warfile, String webroot) 
            throws IOException {
        if (warfile != null) {
            File warfileFile = new File(warfile);
            if(! warfileFile.isFile() )
                throw new IOException("Specified warfile does not exists!");
            
            String targetWebrootDir;
            if(webroot == null)
                targetWebrootDir = createAbstractTempDir(warfileFile.getName());
            else   {
                targetWebrootDir = unifyWebroot(webroot);
                deleteDirectory(new File(targetWebrootDir));
            }
            
            extractFilesFromWar(warfile, targetWebrootDir);
            
            return targetWebrootDir;
        }
        else {
            if(!new File(webroot).exists())
                throw new IOException("Webroot directory does not exists!");
            else {
                //Application expects directory ending with /
                return unifyWebroot(webroot);
            }
        }
    }
    
    /**
     * Unify webroot path to end with slash
     * @param webroot Path to webroot
     * @return Unified webroot path
     */
    private static String unifyWebroot(String webroot) {
        if(webroot.endsWith("/"))
            return webroot;
        else
            return webroot + "/";
    }

    /**
     * Creates temporary directory  
     * 
     * @param warfileName Name of war file
     * @return Path to created directory
     * @throws IOException Thrown if some IO violation occurs
     */
    private static String createAbstractTempDir(String warfileName) throws IOException {
        File tmpFile = File.createTempFile("tmp", "tmp");
        File tmp = new File(tmpFile.getParent(), "undertow4jenkins_temp_" + warfileName);
        tmpFile.delete();
        
        return tmp.getAbsolutePath() + "/";
    }

    /**
     * Deletes whole directory recursively
     * 
     * @param directory Path to directory
     */
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
