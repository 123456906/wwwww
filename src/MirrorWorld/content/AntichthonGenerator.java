package MirrorWorld.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.IntSet;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.*;

public class AntichthonGenerator extends PlanetGenerator {
    public static final int SEED = 8888;
    public static final float scl = 5.0f;
    public static final float heightYOffset = 42.7f;
    public static final float heightScl = 1.01f;
    public static final float waterOffset = 0.04f;

    public static final float TILT_ANGLE = 14f * 3.1415926535f / 180f;
    public static final float TILT_COS = Mathf.cos(TILT_ANGLE);
    public static final float TILT_SIN = Mathf.sin(TILT_ANGLE);

    public static final Vec3 basePos = new Vec3(0.9341721f, 0.0f, 0.3568221f);
    public static final float baseFreq = 0.07f;
    public static final float baseFreq2 = 0.09f;
    public static final float baseRange = 1.05f;
    public static final float baseThreshold = 0.30f;
    public static final float ringGapRatio = 0.12f;
    public static final float subGapRatio = 0.08f;

    /* ================= 新增：外观调参区（想改观感只动这里） ================= */

    /** 墙壁生成 A：黑暗度阈值（针对 darkness() 噪声输出 0~1.4 的标度）。调高墙更少更成块 */
    public static final float darkWallThreshold = 0.58f;

    /** 墙壁生成 B：峰脊菌脉。threshold 越大墙脉越细越少（0.88~0.93 比较好看） */
    public static final float veinScale = 52f;
    public static final float veinThreshold = 0.905f;

    /** 墙壁生成 C：团块菌室。阈值 0.78~0.84 */
    public static final float blobScale = 34f;
    public static final float blobThreshold = 0.8f;

    /** 出生点留白半径，避免落地就被墙围死 */
    public static final int spawnClearRadius = 22;

    /** 绯红白杆菌生物群系：覆盖率与边缘过渡 */
    public static final float crimsonScale = 1.9f;
    public static final float crimsonThreshold = 0.52f;

    public static final Floor[][] marsArr = {
            {AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.rustyGravel, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.darkRedSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redSand, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.redSand, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.darkRedSand, AntichthonBlocks.rustyGravel, AntichthonBlocks.rustyGravel, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.paleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.paleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.deepPaleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.paleMoss, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.redRock, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.deepDryIce, AntichthonBlocks.deepDryIce, AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.redRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock},
            {AntichthonBlocks.deepDryIce, AntichthonBlocks.deepDryIce, AntichthonBlocks.deepDryIce, AntichthonBlocks.deepDryIce, AntichthonBlocks.dryIce, AntichthonBlocks.dryIce, AntichthonBlocks.paleMoss, AntichthonBlocks.deepPaleMoss, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock, AntichthonBlocks.hematiteRock}
    };

    public static Schematic bp1, bp2, bp3;

    public static void loadBlueprints() {
        try {
            bp1 = Schematics.readBase64("bXNjaAF4nCWLzQrCMBCEv6ax9e/oa3jzFepB9KTgQTzEukggTUsbFBHf3S0u7M63MwwlU8u8bmOSmA6uw3y+FMHdJAyYyxUbXSNkGxZ3Gered8m3EXJWzWt9PFXVXstPebe9VhIzdXd+K/HBUnFIIuHsQgBKXQxkevKRDAV/HsfqlxsmmjOKhR8wBx8D");
            bp2 = Schematics.readBase64("bXNjaAF4nCWMwQrCMBBEZ2NabfXob3j0E+pB9KTgQTzEupRAmpQmKCL+uxu7MDs7w/KwQKVRt8En9uloBqjPF6Uzd3YR6nqD9qZn0BbLB8d2tEOywQMzrPrXJiZmdzHOYS3pdG6ag6Ce/A6jABIqafd2x74DUItA+E8x3fO8lIiUEDWIxJBN55pQSlYQK6Z3ST/A/h8g");
            bp3 = Schematics.readBase64("bXNjaAF4nCVMywrCMBCcJq19ehD8DU9+glRQ9KTgQTzEukghTUoSFBH/3aRZWGZnZmdQYZ6i6rRypNxRjGDfH2ZS3ElasOsNqRIDIVmjfpDtTD+6XisgxXJ4r07ntj348Is+2viIQ+NV64jkRUg5sZ3RamO0tSg92/dbUk8AC7/IgQQM07B4MF89iVlkRQAePR5YcHi4sxifxXiOKsg56ggNeOLfeXgpQlmAEvgD5+kkXg==");
        } catch (Exception e) {
            Log.err("Failed to load blueprints: " + e);
        }
    }

