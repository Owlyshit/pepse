package pepse.util;

import pepse.world.Terrain;
/**
    * Contains helper functions that are used throughout the game.
 **/
public abstract class HelpingFunctions {
    /**
     * The height of the ground at the x-coordinate 0.
     */
    public final static float groundHeightAtX0 = Terrain.getGroundHeightAtX0();
    /**
        * Aligns a given height to the nearest multiple of a block size.
        *
        * @param groundHeight The height to align.
        * @param blockSize    The size of the block to align to.
        * @return The aligned height.
     */
    public static int alignHeight(float groundHeight, int blockSize) {
        return (int) Math.floor(groundHeight / blockSize) * blockSize;
    }
    /**
        * Calculates the amount of triangles in a triangle strip with a given number of vertices.
        *
        * @param x The number of vertices in the triangle strip.
        * @return The amount of triangles in the triangle strip.
     */
    public static int getTriangleAmount(int x) {
        int sum = 0;
        for(int i = 0 ; i < x+1 ; i ++ ){
            sum+=i;
        }
        return sum;
    }
}