package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Team;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import arc.math.Rand;
import arc.struct.FloatSeq;
import arc.util.Log;
import mindustry.game.Schematics;
import mindustry.maps.generators.BaseGenerator;
import mindustry.game.Waves;
import mindustry.type.Sector;
import mindustry.world.Tiles;
import mindustry.world.meta.Env;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.*;

public class FusionPlanetGenerator extends PlanetGenerator {

    private static final Block[][] serpuloArr = {
            {Blocks.water, Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksandTaintedWater, Blocks.stone, Blocks.stone},
            {Blocks.water, Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksandTaintedWater, Blocks.stone, Blocks.stone, Blocks.stone},
            {Blocks.water, Blocks.darksandWater, Blocks.darksand, Blocks.sand, Blocks.salt, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksandTaintedWater, Blocks.stone, Blocks.stone, Blocks.stone},
            {Blocks.water, Blocks.sandWater, Blocks.sand, Blocks.salt, Blocks.salt, Blocks.salt, Blocks.sand, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.snow, Blocks.iceSnow, Blocks.ice},
            {Blocks.deepwater, Blocks.water, Blocks.sandWater, Blocks.sand, Blocks.salt, Blocks.sand, Blocks.sand, Blocks.basalt, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.ice},
            {Blocks.deepwater, Blocks.water, Blocks.sandWater, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.moss, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.snow, Blocks.ice},
            {Blocks.deepwater, Blocks.sandWater, Blocks.sand, Blocks.sand, Blocks.moss, Blocks.moss, Blocks.snow, Blocks.basalt, Blocks.basalt, Blocks.basalt, Blocks.ice, Blocks.snow, Blocks.ice},
            {Blocks.deepTaintedWater, Blocks.darksandTaintedWater, Blocks.darksand, Blocks.darksand, Blocks.basalt, Blocks.moss, Blocks.basalt, Blocks.hotrock, Blocks.basalt, Blocks.ice, Blocks.snow, Blocks.ice, Blocks.ice},
            {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.moss, Blocks.moss, Blocks.snow, Blocks.basalt, Blocks.basalt, Blocks.ice, Blocks.snow, Blocks.ice, Blocks.ice},
            {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.moss, Blocks.ice, Blocks.ice, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice},
            {Blocks.deepTaintedWater, Blocks.darksandTaintedWater, Blocks.darksand, Blocks.moss, Blocks.moss, Blocks.ice, Blocks.ice, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice},
            {Blocks.taintedWater, Blocks.darksandTaintedWater, Blocks.darksand, Blocks.moss, Blocks.moss, Blocks.moss, Blocks.iceSnow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice},
            {Blocks.darksandWater, Blocks.darksand, Blocks.snow, Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice}
    };

    private static final Block[] erekirTerrain = {
            Blocks.regolith,
            Blocks.regolith,
            Blocks.regolith,
            Blocks.regolith,
            Blocks.yellowStone,
            Blocks.rhyolite,
            Blocks.rhyolite,
            Blocks.carbonStone
    };

    private final ObjectMap<Block, Block> decMap = new ObjectMap<>();

    public float heightScl = 0.9f;
    public int octaves = 8;
    public float persistence = 0.7f;
    public float heightPow = 4f;
    public float heightMult = 0.8f;
    public float scl = 5f;

    public float arkThresh = 0.28f;
    public float arkScl = 0.83f;
    public int arkSeed = 7;
    public int arkOct = 2;
    public float redThresh = 3.1f;
    public float noArkThresh = 0.3f;
    public int crystalSeed = 8;
    public int crystalOct = 2;
    public float crystalScl = 0.9f;
    public float crystalMag = 0.3f;

    public float iceHeightScale = 1.0f;
    public float darksandMin = -0.4f;
    public float darksandMax = 0.2f;
    public float erekirHeightOffset = 0.1f;
    public float erekirHeightScale = 1.2f;

    public int mixSeed = 9999;
    public int mixOctaves = 4;
    public float mixFreq = 0.25f;
    public float mixThreshold = 0.7f;
    public Vec3 basePos = new Vec3(0.9341721f, 0f, 0.3568221f);

