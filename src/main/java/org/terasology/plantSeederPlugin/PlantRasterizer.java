package org.terasology.plantSeederPlugin;

//TODO: test more, play with random numbers to find appropriate amount
//TODO: see if keeping it from generating underwater works

import org.terasology.math.AABB;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Random;

@RegisterPlugin
public class PlantRasterizer implements WorldRasterizerPlugin {
    private Block wildCarrot;
    private Block squash;
    private Block turnip;
    private Block sugarCane;
    private Block sand;
    private Block grass;
    private Block water;

    private Random random;

    @Override
    public void initialize() {
        wildCarrot = CoreRegistry.get(BlockManager.class).getBlock("PlantPack:WildCarrot5");
        squash = CoreRegistry.get(BlockManager.class).getBlock("PlantPack:Squash5");
        turnip = CoreRegistry.get(BlockManager.class).getBlock("PlantPack:Turnip5");
        sugarCane = CoreRegistry.get(BlockManager.class).getBlock("PlantPack:SugarCane5");
        sand = CoreRegistry.get(BlockManager.class).getBlock("Core:Sand");
        grass = CoreRegistry.get(BlockManager.class).getBlock("Core:Grass");
        water = CoreRegistry.get(BlockManager.class).getBlock("Core:Water");

        random = new Random();
    }

    //places plants in appropriate positions
    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        SurfaceHeightFacet surfaceHeightFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);

        //prepares array that will be selected from later
        Block[] plants = new Block[3];
        plants[0] = wildCarrot;
        plants[1] = squash;
        plants[2] = turnip;

        for (Vector3i position : chunkRegion.getRegion()) {
            AABB boundsOfObj = chunk.getBlock(ChunkMath.calcBlockPos(position)).getBounds(position);
            float surfaceHeight = surfaceHeightFacet.getWorld(position.x, position.z);
            Block blockAtPos = chunk.getBlock(ChunkMath.calcBlockPos(position));
            //check if it's sand, if it is, there's a change of placing sugarcane
            if (blockAtPos.equals(sand) && !chunk.getBlock(ChunkMath.calcBlockPos(new Vector3i(position.x(), position.y()+1, position.z()))).equals(water))) {
                if (random.nextInt(50)>45) {
                    BaseVector3i positionOfNew = ChunkMath.calcBlockPos(position);
                    ((Vector3i) positionOfNew).y+=((int)boundsOfObj.maxY()-(int)boundsOfObj.minY());
                    chunkRegion.getRegion().expandToContain(positionOfNew);
                    if (positionOfNew.y() < 64) {
                        chunk.setBlock(positionOfNew,sugarCane);
                    }
                }
            }
            //also a chance of placing another crop
            else if (blockAtPos.equals(grass)) {
                if (random.nextInt(100)>98) {
                    BaseVector3i positionOfNew = ChunkMath.calcBlockPos(position);
                    ((Vector3i) positionOfNew).y+=((int)boundsOfObj.maxY()-(int)boundsOfObj.minY());
                    chunkRegion.getRegion().expandToContain(positionOfNew);
                    if (positionOfNew.y() < 64) {
                        chunk.setBlock(positionOfNew, plants[random.nextInt(3)]);
                    }
                }
            }
        }
    }
}