    public AntichthonGenerator() {
    }

    @Override
    public float getHeight(Vec3 pos) {
        float h = rawHeight(pos);
        return Math.max(h, 2.0f / marsArr[0].length);
    }

    @Override
    public void getColor(Vec3 pos, Color out) {
        try {
            Block block = getBlock(pos, true);
            if (block != null) {
                out.set(block.mapColor);
                if (block instanceof Floor && ((Floor) block).liquidDrop != null) {
                    out.mul(0.7f);
                    out.a = 1.0f;
                } else {
                    float a = 1.0f - block.albedo;
                    out.a = Float.isNaN(a) ? 1.0f : a;
                }
            } else {
                out.set(Color.valueOf("888888"));
                out.a = 1f;
            }
        } catch (Exception e) {
            out.set(Color.valueOf("888888"));
            out.a = 1f;
        }
    }

    @Override
    public void genTile(Vec3 pos, TileGen tile) {
        Block floor = getBlock(pos, false);
        if (floor == Blocks.darkPanel6) floor = Blocks.darkPanel3;
        tile.floor = floor.asFloor();
        tile.block = Blocks.air;

        // ===== 墙壁生成 B / C：和黑暗度那套完全并存的第二种、第三种长墙方式 =====
        // 放在 genTile 里而不是 postGenerate，是为了让后面的“找安全出生点”能绕开这些墙
        Floor f = floor.asFloor();
        if (f.liquidDrop == null) {
            if (AntichthonBlocks.veinWall(pos, SEED + 4101, veinScale, veinThreshold)) {
                tile.block = AntichthonBlocks.crimsonVeinWall;
            } else if (AntichthonBlocks.blobWall(pos, SEED + 4207, blobScale, blobThreshold)) {
                tile.block = AntichthonBlocks.crimsonBloomWall;
            }
        }
    }

