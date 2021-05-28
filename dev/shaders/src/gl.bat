shaderc -f fs_cubes.sc -o out/fs_cubes.bin --type fragment --platform windows -p spirv --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 

shaderc -f vs_cubes.sc -o out/vs_cubes.bin --type vertex --platform windows -p spirv --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 