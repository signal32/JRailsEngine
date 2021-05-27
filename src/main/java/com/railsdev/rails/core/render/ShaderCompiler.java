package com.railsdev.rails.core.render;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ShaderCompiler {
    private static final String OUT_DIR = "dev/shaders/bin/";
    private static final String SHADERC_DIR = "dev/shaders/src/";

    public static void compile(String dir){
        System.out.println("Compiling shaders:");

        try {
            Stream<Path> path = Files.walk(Paths.get(dir));
            path = path.filter(Files::isRegularFile);
            path = path.filter(str -> str.toString().endsWith(".sc"));
            path = path.filter(str -> !str.toString().endsWith("def.sc"));
            path.forEach(str -> compile(str,OUT_DIR, "spirv"));
        }
        catch (Exception e){
            System.out.println("An error occurred converting shaders.");
        }
    }

    private static void compile(Path shader, String output, String platform){

        String shaderPath = shader.toString();
        String outputPath = output+shader.getFileName();
        String name = shader.getFileName().toString();
        String type = shader.getFileName().toString().startsWith("vs_")? "v" : "f";

        try{
            var process = new ProcessBuilder(
                    SHADERC_DIR + "shaderc",
                    "-f",shader.toString(),
                    "-o",OUT_DIR + platform + "/" + shader.getFileName().toString().substring(0,name.lastIndexOf('.')) + ".bin",
                    "--type", type,
                    "--platform", platform).start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            if (br.readLine() == null){
                System.out.println("Compiled " + shader.getFileName());
            }
            else {
                System.out.println("Compilation error: " + shader.getFileName().toString());
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }catch (Exception e){
            System.out.println("An error occurred converting shaders.");
        }
    }
}