    private final BaseGenerator basegen = new BaseGenerator();

    public FusionPlanetGenerator() {
        decMap.put(Blocks.moss, Blocks.sporeCluster);
        decMap.put(Blocks.taintedWater, Blocks.water);
        decMap.put(Blocks.darksandTaintedWater, Blocks.darksandWater);
    }

    private float rawHeight(Vec3 pos) {
        float h = Simplex.noise3d(seed, octaves, persistence, 1f / heightScl,
                10f + pos.x, 10f + pos.y, 10f + pos.z);
        float val = (h * 1.4f) * 0.5f + 0.5f;
        return Mathf.clamp(val);
    }

    private float rawTemp(Vec3 pos) {
        float temp = pos.dst(0, 0, 1f) * 2.2f
                - Simplex.noise3d(seed, 8, 0.54f, 1.4f, 10f + pos.x, 10f + pos.y, 10f + pos.z) * 2.9f;
        return temp - 0.1f;
    }

    private Block getSerpuloBlock(Vec3 pos) {
        float h = rawHeight(pos);
        float px = pos.x * scl;
        float py = pos.y * scl;
        float pz = pos.z * scl;
        float rad = scl;

        float lat = Mathf.clamp(Math.abs(py * 2f) / rad);
        float tnoise = Simplex.noise3d(seed, 7, 0.56f, 1f/3f, px, py + 999f - 0.1f, pz);
        lat = Mathf.lerp(lat, tnoise, 0.5f);

        float height = (float)Math.pow(h, heightPow) * heightMult;
        height = Mathf.clamp(height);

        int row = Mathf.clamp((int)(lat * serpuloArr.length), 0, serpuloArr.length - 1);
        int col = Mathf.clamp((int)(height * serpuloArr[0].length), 0, serpuloArr[0].length - 1);
        Block block = serpuloArr[row][col];

        if (height >= darksandMin && height <= darksandMax) {
            if (block == Blocks.grass || block == Blocks.sand || block == Blocks.dirt || block == Blocks.mud) {
                return Blocks.darksand;
            }
        }

        if (block == Blocks.snow || block == Blocks.ice || block == Blocks.iceSnow) {
            float iceNoise = Ridged.noise3d(seed + 999, pos.x * 1.2f, pos.y * 1.2f + 50f, pos.z * 1.2f, 2, 0.6f);
            float iceFactor = iceNoise * 0.5f + 0.5f;
            if (lat > 0.8f && height > 0.55f && iceFactor > 0.6f) {
                return block;
            } else {
                return (height > 0.3f) ? Blocks.darksand : Blocks.grass;
            }
        }
        return block;
    }

    private Block getErekirBlock(Vec3 pos) {
        float h = rawHeight(pos);
        float ice = rawTemp(pos);

        float height = (float)Math.pow(h, heightPow) * heightMult;
        float normHeight = Mathf.clamp((height - 0.05f) / 0.8f);
        float adjustedHeight = Mathf.clamp(normHeight * erekirHeightScale + erekirHeightOffset);

        float hNoise = Simplex.noise3d(seed + 888, 3, 0.5f, 0.3f, pos.x, pos.y, pos.z) * 0.12f;
        adjustedHeight = Mathf.clamp(adjustedHeight + hNoise);

        int idx = Mathf.clamp((int)(adjustedHeight * erekirTerrain.length), 0, erekirTerrain.length - 1);
        Block result = erekirTerrain[idx];

        float replaceNoise = Simplex.noise3d(seed + 789, 3, 0.5f, 0.2f, pos.x, pos.y, pos.z);
        if (replaceNoise > 0.3f && result == Blocks.regolith) {
            result = (replaceNoise > 0.5f) ? Blocks.yellowStone : Blocks.rhyolite;
        }

        if (ice < 0.3f + Math.abs(Ridged.noise3d(seed + crystalSeed, pos.x + 4f, pos.y + 8f, pos.z + 1f, crystalOct, crystalScl)) * crystalMag) {
            return Blocks.crystallineStone;
        }
        if (ice < 0.6f) {
            if (result == Blocks.rhyolite || result == Blocks.yellowStone || result == Blocks.regolith) {
                result = Blocks.carbonStone;
            }
        }
        if (ice < redThresh - noArkThresh && Ridged.noise3d(seed + arkSeed, pos.x + 2f, pos.y + 8f, pos.z + 1f, arkOct, arkScl) > arkThresh) {
            result = Blocks.beryllicStone;
        }
        if (ice > redThresh) {
            result = Blocks.redStone;
        } else if (ice > redThresh - 0.4f) {
            result = Blocks.regolith;
        }
        return result;
    }