    public Block getBlock(Vec3 pos, boolean visualOnly) {
        try {
            float height = rawHeight(pos);
            float px = pos.x * scl;
            float py = pos.y * scl;
            float pz = pos.z * scl;

            float latNoise1 = Simplex.noise3d(SEED + 333, 3, 0.5f, 4.0f, pos.x, pos.y, pos.z);
            float latNoise2 = Simplex.noise3d(SEED + 444, 2, 0.5f, 12.0f, pos.x, pos.y, pos.z);

            float yRot = pos.y * TILT_COS + pos.z * TILT_SIN;
            float absLat = Math.abs(yRot + latNoise1 * 0.08f + latNoise2 * 0.03f);

            if (absLat > 0.72f) {
                return absLat > 0.85f ? AntichthonBlocks.deepDryIce : AntichthonBlocks.dryIce;
            }

            float rad = scl;
            float temp = Mathf.clamp(Math.abs(yRot * 2f) / rad);
            float tnoise = Simplex.noise3d(SEED, 7, 0.56f, 1f / 3f, px, py + 999f - 0.1f, pz);
            temp = temp + (tnoise - temp) * 0.5f;

            height *= 1.2f;
            height = Mathf.clamp(height);

            int ti = Mathf.clamp((int) (temp * marsArr.length), 0, marsArr.length - 1);
            int hi = Mathf.clamp((int) (height * marsArr[0].length), 0, marsArr[0].length - 1);
            Block res = marsArr[ti][hi];

            if (res == null) return Blocks.stone;

            if (absLat < 0.45f) {
                if (res == AntichthonBlocks.dryIce || res == AntichthonBlocks.deepDryIce) res = AntichthonBlocks.redSand;
                if (res == AntichthonBlocks.paleMoss || res == AntichthonBlocks.deepPaleMoss) res = AntichthonBlocks.rustyGravel;
            }

            if (absLat < 0.28f) {
                float lakeNoise = Simplex.noise3d(SEED + 111, 4, 0.5f, 5.0f, pos.x, pos.y, pos.z);

                if (lakeNoise > 0.4f) {
                    float waterLevel = 0.38f - (absLat / 0.28f) * 0.08f;

                    if (height < waterLevel) {
                        // 浅滩范围加宽：原来是 -0.05，改 -0.09，湖岸有更长的过渡带，不容易一坨深水
                        if (height < waterLevel - 0.09f) {
                            res = AntichthonBlocks.co2Deep;
                        } else {
                            res = AntichthonBlocks.co2Shallow;
                        }
                    }
                }
            }

            float baseDist = pos.dst(basePos);
            if (baseDist < baseRange) {
                float baseNoise = Simplex.noise3d(SEED, 3, 0.4f, 5.5f, pos.x, pos.y + 200f, pos.z) * 0.015f;
                int mod1 = (baseDist % baseFreq < baseFreq / 2f) ? 1 : 0;

                float ringNoise = Simplex.noise3d(SEED + 777, 2, 0.5f, 9.0f, pos.x, pos.y + 400f, pos.z) * 0.006f;
                float ringPhase = (baseDist + ringNoise) % baseFreq;
                if (ringPhase < baseFreq * ringGapRatio) {
                    return res;
                }

                float subGapNoise = Simplex.noise3d(SEED + 888, 2, 0.5f, 12.0f, pos.x, pos.y + 600f, pos.z) * 0.005f;
                float subPhase = (baseDist + subGapNoise) % baseFreq2;
                if (subPhase < baseFreq2 * subGapRatio) {
                    return res;
                }

                if (baseDist * 0.85f + baseNoise + mod1 * 0.07f < baseThreshold) {
                    float mod2 = (baseDist + 0.01f) % baseFreq2;
                    if (mod2 < baseFreq2 * 0.65f) return Blocks.metalFloor;
                    else return Blocks.darkPanel6;
                }
            }

            // ===== 绯红白杆菌生物群系（新增） =====
            // 独立噪声场，边界再叠一层高频噪声做“蔓延”过渡，避免色块硬切
            if (res != AntichthonBlocks.co2Deep && res != AntichthonBlocks.co2Shallow) {
                float crimsonField = Simplex.noise3d(SEED + 9101, 4, 0.55f, 1f / crimsonScale, px, py + 1300f, pz);
                float crimsonEdge = Simplex.noise3d(SEED + 9102, 3, 0.6f, 1f / 0.45f, px, py + 1700f, pz) * 0.055f;

                if (crimsonField > crimsonThreshold + crimsonEdge && absLat > 0.16f) {
                    float whiteField = Simplex.noise3d(SEED + 9103, 3, 0.5f, 1f / 0.9f, px, py + 2100f, pz);
                    res = whiteField > 0.5f ? AntichthonBlocks.whiteStalkCarpet : AntichthonBlocks.crimsonCarpet;
                }
            }

            // ===== 镜潮流体湖：低洼处自然形成，湖底 Floor 的 liquidDrop 决定泵抽出的液体 =====
            float tideLake = Simplex.noise3d(SEED + 7101, 3, 0.52f, 1f / 7f, px, py + 3100f, pz);
            if (tideLake > 0.64f && height < 0.32f && absLat > 0.17f && absLat < 0.60f
                    && pos.dst(basePos) > baseRange
                    && res != AntichthonBlocks.co2Deep && res != AntichthonBlocks.co2Shallow) {
                float type = Simplex.noise3d(SEED + 7102, 2, 0.5f, 1f / 3f, px, py + 3600f, pz);
                boolean deep = height < 0.25f;

                if (type < 0.32f) {
                    res = deep ? AntichthonBlocks.mirrorDeep : AntichthonBlocks.mirrorShallow;
                } else if (type < 0.40f) {
                    res = deep ? AntichthonBlocks.lumenDeep : AntichthonBlocks.lumenShallow;
                } else if (type < 0.47f) {
                    res = deep ? AntichthonBlocks.phaseDeep : AntichthonBlocks.phaseShallow;
                } else if (type < 0.54f) {
                    res = deep ? AntichthonBlocks.voidDeep : AntichthonBlocks.voidShallow;
                } else if (type < 0.66f) {
                    res = deep ? AntichthonBlocks.cryoDeep : AntichthonBlocks.cryoShallow;
                } else {
                    res = deep ? AntichthonBlocks.moltenDeep : AntichthonBlocks.moltenShallow;
                }
            }

            float olivine = Simplex.noise3d(SEED + 5000, 4, 0.55f, 1f / 2f, px, py + 999f, pz);
            if (olivine > 0.55f && !visualOnly) {
                return AntichthonBlocks.olivineSand;
            }

            if (res == AntichthonBlocks.paleMoss && !visualOnly) {
                float lichenNoise = Simplex.noise3d(SEED + 6000, 3, 0.5f, 1f / 4f, px, py + 500f, pz);
                if (lichenNoise > 0.65f) return AntichthonBlocks.lichen;
            }

            return res;
        } catch (Exception e) {
            return Blocks.stone;
        }
    }

