package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.content.Loadouts;
import mindustry.game.Schematics;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.Env;

import static mindustry.Vars.*;

public class SuperPlanetGenerator extends PlanetGenerator {

    public float heightScale = 4.5f;
    public float heightPow = 1.8f;
    public float waterOffset = 0.02f;
    public int octaves = 6;
    public float persistence = 0.55f;
    public float frequency = 1f / 3.5f;
    public float waterLevel = 0.12f;
    public float mountainLevel = 0.55f;

    private float tmpTemp = 0.5f;
    private float tmpHumidity = 0.5f;

    public SuperPlanetGenerator() {
        baseSeed = 77;
        defaultLoadout = Loadouts.basicShard;
    }

    public float rawHeight(Vec3 position) {
        float x = position.x * heightScale;
        float y = position.y * heightScale + 50f;
        float z = position.z * heightScale;

        float noise = Simplex.noise3d(baseSeed, octaves, persistence, frequency, x, y, z);

        float mountain = Ridged.noise3d(baseSeed + 100,
                position.x * 4f, position.y * 4f + 100f, position.z * 4f,
                3, 0.45f) * 0.25f;

        float river = Simplex.noise3d(baseSeed + 200, 4, 0.5f, 0.2f,
                position.x * 2.5f, position.y * 2.5f, position.z * 2.5f) * 0.12f;

        float shelf = Simplex.noise3d(baseSeed + 300, 3, 0.6f, 0.15f,
                position.x * 2f, position.y * 2f, position.z * 2f) * 0.08f;

        float combined = noise + mountain + river + shelf;

        float sign = combined >= 0f ? 1f : -1f;
        float h = (float) Math.pow(Math.abs(combined), heightPow) * sign + waterOffset;
        h = h / (1f + waterOffset);

        if (h > 0.05f && h < 0.3f) {
            h = h + (h - 0.05f) * 0.2f;
        }

        return Mathf.clamp(h, 0f, 0.8f);
    }

    private void computeClimate(Vec3 position) {
        float lat = Math.abs(position.y);
        float baseTemp = 1.0f - lat * 1.3f;
        float height = rawHeight(position);
        float altTemp = height * 0.3f;

        float current = Simplex.noise3d(baseSeed + 400, 4, 0.5f, 0.3f,
                position.x * 3f, position.y * 3f, position.z * 3f) * 0.15f;

        float temp = baseTemp - altTemp + current;

        float baseHumidity = Simplex.noise3d(baseSeed + 500, 5, 0.5f, 0.25f,
                position.x * 4f, position.y * 4f, position.z * 4f) * 0.3f + 0.5f;

        float altHumidity = height * 0.2f;
        float humidity = baseHumidity - altHumidity + 0.2f;

        float coast = Math.abs(height - waterLevel);
        if (coast < 0.05f) humidity += 0.2f;

        tmpTemp = Mathf.clamp(temp, 0f, 1f);
        tmpHumidity = Mathf.clamp(humidity, 0f, 1f);
    }

    private String getBiome(Vec3 position) {
        computeClimate(position);
        float lat = Math.abs(position.y);
        float height = rawHeight(position);
        float humidity = tmpHumidity;

        if (height < waterLevel) {
            if (height < waterLevel * 0.4f) return "deepwater";
            return "water";
        }

        if (height < waterLevel + 0.04f) return "beach";

        if (lat > 0.8f) {
            if (height > 0.6f) return "arctic_mountain";
            if (height > 0.3f) return "tundra";
            return "polar";
        }

        if (lat > 0.65f) {
            if (height > 0.6f) return "taiga_mountain";
            if (height > 0.35f) return "taiga";
            return "boreal_forest";
        }

        if (lat > 0.35f) {
            if (height > 0.6f) return "temperate_mountain";
            if (humidity > 0.6f) {
                if (height > 0.3f) return "temperate_forest";
                return "temperate_grassland";
            }
            if (humidity > 0.3f) return "temperate_grassland";
            return "temperate_steppe";
        }

        if (lat > 0.15f) {
            if (height > 0.6f) return "subtropical_mountain";
            if (humidity > 0.7f) {
                if (height > 0.35f) return "subtropical_forest";
                return "subtropical_wetland";
            }
            if (humidity > 0.4f) return "subtropical_grassland";
            return "subtropical_savanna";
        }

        if (height > 0.6f) return "tropical_mountain";
        if (humidity > 0.7f) {
            if (height > 0.35f) return "tropical_rainforest";
            return "tropical_wetland";
        }
        if (humidity > 0.4f) return "tropical_forest";
        return "tropical_savanna";
    }