    public Block getBlock(Vec3 pos) {
        float mixNoise = Simplex.noise3d(mixSeed, mixOctaves, 0.5f, mixFreq, pos.x * 2f + 10f, pos.y * 2f + 20f, pos.z * 2f);
        float mixVal = mixNoise * 0.5f + 0.5f;
        if (mixVal > mixThreshold) {
            return getSerpuloBlock(pos);
        } else {
            return getErekirBlock(pos);
        }
    }

    @Override
    public float getHeight(Vec3 pos) {
        float h = rawHeight(pos);
        float baseHeight = (float)Math.pow(h, heightPow) * heightMult;
        Block block = getBlock(pos);
        if (block == Blocks.snow || block == Blocks.ice || block == Blocks.iceSnow) {
            return baseHeight * iceHeightScale;
        }
        if (block == Blocks.carbonStone) {
            return baseHeight * 0.33f;
        }
        return baseHeight;
    }

    @Override
    public void getColor(Vec3 pos, Color out) {
        Block block = getBlock(pos);
        out.set(block.mapColor);
        out.a = 1f;
    }

    @Override
    public void genTile(Vec3 pos, TileGen tile) {
        Block floor = getBlock(pos);
        tile.floor = floor;
        Block wall = floor.asFloor().wall;
        if (wall != null && wall != Blocks.air) {
            tile.block = wall;
            if (Ridged.noise3d(seed + 1, pos.x, pos.y, pos.z, 2, 10f) > 0.1f) {
                tile.block = Blocks.air;
            }
        } else {
            tile.block = Blocks.air;
        }
    }

    protected float noise(float x, float y, double octaves, double falloff, double scl, double mag) {
        Vec3 v = sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }

    @Override
    public void postGenerate(Tiles tiles) {
        if (tiles == null) return;
        int w = tiles.width, h = tiles.height;
        int cx = w / 2, cy = h / 2;
        float difficulty = sector != null ? sector.threat : 0.5f;

        for (int dx = -15; dx <= 15; dx++) {
            for (int dy = -15; dy <= 15; dy++) {
                if (dx * dx + dy * dy > 15 * 15) continue;
                int tx = cx + dx, ty = cy + dy;
                if (tx < 0 || tx >= w || ty < 0 || ty >= h) continue;
                Tile tile = tiles.getn(tx, ty);
                if (tile != null && tile.block() != Blocks.air) {
                    tile.setBlock(Blocks.air);
                }
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (x < 5 || x >= w - 5 || y < 5 || y >= h - 5) continue;
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                Floor floor = tile.floor();
                if (floor == null) continue;
                if (tile.overlay() != Blocks.air || tile.block() != Blocks.air) continue;
                if (floor == Blocks.grass || floor == Blocks.dirt || floor == Blocks.mud) {
                    if (rand.chance(0.005)) tile.setFloor(Blocks.shale.asFloor());
                } else if (floor == Blocks.water || floor == Blocks.darksandWater) {
                    if (rand.chance(0.005)) tile.setFloor(Blocks.taintedWater.asFloor());
                }
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (x >= 5 && x < w - 5 && y >= 5 && y < h - 5) continue;
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                if (tile.floor() == null || tile.floor().isLiquid) continue;
                if (tile.overlay() != Blocks.air) continue;
                float r = rand.nextFloat();
                if (r < 0.33f) tile.setOverlay(Blocks.oreBeryllium);
                else if (r < 0.66f) tile.setOverlay(Blocks.oreLead);
                else tile.setOverlay(Blocks.oreCopper);
            }
        }

        float poles = 0;
        if (sector != null) {
            poles = Math.abs(sector.tile.v.y);
        }
        float nmag = 0.5f;
        float scl = 1.0f;
        float addscl = 1.3f;

        Seq<Block> ores = Seq.with(Blocks.oreCopper, Blocks.oreLead);
        if (Simplex.noise3d(baseSeed, 2, 0.5f, scl,
                sector.tile.v.x, sector.tile.v.y, sector.tile.v.z) * nmag + poles > 0.45f * addscl) {
            ores.add(Blocks.oreCoal);
        }
        if (Simplex.noise3d(baseSeed, 2, 0.5f, scl,
                sector.tile.v.x + 1f, sector.tile.v.y, sector.tile.v.z) * nmag + poles > 0.5f * addscl) {
            ores.add(Blocks.oreTitanium);
        }
        if (Simplex.noise3d(baseSeed, 2, 0.5f, scl,
                sector.tile.v.x + 2f, sector.tile.v.y, sector.tile.v.z) * nmag + poles > 0.88f * addscl) {
            ores.add(Blocks.oreThorium);
        }
        if (rand.chance(0.25)) {
            ores.add(Blocks.oreScrap);
        }
        ores.add(Blocks.oreBeryllium);

        FloatSeq frequencies = new FloatSeq();
        for (int i = 0; i < ores.size; i++) {
            frequencies.add(rand.random(-0.1f, 0.01f) - i * 0.01f + poles * 0.04f);
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                Floor floor = tile.floor();
                if (floor == null || !floor.hasSurface()) continue;
                if (Math.abs(x - cx) <= 5 && Math.abs(y - cy) <= 5) continue;

                int offsetX = x - 4, offsetY = y + 23;
                for (int i = ores.size - 1; i >= 0; i--) {
                    Block entry = ores.get(i);
                    float freq = frequencies.get(i);
                    float cond1 = Math.abs(0.5f - noise(offsetX, offsetY + i * 999, 2, 0.7f, 40 + i * 2f, 1f));
                    float cond2 = Math.abs(0.5f - noise(offsetX, offsetY - i * 999, 1, 1f, 30 + i * 4f, 1f));
                    if (cond1 > 0.22f + i * 0.01f && cond2 > 0.37f + freq) {
                        tile.setOverlay(entry);
                        if (entry == Blocks.oreScrap && rand.chance(0.33f)) {
                            tile.setFloor(Blocks.metalFloorDamaged.asFloor());
                        }
                        break;
                    }
                }
            }
        }

        int metalSeed = this.seed + 3;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                if (tile.block() != Blocks.air) continue;
                if (tile.floor() == null || !tile.floor().hasSurface()) continue;
                if (tile.overlay() != Blocks.air) continue;
                if (Mathf.within(x, y, cx, cy, 20)) continue;

                Vec3 pos = sector.rect.project((float)x / w, (float)y / h);
                float vx = pos.x, vy = pos.y, vz = pos.z;

