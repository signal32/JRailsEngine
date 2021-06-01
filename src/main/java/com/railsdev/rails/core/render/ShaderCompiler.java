package com.railsdev.rails.core.render;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ShaderCompiler {

    private static final Logger LOGGER = LogManager.getLogger(ShaderCompiler.class);

    //private static final String OUT_DIR = "dev/shaders/bin/";
    //private static final String SHADERC_DIR = "dev/shaders/src/";
    private static final String SHADERS = "dev/shaders/";
    private static final String BGFX_SRC = SHADERS + "bgfx/";
    private static final String BGFX_COMMON = SHADERS + "common/";
    private static final String SHADERC_DIR = SHADERS + "src/";
    private static final String OUT_DIR = SHADERS + "bin/";

    public static void compile(String dir, String profile){
        LOGGER.info("Compiling shaders for {}", profile);

        try {
            Stream<Path> path = Files.walk(Paths.get(dir));
            path = path.filter(Files::isRegularFile);
            path = path.filter(str -> str.toString().endsWith(".sc"));
            path = path.filter(str -> !str.toString().endsWith("def.sc"));
            path.forEach(str -> compile(str,OUT_DIR, profile));
        }
        catch (Exception e){
            System.out.println("An error occurred converting shaders.");
        }
    }

    private static void compile(Path shader, String output, String profile){

        String shaderPath = shader.toString();
        String outputPath = output+shader.getFileName();
        String name = shader.getFileName().toString();
        String type = shader.getFileName().toString().startsWith("vs_")? "vertex" : "fragment";
        String platform = profile.equals("spirv")? "linux" : "windows";
        boolean glsl = profile.equals("glsl");

        try{
            //var process
                    var x= new ProcessBuilder(
                    SHADERC_DIR + "shaderc",
                    "-f",shader.toString(),
                    "-o",OUT_DIR + profile + "/" + shader.getFileName().toString().substring(0,name.lastIndexOf('.')) + ".bin",
                    "--type", type,
                    "--platform", platform,
                    (glsl)?"":"--profile", (glsl)? "" : profile,
                    "-i", BGFX_SRC,
                    "-i", BGFX_COMMON + "shaderlib.sh",
                    "-i", BGFX_COMMON + "common.sh");

                    var cmd = x.command();

                    var process = x.start();


            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            if (br.readLine() == null){
                LOGGER.info("Compiled {}", shader::getFileName);
            }
            else {
                LOGGER.error("Shader compiler failed: {}", shader::getFileName);
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }catch (Exception e){
            System.out.println("An error occurred converting shaders.");
        }
    }
}
