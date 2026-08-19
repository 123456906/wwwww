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

    public float heightScale = 2.0f;
    public float waterLevel = 0.06f;

    private Block[][] ringTerrain = {
            {Blocks.water, Blocks.water, Blocks.sand, Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone},
            {Blocks.water, Blocks.sand, Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.darksand},
            {Blocks.sand, Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.darksand, Blocks.darksand},
            {Blocks.sand, Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.darksand, Blocks.darksand, Blocks.darkMetal},
            {Blocks.grass, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.darksand, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel6},
            {Blocks.stone, Blocks.stone, Blocks.darksand, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel6, Blocks.darkPanel6, Blocks.darkMetal},
            {Blocks.darksand, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel6, Blocks.darkPanel6, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel3},
            {Blocks.darkMetal, Blocks.darkPanel6, Blocks.darkPanel6, Blocks.darkMetal, Blocks.darkMetal, Blocks.darkPanel3, Blocks.darkPanel3, Blocks.metalFloorDamaged}
    };

    private Block[] decayDecor = {Blocks.metalFloorDamaged, Blocks.darkPanel3, Blocks.darkPanel4, Blocks.darkPanel5};

    @Override
    public float getHeight(Vec3 pos) {
        float radial = Math.abs(pos.y);
        float noise = Simplex.noise3d(seed, 4, 0.5f, 0.8f, pos.x * heightScale, pos.y * heightScale + 10, pos.z * heightScale);
        noise = noise * 0.5f + 0.5f;
        float h = Mathf.lerp(0.02f, noise * 0.8f + 0.1f, Mathf.clamp((radial - 0.1f) * 4f));
        return Math.max(h, waterLevel);
    }

    @Override
    public void getColor(Vec3 pos, Color out) {
        Block block = getBlock(pos);
        out.set(block.mapColor);
        out.mul(0.9f);
        out.a = 1f;
    }

    private Block getBlock(Vec3 pos) {
        float h = rawHeight(pos);
        float radial = Mathf.clamp(Math.abs(pos.y) * 1.8f);
        float heightVal = Mathf.clamp(h * 1.2f);
        int row = Mathf.clamp((int)(radial * (ringTerrain.length - 1)), 0, ringTerrain.length - 1);
        int col = Mathf.clamp((int)(heightVal * (ringTerrain[0].length - 1)), 0, ringTerrain[0].length - 1);
        Block block = ringTerrain[row][col];
        if (block == Blocks.darkMetal || block == Blocks.darkPanel6) {
            if (rand.chance(0.15)) {
                block = decayDecor[rand.nextInt(decayDecor.length)];
            }
        }
        return block;
    }

    private float rawHeight(Vec3 pos) {
        float radial = Math.abs(pos.y);
        float noise = Simplex.noise3d(seed, 4, 0.5f, 0.8f, pos.x * heightScale, pos.y * heightScale + 10, pos.z * heightScale);
        noise = noise * 0.5f + 0.5f;
        return Mathf.lerp(0.02f, noise * 0.8f + 0.1f, Mathf.clamp((radial - 0.1f) * 4f));
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
            if (rand.chance(0.04)) {
                tile.block = Blocks.shrubs;
            }
            if (rand.chance(0.02)) {
                tile.block = Blocks.pine;
            }
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
                    if (rand.chance(0.03)) {
                        // ★ 修正：原 darkMetalWall 不存在，改为 darkMetal（或可继续使用地板，但这里作为装饰可保留）
                        tile.setBlock(Blocks.darkMetal);
                    }
                }
                if (floor == Blocks.grass || floor == Blocks.sand || floor == Blocks.stone) {
                    if (rand.chance(0.02)) {
                        tile.setBlock(Blocks.sporeCluster);
                    }
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