    public Block getBlock(Vec3 position, boolean visualOnly) {
        float height = rawHeight(position);
        String biome = getBiome(position);

        switch (biome) {
            case "deepwater": return Blocks.deepwater;
            case "water": return Blocks.water;
            case "beach": return Blocks.sand;
            case "tropical_rainforest": return height > 0.3f ? Blocks.sporeMoss : Blocks.moss;
            case "tropical_forest": return height > 0.3f ? Blocks.moss : Blocks.grass;
            case "tropical_wetland": return height > 0.2f ? Blocks.moss : Blocks.grass;
            case "tropical_savanna": return height > 0.4f ? Blocks.grass : Blocks.sand;
            case "tropical_mountain": return height > 0.7f ? Blocks.basalt : (height > 0.5f ? Blocks.stone : Blocks.moss);
            case "subtropical_forest": return height > 0.3f ? Blocks.moss : Blocks.grass;
            case "subtropical_wetland": return height > 0.2f ? Blocks.moss : Blocks.grass;
            case "subtropical_grassland": return Blocks.grass;
            case "subtropical_savanna": return height > 0.4f ? Blocks.grass : Blocks.sand;
            case "subtropical_mountain": return height > 0.7f ? Blocks.rhyolite : (height > 0.5f ? Blocks.stone : Blocks.moss);
            case "temperate_forest": return height > 0.3f ? Blocks.moss : Blocks.grass;
            case "temperate_grassland": return Blocks.grass;
            case "temperate_steppe": return height > 0.4f ? Blocks.grass : Blocks.sand;
            case "temperate_mountain": return height > 0.7f ? Blocks.dacite : (height > 0.5f ? Blocks.stone : Blocks.moss);
            case "taiga": return height > 0.3f ? Blocks.stone : Blocks.moss;
            case "boreal_forest": return height > 0.3f ? Blocks.moss : Blocks.grass;
            case "taiga_mountain": return height > 0.6f ? Blocks.basalt : Blocks.stone;
            case "tundra": return height > 0.3f ? Blocks.stone : Blocks.snow;
            case "polar": return height > 0.3f ? Blocks.ice : Blocks.snow;
            case "arctic_mountain": return height > 0.5f ? Blocks.stone : Blocks.ice;
            default: return Blocks.grass;
        }
    }

