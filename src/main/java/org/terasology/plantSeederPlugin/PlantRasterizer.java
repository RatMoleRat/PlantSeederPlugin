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

    /**
     * Places plants in the world.
     * <p>
     * Sugarcane spawns on sand near water, and the other plants spawn with less frequency and only on grass.
     * @param chunk
     * @param chunkRegion
     */
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
            //check if it's sand ( and next to water and not water above it), if it is, there's a change of placing sugarcane
            if (blockAtPos.equals(sand)) {
                boolean nextToWater = false;
                if (chunk.getBlock(ChunkMath.calcBlockPos(new Vector3i(position.x()+1, position.y(), position.z()))).equals(water)) {
                    nextToWater = true;
                } else if (chunk.getBlock(ChunkMath.calcBlockPos(new Vector3i(position.x()-1, position.y(), position.z()))).equals(water)) {
                    nextToWater = true;
                } else if (chunk.getBlock(ChunkMath.calcBlockPos(new Vector3i(position.x(), position.y(), position.z()+1))).equals(water)) {
                    nextToWater = true;
                } else if (chunk.getBlock(ChunkMath.calcBlockPos(new Vector3i(position.x(), position.y(), position.z()-1))).equals(water)) {
                    nextToWater = true;
                }
                if (nextToWater && !chunk.getBlock(ChunkMath.calcBlockPos(new Vector3i(position.x(), position.y()+1, position.z()))).equals(water)) {
                    if (random.nextInt(50)>45) {
                        placeBlocks(boundsOfObj, position, chunkRegion, chunk, sugarCane);
                    }
                }
            }
            //also a chance of placing another crop
            else if (blockAtPos.equals(grass)) {
                if (random.nextInt(300)>298) {
                    placeBlocks(boundsOfObj, position, chunkRegion, chunk, plants[random.nextInt(3)]);
                }
            }
        }
    }

    /**
     * Places blocks based on the set parameters
     * @param objBounds
     * @param pos
     * @param region
     * @param c
     */
    private void placeBlocks(AABB objBounds, Vector3i pos, Region region, CoreChunk c, Block toPlace) {
        BaseVector3i positionOfNew0 = ChunkMath.calcBlockPos(pos);
        BaseVector3i positionOfNew1 = positionOfNew0;
        ((Vector3i) positionOfNew1).y+=((int)objBounds.maxY()-(int)objBounds.minY());
        positionOfNew1 = ChunkMath.calcBlockPos((Vector3i)positionOfNew1);
        ((Vector3i) positionOfNew1).setX(positionOfNew0.x());
        ((Vector3i) positionOfNew1).setZ(positionOfNew0.z());
        region.getRegion().expandToContain(positionOfNew1);
        if (positionOfNew1.y() < 64) {
            c.setBlock(positionOfNew1,toPlace);
        }
    }
}
