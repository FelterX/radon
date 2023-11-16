package radon.engine.core;

import radon.engine.logging.Log;
import radon.engine.resource.ManagedResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class RadonFiles {

    private static RadonJarFileSystem jarFileSystem;

    public static URL getURL(String path) {
       URL url = RadonFiles.class.getResource(normalize(path));
       if(url == null) {
           Log.error("Failed to get resource " + path);
       }
       return url;
    }

    public static URI getURI(String path) {
        try {
            URL url = getURL(path);
            return url == null ? null : url.toURI();
        } catch (URISyntaxException | NullPointerException e) {
            Log.error("Failed to create URI from URL", e);
        }
        return null;
    }

    public static File getFile(String filename) {
        Path path = getPath(filename);
        return path == null ? null : path.toFile();
    }

    public static Path getPath(String path) {

        URI uri = getURI(path);

        return uri == null ? null : getPath(uri);
    }

    private static Path getPath(URI uri) {

        Path path;

        if(uri.getScheme().contains("jar")) {

            String[] uriParts = uri.toString().split("!");

            if(jarFileSystem == null) {
                jarFileSystem = new RadonJarFileSystem(uriParts[0]);
            }

            path = jarFileSystem.getPath(uriParts[1]);

        } else {
            path = Paths.get(uri);
        }

        return path;
    }

    public static InputStream getInputStream(String path) {
        return RadonFiles.class.getResourceAsStream(normalize(path));
    }

    private static String normalize(String path) {
        return path.charAt(0) == File.pathSeparatorChar ? path : '/' + path;
    }

    private RadonFiles() {}

    private static class RadonJarFileSystem extends ManagedResource {

        private final FileSystem fileSystem;

        public RadonJarFileSystem(String root) {
            fileSystem = createFileSystem(root);
        }

        public Path getPath(String first, String... others) {
            return fileSystem.getPath(first, others).toAbsolutePath();
        }

        @Override
        protected void free() {
            try {
                fileSystem.close();
            } catch (IOException e) {
                Log.error("Failed to close file system " + fileSystem, e);
            }
        }

        private FileSystem createFileSystem(String root) {
            FileSystem fileSystem = null;
            try {
                Map<String, String> environment = new HashMap<>();
                environment.put("create", "true");
                environment.put("encoding", "UTF-8");
                fileSystem = FileSystems.newFileSystem(URI.create(root), environment, ClassLoader.getSystemClassLoader());
            } catch (Exception e) {
                Log.error("Failed to create JAR FileSystem", e);
            }
            return fileSystem;
        }
    }
}