    private Block getVegetation(Vec3 position, Floor floor) {
        String biome = getBiome(position);
        float height = rawHeight(position);
        float lat = Math.abs(position.y);

        if (floor == Blocks.water || floor == Blocks.deepwater || floor == Blocks.sand) return null;

        float density;
        switch (biome) {
            case "tropical_rainforest": density = 0.12f; break;
            case "tropical_forest": density = 0.08f; break;
            case "tropical_wetland": density = 0.06f; break;
            case "tropical_savanna": density = 0.02f; break;
            case "tropical_mountain": density = 0.03f; break;
            case "subtropical_forest": density = 0.07f; break;
            case "subtropical_wetland": density = 0.05f; break;
            case "subtropical_grassland": density = 0.03f; break;
            case "subtropical_savanna": density = 0.02f; break;
            case "temperate_forest": density = 0.06f; break;
            case "temperate_grassland": density = 0.03f; break;
            case "temperate_steppe": density = 0.015f; break;
            case "taiga": density = 0.025f; break;
            case "boreal_forest": density = 0.04f; break;
            case "tundra": density = 0.01f; break;
            default: density = 0.02f;
        }

        if (height > 0.5f) density *= 0.3f;
        if (height < 0.15f) density *= 0.5f;

        if (rand.nextFloat() > density) return null;

        float vegType = rand.nextFloat();

        if (lat < 0.35f) {
            if (vegType < 0.3f) return Blocks.pine;
            if (vegType < 0.5f) return Blocks.shrubs;
            return Blocks.sporeCluster;
        }

        if (lat < 0.65f) {
            if (vegType < 0.4f) return rand.nextFloat() < 0.6f ? Blocks.pine : Blocks.whiteTree;
            if (vegType < 0.6f) return Blocks.shrubs;
            if (vegType < 0.8f) return Blocks.moss;
            return Blocks.sporeCluster;
        }

        if (vegType < 0.3f) return Blocks.whiteTree;
        if (vegType < 0.5f) return Blocks.pine;
        if (vegType < 0.7f) return Blocks.shrubs;
        return Blocks.stone;
    }

    @Override
    public float getHeight(Vec3 position) {
        float height = rawHeight(position);
        return Math.max(height, waterLevel);
    }

    @Override
    public void getColor(Vec3 position, Color out) {
        Block block = getBlock(position, true);
        float height = rawHeight(position);
        computeClimate(position);
        float humidity = tmpHumidity;
        float temp = tmpTemp;

        if (block == Blocks.grass) {
            float g = 180f + humidity * 60f;
            float r = 120f - humidity * 30f + height * 30f;
            float b = 80f - humidity * 30f;
            out.r = Mathf.clamp(r / 255f, 0f, 1f);
            out.g = Mathf.clamp(g / 255f, 0f, 1f);
            out.b = Mathf.clamp(b / 255f, 0f, 1f);
            out.a = 1f;
            return;
        }
        if (block == Blocks.moss) {
            float g = 140f + humidity * 50f;
            float r = 100f - humidity * 20f;
            float b = 70f - humidity * 20f;
            out.r = Mathf.clamp(r / 255f, 0f, 1f);
            out.g = Mathf.clamp(g / 255f, 0f, 1f);
            out.b = Mathf.clamp(b / 255f, 0f, 1f);
            out.a = 1f;
            return;
        }
        if (block == Blocks.sporeMoss) {
            float r = 140f + humidity * 30f;
            float g = 80f + humidity * 20f;
            float b = 180f + humidity * 40f;
            out.r = Mathf.clamp(r / 255f, 0f, 1f);
            out.g = Mathf.clamp(g / 255f, 0f, 1f);
            out.b = Mathf.clamp(b / 255f, 0f, 1f);
            out.a = 1f;
            return;
        }
        if (block == Blocks.water) {
            float depth = (waterLevel - height) / waterLevel;
            float blue = 150f + (1f - temp) * 50f;
            float green = 80f + temp * 30f;
            out.r = 40f / 255f;
            out.g = Mathf.clamp(green / 255f, 0f, 1f);
            out.b = Mathf.clamp(blue / 255f, 0f, 1f);
            out.a = 0.6f + depth * 0.2f;
            return;
        }
        if (block == Blocks.deepwater) {
            out.r = 20f / 255f;
            out.g = 60f / 255f;
            out.b = 120f / 255f;
            out.a = 0.5f;
            return;
        }
        if (block == Blocks.sand) {
            float r = 200f + humidity * 30f;
            float g = 180f + humidity * 20f;
            float b = 140f + humidity * 20f;
            out.r = Mathf.clamp(r / 255f, 0f, 1f);
            out.g = Mathf.clamp(g / 255f, 0f, 1f);
            out.b = Mathf.clamp(b / 255f, 0f, 1f);
            out.a = 1f;
            return;
        }
        if (block == Blocks.stone) {
            float shade = 120f - height * 80f;
            out.r = Mathf.clamp(shade / 255f, 0f, 1f);
            out.g = Mathf.clamp(shade / 255f, 0f, 1f);
            out.b = Mathf.clamp(shade / 255f, 0f, 1f);
            out.a = 1f;
            return;
        }
        if (block == Blocks.basalt) {
            out.set(Color.valueOf("505057"));
            out.a = 1f;
            return;
        }
        if (block == Blocks.rhyolite) {
            out.set(Color.valueOf("786e5a"));
            out.a = 1f;
            return;
        }
        if (block == Blocks.dacite) {
            out.set(Color.valueOf("826e50"));
            out.a = 1f;
            return;
        }
        if (block == Blocks.shale) {
            out.set(Color.valueOf("6e6e5f"));
            out.a = 1f;
            return;
        }
        if (block == Blocks.ice) {
            float blue = 200f + (1f - temp) * 40f;
            out.r = 180f / 255f;
            out.g = 200f / 255f;
            out.b = Mathf.clamp(blue / 255f, 0f, 1f);
            out.a = 0.85f;
            return;
        }
        if (block == Blocks.snow) {
            out.set(Color.valueOf("e6ebf0"));
            out.a = 0.95f;
            return;
        }

        out.set(block.mapColor);
        out.a = 1f - block.albedo;
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        Block block = getBlock(position, false);
        tile.floor = block;
        tile.block = Blocks.air;
    }

