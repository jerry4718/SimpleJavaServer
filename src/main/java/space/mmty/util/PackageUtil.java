package space.mmty.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageUtil {
    private static void packetScanner(File curFile, String packName, Consumer<Class<?>> dealClass) {
        if (!curFile.isDirectory()) {
            return;
        }
        File[] files = curFile.listFiles();
        if (files == null || files.length < 1) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                String fileName = file.getName().replace(".class", "");
                String className = packName + "." + fileName;
                try {
                    Class<?> klass = Class.forName(className);
                    dealClass.accept(klass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (file.isDirectory()) {
                packetScanner(file, packName + "." + file.getName(), dealClass);
            }
        }
    }

    private static void scanJarPacket(URL url, Consumer<Class<?>> dealClass) throws IOException {
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarName = jarEntry.getName();
            if (jarEntry.isDirectory() || !jarName.endsWith(".class")) {
                continue;
            }
            String className = jarName.replace(".class", "").replaceAll("/", ".");
            try {
                Class<?> klass = Class.forName(className);
                if (klass.isAnnotation()
                        || klass.isEnum()
                        || klass.isInterface()
                        || klass.isPrimitive()) {
                    continue;
                }
                dealClass.accept(klass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void scanPacket(String packetName, Consumer<Class<?>> dealClass) {
        String packetPath = packetName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(packetPath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar")) {
                    scanJarPacket(url, dealClass);
                } else {
                    File file = new File(url.toURI());
                    if (!file.exists()) {
                        continue;
                    }
                    packetScanner(file, packetName, dealClass);
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