                if (pos.dst(sector.rect.center) < 0.65f * sector.rect.radius) {
                    float dst = 999f;
                    for (Sector sec : Planets.serpulo.sectors) {
                        if (sec != null && sec.hasEnemyBase()) {
                            float d = (float)Math.sqrt(
                                    (vx - sec.tile.v.x) * (vx - sec.tile.v.x) +
                                            (vy - sec.tile.v.y) * (vy - sec.tile.v.y) +
                                            (vz - sec.tile.v.z) * (vz - sec.tile.v.z)
                            );
                            if (d < dst) dst = d;
                        }
                    }

                    float freq = 0.05f, freq2 = 0.07f;
                    float baseDst = (float)Math.sqrt(
                            (vx - basePos.x) * (vx - basePos.x) +
                                    (vy - basePos.y) * (vy - basePos.y) +
                                    (vz - basePos.z) * (vz - basePos.z)
                    );
                    float metalNoise = Simplex.noise3d(metalSeed, 3, 0.4f, 5.5f, vx, vy + 200f, vz) * 0.015f;
                    int stripe = ((baseDst + 0.0f) % freq < freq / 2f) ? 1 : 0;
                    float cond = dst * 0.85f + metalNoise + stripe * 0.07f;

                    if (cond < 0.15f) {
                        if ((baseDst + 0.01f) % freq2 < freq2 * 0.65f) {
                            tile.setFloor(Blocks.metalFloor.asFloor());
                        } else {
                            tile.setFloor(Blocks.darkPanel6.asFloor());
                        }
                    }
                }
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                if (tile.block() != Blocks.air) continue;
                float maxDark = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                        float dark = world.getDarkness(nx, ny);
                        if (dark > maxDark) maxDark = dark;
                    }
                }
                if (maxDark > 0) {
                    Floor floor = tile.floor();
                    if (floor != null) {
                        Block wall = floor.wall;
                        if (wall != null && wall != Blocks.air) {
                            tile.setBlock(wall);
                        }
                    }
                }
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                Floor floor = tile.floor();
                Block block = tile.block();
                if (rand.chance(0.0075) && block == Blocks.air) {
                    boolean any = false, all = true;
                    for (int i = 0; i < 4; i++) {
                        Tile other = tiles.get(x + Geometry.d4[i].x, y + Geometry.d4[i].y);
                        if (other != null && other.block() == Blocks.air) any = true;
                        else all = false;
                    }
                    if (any && ((block == Blocks.snowWall || block == Blocks.iceWall) ||
                            (all && block == Blocks.air && floor == Blocks.snow && rand.chance(0.03)))) {
                        tile.setBlock(rand.chance(0.5) ? Blocks.whiteTree : Blocks.whiteTreeDead);
                    }
                }
                if (block == Blocks.air) {
                    boolean nearSolid = false;
                    for (int i = 0; i < 4; i++) {
                        Tile other = tiles.get(x + Geometry.d4[i].x, y + Geometry.d4[i].y);
                        if (other != null && other.block() != Blocks.air) {
                            nearSolid = true;
                            break;
                        }
                    }
                    if (!nearSolid && rand.chance(0.01) && floor.hasSurface() && tile.block() == Blocks.air) {
                        Block deco = decMap.get(floor, floor.decoration);
                        if (deco != null && deco != Blocks.air) {
                            tile.setBlock(deco);
                        }
                    }
                }
            }
        }

        int spawnX = cx, spawnY = cy;
        Seq<Vec2> enemySpawns = new Seq<>();
        int offset = rand.nextInt(360);
        float length = w / 2.55f - rand.random(13f, 23f);
        int waterCheckRad = 5;

        for (int i = 0; i < 360; i += 5) {
            int angle = offset + i;
            int ex = (int)(w / 2f + Angles.trnsx(angle, length));
            int ey = (int)(h / 2f + Angles.trnsy(angle, length));
            if (ex < 0 || ex >= w || ey < 0 || ey >= h) continue;

            int waterTiles = 0;
            for (int rx = -waterCheckRad; rx <= waterCheckRad; rx++) {
                for (int ry = -waterCheckRad; ry <= waterCheckRad; ry++) {
                    int tx = ex + rx, ty = ey + ry;
                    if (tx < 0 || tx >= w || ty < 0 || ty >= h) continue;
                    Tile t = tiles.getn(tx, ty);
                    if (t != null && t.floor() != null && t.floor().liquidDrop != null) {
                        waterTiles++;
                    }
                }
            }

            if (waterTiles <= 4) {
                spawnX = ex;
                spawnY = ey;
                int enemyCount = Math.max(1, (int)(difficulty * 4));
                for (int j = 0; j < enemyCount; j++) {
                    float enemyOffset = rand.range(60f);
                    int ex2 = (int)(spawnX + Angles.trnsx(180f + enemyOffset, w / 2.5f));
                    int ey2 = (int)(spawnY + Angles.trnsy(180f + enemyOffset, w / 2.5f));
                    if (ex2 >= 0 && ex2 < w && ey2 >= 0 && ey2 < h) {
                        enemySpawns.add(new Vec2(ex2, ey2));
                    }
                }
                break;
            }
        }

        Seq<Tile> enemyTiles = new Seq<>();
        for (Vec2 e : enemySpawns) {
            Tile tile = tiles.getn((int)e.x, (int)e.y);
            if (tile != null) {
                tile.setOverlay(Blocks.spawn);
                enemyTiles.add(tile);
            }
        }

        if (enemySpawns.size > 0) {
            state.rules.attackMode = true;
            int baseWaves = 20;
            int extraWaves = (int)(difficulty * 30);
            state.rules.winWave = baseWaves + extraWaves;
            state.rules.waveSpacing = 60f * 60f * 2f;
            state.rules.waves = true;
            state.rules.enemyCoreBuildRadius = 600f;
            state.rules.waveTeam = Team.crux;
            try {
                state.rules.spawns = Waves.generate(difficulty, new Rand(sector != null ? sector.id : 0), true, false, false);
            } catch (Exception e) {
                Log.err("波次生成失败: @", e);
            }
        } else {
            int baseWaves = 20;
            int extraWaves = (int)(difficulty * 20);
            state.rules.winWave = baseWaves + extraWaves;
            state.rules.waves = true;
        }

        int coreX = spawnX, coreY = spawnY;
        boolean found = false;
        int searchRadius = 30;

        for (int r = 0; r <= searchRadius && !found; r++) {
            for (int dx = -r; dx <= r && !found; dx++) {
                for (int dy = -r; dy <= r && !found; dy++) {
                    int tx = spawnX + dx, ty = spawnY + dy;
                    if (tx < 0 || tx >= w || ty < 0 || ty >= h) continue;
                    Tile tile = tiles.getn(tx, ty);
                    if (tile == null) continue;
                    if (tile.block() != Blocks.air) continue;
                    Floor floor = tile.floor();
                    if (floor == null || floor.isLiquid) continue;
                    if (floor == Blocks.ice || floor == Blocks.iceSnow || floor == Blocks.snow) continue;

                    boolean hasWallNearby = false;
                    int checkRadius = 3;
                    for (int dx2 = -checkRadius; dx2 <= checkRadius && !hasWallNearby; dx2++) {
                        for (int dy2 = -checkRadius; dy2 <= checkRadius && !hasWallNearby; dy2++) {
                            int nx = tx + dx2, ny = ty + dy2;
                            if (nx < 0 || nx >= w || ny < 0 || ny >= h) {
                                hasWallNearby = true;
                                break;
                            }
                            Tile neighbor = tiles.getn(nx, ny);
                            if (neighbor != null && neighbor.block() != Blocks.air) {
                                hasWallNearby = true;
                                break;
                            }
                        }
                    }
                    if (!hasWallNearby) {
                        coreX = tx;
                        coreY = ty;
                        found = true;
                    }
                }
            }
        }

        if (!found) {
            coreX = spawnX;
            coreY = spawnY;
            int clearRadius = 6;
            for (int dx = -clearRadius; dx <= clearRadius; dx++) {
                for (int dy = -clearRadius; dy <= clearRadius; dy++) {
                    int tx = coreX + dx, ty = coreY + dy;
                    if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
                        Tile tile = tiles.getn(tx, ty);
                        if (tile != null) {
                            tile.setBlock(Blocks.air);
                            Floor floor = tile.floor();
                            if (floor == null || floor.isLiquid || floor == Blocks.ice || floor == Blocks.iceSnow || floor == Blocks.snow) {
                                tile.setFloor(Blocks.stone.asFloor());
                            }
                        }
                    }
                }
            }
        }

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int tx = coreX + dx, ty = coreY + dy;
                if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
                    Tile tile = tiles.getn(tx, ty);
                    if (tile != null && tile.block() != Blocks.air) {
                        tile.setBlock(Blocks.air);
                    }
                }
            }
        }

        Schematics.placeLaunchLoadout(coreX, coreY);

        state.rules.env = Env.terrestrial;
        state.rules.placeRangeCheck = true;
    }

    @Override
    public float getSizeScl() {
        return 2000f;
    }
}