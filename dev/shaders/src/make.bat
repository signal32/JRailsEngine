shaderc -f fs_cubes.sc -o ../bin/spirv/fs_cubes.bin --type fragment --platform windows -p spirv --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 

shaderc -f vs_cubes.sc -o ../bin/spirv/vs_cubes.bin --type vertex --platform windows -p spirv --depends -i ../bgfx -i ../common/shaderlib.sh i ../common/common.sh 