    private float rawHeight(Vec3 pos) {
        try {
            float n = Simplex.noise3d(SEED, 7, 0.5f, 1f / 3f,
                    pos.x * scl, pos.y * scl + heightYOffset, pos.z * scl);

            if (Float.isNaN(n)) n = 0f;
            float safeN = n * heightScl;
            if (safeN < 0) safeN = 0f;

            float p = (float) Math.pow(safeN, 2.3f);
            if (Float.isNaN(p)) p = 0f;

            float result = (p + waterOffset) / (1f + waterOffset);
            return Float.isNaN(result) ? 0.5f : result;
        } catch (Exception e) {
            return 0.5f;
        }
    }

    @Override
    public float getSizeScl() {
        return 2000f;
    }

    @Override
    public void postGenerate(Tiles tiles) {
        int w = tiles.width;
        int h = tiles.height;
        int cx = w / 2;
        int cy = h / 2;

        int spawnX = cx;
        int spawnY = cy;
        boolean safeFound = false;

        for (int attempt = 0; attempt < 1000; attempt++) {
            int testX = 30 + rand.nextInt(w - 60);
            int testY = 30 + rand.nextInt(h - 60);

            boolean isSafe = true;
            for (int dx = -8; dx <= 8; dx++) {
                for (int dy = -8; dy <= 8; dy++) {
                    int tx = testX + dx;
                    int ty = testY + dy;
                    if (tx < 0 || ty < 0 || tx >= w || ty >= h) continue;
                    Tile t = tiles.getn(tx, ty);
                    if (t.block() != Blocks.air || (t.floor() instanceof Floor && ((Floor) t.floor()).liquidDrop != null)) {
                        isSafe = false;
                        break;
                    }
                }
                if (!isSafe) break;
            }

            if (isSafe) {
                spawnX = testX;
                spawnY = testY;
                safeFound = true;
                break;
            }
        }

        if (!safeFound) Log.info("Could not find a safe spawn location, using map center.");

        Schematics.placeLaunchLoadout(spawnX, spawnY);

        Block[] ores = new Block[]{Blocks.oreCopper, Blocks.oreLead};
        if (Simplex.noise3d(SEED, 2, 0.5f, 1f, 0f, 0f, 0f) * 0.5f > 0.25f * 1.3f) {
            ores = append(ores, Blocks.oreCoal);
        }
        if (Simplex.noise3d(SEED, 2, 0.5f, 1f, 1f, 0f, 0f) * 0.5f > 0.5f * 1.3f) {
            ores = append(ores, Blocks.oreTitanium);
        }
        if (Simplex.noise3d(SEED, 2, 0.5f, 1f, 2f, 0f, 0f) * 0.5f > 0.7f * 1.3f) {
            ores = append(ores, Blocks.oreThorium);
        }

        // ===== 镜潮矿物：接入生成器后才能在自然地图上刷出 =====
        ores = append(ores, AntichthonBlocks.oreMirrorShard);
        ores = append(ores, AntichthonBlocks.orePrismCrystal);
        ores = append(ores, AntichthonBlocks.oreLumenCrystal);
        ores = append(ores, AntichthonBlocks.oreVoid);
        ores = append(ores, AntichthonBlocks.oreAbyss);

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                Tile tile = tiles.getn(i, j);
                if (!tile.floor().hasSurface()) continue;
                if (tile.block() != Blocks.air) continue;

                int offsetX = i - 4;
                int offsetY = j + 23;
                for (int oi = ores.length - 1; oi >= 0; oi--) {
                    Block entry = ores[oi];
                    float freq = -0.1f - oi * 0.01f;
                    if (Math.abs(0.5f - noise(offsetX, offsetY + oi * 999, 2, 0.7f, 40 + oi * 2f, 1f)) > 0.32f + oi * 0.01f &&
                            Math.abs(0.5f - noise(offsetX, offsetY - oi * 999, 1, 1f, 30 + oi * 4f, 1f)) > 0.48f + freq) {
                        tile.setOverlay(entry);
                        break;
                    }
                }
            }
        }

        /* ===== 墙壁生成 A：黑暗度（原有逻辑，保留；只做三处外观优化） =====
           1. 出生点周围留白，不再落地就被墙围死
           2. 阈值从“有一点暗就起墙”改成可调阈值，避免整图糊成一片墙
           3. 用噪声扰动阈值，让墙体边缘呈不规则侵蚀状，而不是贴着暗区平铺
        */
        try {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    Tile t = tiles.getn(x, y);
                    if (t == null) continue;
                    if (t.block() != Blocks.air) continue;
                    if (t.floor() == null || (t.floor() instanceof Floor && ((Floor) t.floor()).liquidDrop != null)) continue;
                    if (Mathf.within(x, y, spawnX, spawnY, spawnClearRadius)) continue;

                    float maxDark = 0f;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx, ny = y + dy;
                            if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                            float dark = darkness(nx, ny);
                            if (dark > maxDark) maxDark = dark;
                        }
                    }

                    if (maxDark <= 0f) continue;

                    float erosion = noise(x, y, 2, 0.6f, 42f, 1f);
                    float need = darkWallThreshold * (0.7f + erosion * 0.9f);
                    if (maxDark < need) continue;

                    Floor f = t.floor();
                    Block wall = f.wall;
                    if (wall != null && wall != Blocks.air) t.setBlock(wall);
                }
            }
        } catch (Exception ignored) {}

        /* ===== 装饰散布：蘑菇簇 / 石头 / 巨型蘑菇（新增） ===== */
        try {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    Tile t = tiles.getn(x, y);
                    if (t == null || t.block() != Blocks.air) continue;
                    Floor f = t.floor();
                    if (f == null || f.liquidDrop != null) continue;
                    if (Mathf.within(x, y, spawnX, spawnY, 10)) continue;

                    float n = noise(x, y, 3, 0.62f, 28f, 1f);
                    boolean crimson = f == AntichthonBlocks.crimsonCarpet || f == AntichthonBlocks.whiteStalkCarpet;

                    if (crimson) {
                        if (n > 0.993f) t.setBlock(AntichthonBlocks.giantMushroom);
                        else if (n > 0.966f) t.setBlock(AntichthonBlocks.giantMushroomStump);
                        else if (n > 0.935f) t.setBlock(AntichthonBlocks.crimsonMushroomCluster);
                        else if (n > 0.905f) t.setBlock(AntichthonBlocks.whiteStalkCluster);
                    } else if (f == AntichthonBlocks.redRock || f == AntichthonBlocks.hematiteRock
                            || f == AntichthonBlocks.rustyGravel) {
                        if (n > 0.945f) t.setBlock(AntichthonBlocks.crimsonRock);
                        else if (n > 0.915f) t.setBlock(AntichthonBlocks.paleRock);
                    }
                }
            }
        } catch (Exception ignored) {}

        try {
            placeBlueprints(tiles, 5 + rand.nextInt(4), spawnX, spawnY);
        } catch (Exception ignored) {}
    }

    private Block[] append(Block[] arr, Block b) {
        Block[] res = new Block[arr.length + 1];
        System.arraycopy(arr, 0, res, 0, arr.length);
        res[arr.length] = b;
        return res;
    }

    private float noise(float x, float y, int octaves, float falloff, float scl, float mag) {
        try {
            Vec3 v = sector.rect.project(x, y).scl(5f);
            return Simplex.noise3d(SEED, octaves, falloff, 1f / scl, v.x, v.y, v.z) * mag;
        } catch (Exception e) {
            return 0f;
        }
    }

    /** 黑暗度生成：模仿原版生成器，用多层噪声叠加出暗区；值越大越暗，越容易在暗区边缘起墙 */
    private float darkness(float x, float y) {
        float base = Math.abs(noise(x, y, 4, 0.55f, 16f, 1f));
        float detail = Math.abs(noise(x, y, 2, 0.6f, 34f, 1f));
        float patch = Math.abs(noise(x, y, 3, 0.5f, 8f, 1f));
        return base * 0.55f + detail * 0.35f + patch * 0.25f;
    }

    private void placeBlueprints(Tiles tiles, int count, int spawnX, int spawnY) {
        if (bp1 == null) return;
        int placed = 0;
        int attempts = 0;

        while (placed < count && attempts < 1000) {
            attempts++;
            int cx = 20 + rand.nextInt(tiles.width - 40);
            int cy = 20 + rand.nextInt(tiles.height - 40);

            if (Mathf.within(cx, cy, spawnX, spawnY, 30)) continue;

            float r = rand.nextFloat();
            Schematic bp = bp1;
            if (r > 0.6f && r <= 0.85f) bp = bp2;
            else if (r > 0.85f) bp = bp3;

            int bw = bp.width;
            int bh = bp.height;

            int startX = cx - bw / 2;
            int startY = cy - bh / 2;

            if (startX < 5 || startY < 5 || startX + bw >= tiles.width - 5 || startY + bh >= tiles.height - 5) continue;

            boolean canPlace = true;
            for (int i = 0; i < bw; i++) {
                for (int j = 0; j < bh; j++) {
                    Tile t = tiles.getn(startX + i, startY + j);
                    if (t.block() != Blocks.air || (t.floor() instanceof Floor && ((Floor) t.floor()).liquidDrop != null)) {
                        canPlace = false;
                        break;
                    }
                }
                if (!canPlace) break;
            }

            if (canPlace) {
                for (Stile stile : bp.tiles) {
                    int tx = startX + stile.x;
                    int ty = startY + stile.y;
                    Tile t = tiles.getn(tx, ty);

                    if (t != null) {
                        if (stile.block != null && stile.block != Blocks.air) {
                            t.setBlock(stile.block, Team.derelict, stile.rotation);
                            if (t.build != null) {
                                t.build.rotation = stile.rotation;
                                t.build.health = t.build.maxHealth * rand.random(0.2f, 1f);
                            }
                        }
                    }
                }
                placed++;
            }
        }
    }

    /* ================= 发射规则：必须建造核心发射器 ================= */

    private final IntSet launchReach = new IntSet();
    private Planet launchCachePlanet;
    private float launchCacheTime = -10000f;

    /**
     * 星图里哪些区块可以被“发射过去”。
     * 原版只允许与已占领区块相邻的区块，这里额外允许被发射器射程覆盖的区块。
     */
    @Override
    public boolean allowLanding(Sector sector){
        if(super.allowLanding(sector)) return true;
        if(!sector.planet.allowLaunchToNumbered) return false;

        if(launchCachePlanet != sector.planet || Time.time - launchCacheTime > 30f){
            rebuildLaunchReach(sector.planet);
        }
        return launchReach.contains(sector.id);
    }

    private void rebuildLaunchReach(Planet p){
        launchCachePlanet = p;
        launchCacheTime = Time.time;
        launchReach.clear();
        if(p == null) return;

        for(Sector src : p.sectors){
            if(!src.hasBase()) continue;

            CoreLauncher launcher = CoreLaunchers.byLevel(CoreLaunchers.level(src));
            if(launcher == null) continue;

            Block core = CoreLaunchers.bestCore(src);
            if(core != null && core.size > launcher.maxCoreSize) continue;

            if(launcher.sectorRange <= 0){
                for(Sector dest : p.sectors){
                    if(dest != src) launchReach.add(dest.id);
                }
                continue;
            }

            ObjectSet<Sector> seen = new ObjectSet<>();
            Seq<Sector> frontier = new Seq<>();
            seen.add(src);
            frontier.add(src);

            for(int depth = 1; depth <= launcher.sectorRange; depth++){
                Seq<Sector> next = new Seq<>();
                for(Sector s : frontier){
                    for(Sector n : s.near()){
                        if(n == null || seen.contains(n)) continue;
                        seen.add(n);
                        next.add(n);
                        launchReach.add(n.id);
                    }
                }
                if(next.isEmpty()) break;
                frontier = next;
            }
        }
    }

    /** 发射来源：优先当前正在玩的区块，其次找射程内最近的、装有发射器的区块。 */
    @Override
    public Sector findLaunchCandidate(Sector destination, @Nullable Sector selected){
        if(selected != null && selected.planet == destination.planet && CoreLaunchers.canLaunch(selected, destination)){
            return selected;
        }

        Sector best = null;
        float bestDist = Float.MAX_VALUE;

        for(Sector s : destination.planet.sectors){
            if(s == destination || !s.hasBase()) continue;
            if(!CoreLaunchers.canLaunch(s, destination)) continue;

            float dist = s.tile.v.dst2(destination.tile.v);
            if(best == null || dist < bestDist){
                best = s;
                bestDist = dist;
            }
        }
        return best;
    }
}