    @Override
    public void postGenerate(Tiles tiles) {
        if (tiles == null) return;

        int w = tiles.width;
        int h = tiles.height;
        int cx = w / 2;
        int cy = h / 2;

        // 阶段 1：用 Ridged 噪声生成基本墙壁
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                Floor floor = tile.floor();
                if (floor == null || floor.isLiquid) continue;
                Block wall = floor.wall;
                if (wall == null || wall == Blocks.air) continue;

                Vec3 pos = sector.rect.project((float) x / w, (float) y / h);
                float wallNoise = Ridged.noise3d(baseSeed + 700, pos.x, pos.y, pos.z, 2, 22f);
                if (wallNoise > 0.35f) {
                    tile.setBlock(wall);
                }
            }
        }

        // 阶段 2：黑暗度包裹法
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null || tile.block() != Blocks.air) continue;
                if (tile.floor() == null || tile.floor().isLiquid) continue;

                float maxDark = 0f;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                        float dark = world.getDarkness(nx, ny);
                        if (dark > maxDark) maxDark = dark;
                    }
                }
                if (maxDark > 0f) {
                    Block wall = tile.floor().wall;
                    if (wall != null && wall != Blocks.air) {
                        tile.setBlock(wall);
                    }
                }
            }
        }

        // 阶段 3：出生点清理
        int startClear = 5;
        for (int dx = -startClear; dx <= startClear; dx++) {
            for (int dy = -startClear; dy <= startClear; dy++) {
                int tx = cx + dx, ty = cy + dy;
                if (tx < 0 || tx >= w || ty < 0 || ty >= h) continue;
                Tile tile = tiles.getn(tx, ty);
                if (tile != null) {
                    tile.setBlock(Blocks.air);
                }
            }
        }

        // 阶段 4：BFS 主区域标记
        boolean[][] reachable = new boolean[w][h];
        Queue<Point2> queue = new Queue<>();
        queue.addLast(new Point2(cx, cy));
        reachable[cx][cy] = true;

        while (queue.size > 0) {
            Point2 p = queue.removeFirst();
            for (Point2 d : Geometry.d4) {
                int nx = p.x + d.x;
                int ny = p.y + d.y;
                if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                if (reachable[nx][ny]) continue;
                Tile t = tiles.getn(nx, ny);
                if (t == null) continue;
                if (t.block() != Blocks.air) continue;
                if (t.floor() == null || t.floor().isLiquid) continue;
                reachable[nx][ny] = true;
                queue.addLast(new Point2(nx, ny));
            }
        }

        // 阶段 5：把不可达的 air tile 转为墙
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                if (tile.block() != Blocks.air) continue;
                if (reachable[x][y]) continue;
                if (tile.floor() == null || tile.floor().isLiquid) continue;
                Block wall = tile.floor().wall;
                if (wall != null && wall != Blocks.air) {
                    tile.setBlock(wall);
                }
            }
        }

        // 阶段 6：出生点二次清理
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                int tx = cx + dx, ty = cy + dy;
                if (tx < 0 || tx >= w || ty < 0 || ty >= h) continue;
                Tile tile = tiles.getn(tx, ty);
                if (tile == null) continue;
                tile.setBlock(Blocks.air);
                Floor floor = tile.floor();
                if (floor == null || floor.isLiquid) {
                    tile.setFloor(Blocks.grass.asFloor());
                }
            }
        }

        // 阶段 7：矿物生成
        float poles = sector != null ? Math.abs(sector.tile.v.y) : 0f;
        float nmag = 0.5f;
        float scl = 1f;
        float addscl = 1.3f;

        Seq<Block> ores = Seq.with(Blocks.oreCopper, Blocks.oreLead);
        if (sector != null && Simplex.noise3d(baseSeed, 2, 0.5f, scl,
                sector.tile.v.x, sector.tile.v.y, sector.tile.v.z) * nmag + poles > 0.45f * addscl) {
            ores.add(Blocks.oreCoal);
        }
        if (sector != null && Simplex.noise3d(baseSeed, 2, 0.5f, scl,
                sector.tile.v.x + 1f, sector.tile.v.y, sector.tile.v.z) * nmag + poles > 0.5f * addscl) {
            ores.add(Blocks.oreTitanium);
        }
        if (sector != null && Simplex.noise3d(baseSeed, 2, 0.5f, scl,
                sector.tile.v.x + 2f, sector.tile.v.y, sector.tile.v.z) * nmag + poles > 0.88f * addscl) {
            ores.add(Blocks.oreThorium);
        }
        if (rand.chance(0.25f)) {
            ores.add(Blocks.oreScrap);
        }

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
                if (tile.block() != Blocks.air) continue;
                if (tile.overlay() != Blocks.air) continue;
                if (Math.abs(x - cx) <= 6 && Math.abs(y - cy) <= 6) continue;

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

        // 阶段 8：植被生成
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile == null) continue;
                if (tile.block() != Blocks.air) continue;
                Floor floor = tile.floor();
                if (floor == null || floor.isLiquid) continue;
                if (tile.overlay() != Blocks.air) continue;
                if (Math.abs(x - cx) <= 6 && Math.abs(y - cy) <= 6) continue;

                Vec3 pos = sector.rect.project((float) x / w, (float) y / h);
                Block veg = getVegetation(pos, floor);
                if (veg != null && veg != Blocks.air) {
                    tile.setBlock(veg);
                }
            }
        }

        // 阶段 9：出生点放置与规则
        Schematics.placeLaunchLoadout(cx, cy);

        state.rules.waves = true;
        state.rules.waveSpacing = 60f * 60f * 1.5f;
        state.rules.env = Env.terrestrial;
        state.rules.winWave = 15;
        state.rules.placeRangeCheck = true;
        state.rules.attackMode = false;
        state.rules.buildSpeedMultiplier = 1.2f;
        state.rules.blockHealthMultiplier = 1.0f;
    }

    protected float noise(float x, float y, double octaves, double falloff, double scl, double mag) {
        Vec3 v = sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z) * (float) mag;
    }

    @Override
    public float getSizeScl() {
        return 2000f;
    }
}