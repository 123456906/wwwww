package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.game.Schematics;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.Env;

import static mindustry.Vars.*;

public class RingPlanetGenerator extends PlanetGenerator {

    public float ringInnerRadius = 0.30f;
    public float ringOuterRadius = 0.85f;
    public float flatHeight = 0.04f;
    public float edgeHeight = 0.25f;

    private Block[][] ringTerrain = {
            // 内圈（平坦低地）
            {Blocks.water, Blocks.water, Blocks.sand, Blocks.sand, Blocks.grass, Blocks.grass},
            {Blocks.water, Blocks.sand, Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone},
            {Blocks.sand, Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone},
            {Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.darksand},
            {Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.darksand, Blocks.darkMetal},
            {Blocks.stone, Blocks.stone, Blocks.darksand, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel6},
            {Blocks.darksand, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel6, Blocks.darkPanel6, Blocks.darkMetal},
            {Blocks.darkMetal, Blocks.darkPanel6, Blocks.darkPanel6, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel3},
            {Blocks.darkPanel6, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel3, Blocks.darkPanel3, Blocks.metalFloorDamaged}
    };

    @Override
    public float getHeight(Vec3 pos) {
        float radial = (float)Math.sqrt(pos.x * pos.x + pos.z * pos.z);
        if (radial < ringInnerRadius) {
            return flatHeight;
        } else if (radial > ringOuterRadius) {
            return flatHeight;
        } else {
            float t = (radial - ringInnerRadius) / (ringOuterRadius - ringInnerRadius);
            float edge = Mathf.pow(t, 0.25f);
            float noise = Simplex.noise3d(seed, 2, 0.5f, 0.5f, pos.x, pos.y, pos.z) * 0.015f;
            return flatHeight + edge * edgeHeight + noise;
        }
    }

    @Override
    public void getColor(Vec3 pos, Color out) {
        Block block = getBlock(pos);
        out.set(block.mapColor);
        out.mul(0.9f);
        out.a = 1f;
    }

    private Block getBlock(Vec3 pos) {
        float h = getHeight(pos);
        float radial = (float)Math.sqrt(pos.x * pos.x + pos.z * pos.z);
        float rowF = Mathf.clamp((radial - 0.1f) / 0.9f) * (ringTerrain.length - 1);
        int row = Mathf.clamp((int)rowF, 0, ringTerrain.length - 1);
        int col = Mathf.clamp((int)(h * (ringTerrain[0].length - 1) * 2f), 0, ringTerrain[0].length - 1);
        return ringTerrain[row][col];
    }

    @Override
    public void genTile(Vec3 pos, TileGen tile) {
        Block floor = getBlock(pos);
        tile.floor = floor;
        Block wall = floor.asFloor().wall;
        if (wall != null && wall != Blocks.air) {
            tile.block = wall;
            if (rand.chance(0.25)) {
                tile.block = Blocks.air;
            }
        } else {
            tile.block = Blocks.air;
        }
        if (floor == Blocks.sand || floor == Blocks.grass || floor == Blocks.stone) {
            if (rand.chance(0.04)) tile.block = Blocks.shrubs;
            if (rand.chance(0.02)) tile.block = Blocks.pine;
        }
    }

    @Override
    public void postGenerate(Tiles tiles) {
        int w = tiles.width, h = tiles.height;
        int cx = w / 2, cy = h / 2;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                Floor floor = tile.floor();
                if (floor == Blocks.darkMetal || floor == Blocks.darkPanel6 || floor == Blocks.metalFloorDamaged) {
                    if (rand.chance(0.03)) tile.setBlock(Blocks.darkMetal);
                }
                if (floor == Blocks.grass || floor == Blocks.sand || floor == Blocks.stone) {
                    if (rand.chance(0.02)) tile.setBlock(Blocks.sporeCluster);
                }
            }
        }

        int coreX = cx, coreY = cy;
        boolean found = false;
        for (int r = 0; r < 30 && !found; r++) {
            for (int dx = -r; dx <= r && !found; dx++) {
                for (int dy = -r; dy <= r && !found; dy++) {
                    int tx = cx + dx, ty = cy + dy;
                    if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
                        Tile tile = tiles.getn(tx, ty);
                        if (tile != null && tile.floor() != Blocks.water && tile.floor() != Blocks.deepwater) {
                            coreX = tx;
                            coreY = ty;
                            found = true;
                            break;
                        }
                    }
                }
            }
        }

        Schematics.placeLaunchLoadout(coreX, coreY);
        state.rules.waves = true;
        state.rules.env = Env.terrestrial;
        state.rules.winWave = 10;
        state.rules.placeRangeCheck = true;
    }

    @Override
    public float getSizeScl() {
        return 2500f;
    }
}