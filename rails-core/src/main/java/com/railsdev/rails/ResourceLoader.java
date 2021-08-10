package com.railsdev.rails;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.*;

public class ResourceLoader {

    public static ByteBuffer loadResource(String resourcePath) throws IOException {
        Path path = Paths.get(resourcePath);
        ByteBuffer resource = memAlloc((int) Files.size(path));

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(resourcePath))){
            int b;
            do {
                b = bis.read();
                if (b != -1) {
                    resource.put((byte) b);
                }
            } while (b != -1);
        }

        resource.flip();
        return resource;
    }
}
