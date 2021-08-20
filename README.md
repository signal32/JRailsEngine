# Rails
[![Java CI with Maven](https://github.com/signal32/JRailsEngine/actions/workflows/maven.yml/badge.svg)](https://github.com/signal32/JRailsEngine/actions/workflows/maven.yml)

Java based Game Engine / Framework.

My first foray into game engine design and implementation. Nowhere near complete let alone production ready but could form a jumping off point for your own projects. Although RailsEngine is now being improved upon and rewritten in C#, pull requests will still be considered. New version will be made public when feature parity is reached.

### Key Features
- Vulkan, Metal, DirectX and OpenGL rendering backends via BGFX.
- Floating Origin implementation for large virtual world support

## Notes

### Shader Compilation
Use provided shaderc.exe for compilation on Windows. Must use exact version of `shaderc`, `shaderlib.sh`, `common.sh`, `bgfx_shader.sh`, `bgfx_compute.sh` as from [LWJGL BGFX Branch](https://github.com/LWJGL-CI/bgfx). Examples:

#### OpenGL (glsl)
    shaderc -f fs_cubes.sc -o out/fs_cubes.bin --type fragment --platform windows --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 
    shaderc -f vs_cubes.sc -o out/vs_cubes.bin --type vertex --platform windows --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 

#### Vulkan (spirv)
Note platform is specified as linux regardless of target or compilation environment.

    shaderc -f fs_cubes.sc -o ../bin/spirv/fs_cubes.bin --type fragment --platform linux -p spirv --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 
    shaderc -f vs_cubes.sc -o ../bin/spirv/vs_cubes.bin --type vertex --platform linux -p spirv --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 
