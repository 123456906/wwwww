package MirrorWorld.content;

import arc.graphics.Color;
import arc.math.geom.Vec3;
import arc.struct.ObjectSet;
import arc.util.Timer;
import arc.util.noise.Simplex;
import mindustry.content.StatusEffects;
import mindustry.gen.Groups;
import mindustry.graphics.CacheLayer;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.ShallowLiquid;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.environment.TreeBlock;
import mindustry.type.Item;
import mindustry.type.StatusEffect;

import static MirrorWorld.content.MrItems.*;
import static mindustry.Vars.world;

public class AntichthonBlocks {
    public static Liquid liquidCO2;

    public static Floor co2Shallow, co2Deep;

    /* ================= 自然矿物地板（OreBlock：可被钻头开采） ================= */
    public static OreBlock oreMirrorShard, orePrismCrystal, oreLumenCrystal, oreVoid, oreAbyss;

    /* ================= 镜潮流体湖（液体地板，泵可取液） ================= */
    public static Floor mirrorShallow, mirrorDeep;
    public static Floor lumenShallow, lumenDeep;
    public static Floor voidShallow, voidDeep;
    public static Floor moltenShallow, moltenDeep;
    public static Floor phaseShallow, phaseDeep;
    public static Floor cryoShallow, cryoDeep;

    public static Floor redSand, darkRedSand, rustyGravel, redRock, hematiteRock;
    public static Floor dryIce, deepDryIce, paleMoss, deepPaleMoss, olivineSand, lichen;

    /* ================= 绯红白杆菌 ================= */
    public static CrimsonFloor crimsonCarpet, whiteStalkCarpet;
    public static StaticWall crimsonCarpetWall, whiteStalkCarpetWall;
    /** 第二种墙壁生成用的墙：绯红脉墙、绯红菌簇墙 */
    public static StaticWall crimsonVeinWall, crimsonBloomWall;

    /* ================= 装饰 ================= */
    public static Prop crimsonMushroomCluster, whiteStalkCluster, crimsonRock, paleRock, giantMushroomStump;
    /** 巨型蘑菇：沿用原版松树的 TreeBlock 类型 */
    public static TreeBlock giantMushroom;

    /* ================= 地形伤害参数（运行时可改） ================= */
    public static float crimsonDamage = 10f;
    public static float crimsonWallDamage = 10f;
    public static float crimsonDamageInterval = 2f;

    /** 算作“会伤人的墙面”的方块；默认只认绯红菌系，避免全图贴石头都掉血 */
    public static final ObjectSet<Block> crimsonWalls = new ObjectSet<>();

    /** 带伤害属性的地板 */
    public static class CrimsonFloor extends Floor {
        public float buildDamage = 10f;
        public CrimsonFloor(String name){
            super(name);
        }
    }

    private static boolean damageTaskReady = false;

    public static void load() {
        // 地形里的液体地板要引用 MrItems 的流体，先确保物品/流体已创建（幂等）
        MrItems.load();

        liquidCO2 = new Liquid("liquid-co2", Color.valueOf("6773D6")) {{
            heatCapacity = 0.6f;
            temperature = 0.1f;
            effect = StatusEffects.freezing;
            alwaysUnlocked = true;
            boilPoint = 0.85f;
        }};

        co2Shallow = new ShallowLiquid("co2-shallow") {{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            liquidDrop = liquidCO2;
            variants = 0;
            mapColor.set(Color.valueOf("6773D680"));
        }};

        co2Deep = new Floor("co2-deep") {{
            speedMultiplier = 0.18f;
            variants = 0;
            status = StatusEffects.freezing;
            statusDuration = 140f;
            drownTime = 200f;
            liquidDrop = liquidCO2;
            liquidMultiplier = 1.5f;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("4E569D80"));
        }};

        /* ================= 自然矿物地板 ================= */
        oreMirrorShard = ore("ore-mirror-shard", mirrorShard, 0.80f, 24f);
        orePrismCrystal = ore("ore-prism-crystal", prismCrystal, 0.82f, 26f);
        oreLumenCrystal = ore("ore-lumen-crystal", lumenCrystal, 0.84f, 28f);
        oreVoid = ore("ore-void", voidOre, 0.86f, 30f);
        oreAbyss = ore("ore-abyss", abyssOre, 0.88f, 32f);

        /* ================= 镜潮流体湖 ================= */
        mirrorDeep = deepLiquid("mirror-fluid-deep", mirrorFluid, StatusEffects.wet, CacheLayer.water, "5a8ac880");
        mirrorShallow = shallowLiquid("mirror-fluid-shallow", mirrorFluid, CacheLayer.water, "9fd8ff80");

        lumenDeep = deepLiquid("lumen-solution-deep", lumenSolution, StatusEffects.wet, CacheLayer.water, "7a9a4a80");
        lumenShallow = shallowLiquid("lumen-solution-shallow", lumenSolution, CacheLayer.water, "d8ff9f80");

        voidDeep = deepLiquid("void-essence-deep", voidEssence, StatusEffects.wet, CacheLayer.water, "4a2a6a80");
        voidShallow = shallowLiquid("void-essence-shallow", voidEssence, CacheLayer.water, "6a3a9a80");

        moltenDeep = deepLiquid("molten-mirror-deep", moltenMirror, StatusEffects.melting, CacheLayer.slag, "c86a2a80");
        moltenShallow = shallowLiquid("molten-mirror-shallow", moltenMirror, CacheLayer.slag, "ffb06680");

        phaseDeep = deepLiquid("phase-solvent-deep", phaseSolvent, StatusEffects.wet, CacheLayer.water, "4a5a9a80");
        phaseShallow = shallowLiquid("phase-solvent-shallow", phaseSolvent, CacheLayer.water, "8fa8ff80");

        cryoDeep = deepLiquid("cryo-prism-deep", cryoPrism, StatusEffects.freezing, CacheLayer.cryofluid, "6a9aa880");
        cryoShallow = shallowLiquid("cryo-prism-shallow", cryoPrism, CacheLayer.cryofluid, "b8f4ff80");

        redSand = floor("red-sand", "b05a3a", "8a4530", 3);
        darkRedSand = floor("dark-red-sand", "8a3a25", "5a2518", 3);
        rustyGravel = floor("rusty-gravel", "9a5a3a", "6a3a25", 3);
        redRock = floor("red-rock", "6a3025", "4a2018", 2);
        hematiteRock = floor("hematite-rock", "4a1a15", "2a0e08", 2);
        dryIce = floor("dry-ice", "d8e4f0", "a8b8c8", 3);
        deepDryIce = floor("deep-dry-ice", "eef4fc", "c8d8e8", 2);
        paleMoss = floor("pale-moss", "7a8ac8", "5a6a98", 3);
        deepPaleMoss = floor("deep-pale-moss", "5a6a98", "3a4a78", 2);
        olivineSand = floor("olivine-sand", "5a6a4a", "3a4a2a", 2);

        lichen = new Floor("lichen") {{
            mapColor.set(Color.valueOf("a8b8b8"));
            variants = 2;
            albedo = 0.6f;
        }};
        StaticWall lichenWall = new StaticWall("lichen-wall") {{
            mapColor.set(Color.valueOf("889898"));
        }};
        lichen.wall = lichenWall;

        /* ================= 绯红白杆菌 ================= */
        crimsonCarpet = new CrimsonFloor("crimson-carpet") {{
            buildDamage = crimsonDamage;
            mapColor.set(Color.valueOf("8e1f2e"));
            variants = 3;
            supportsOverlay = true;
            speedMultiplier = 0.88f;
            status = StatusEffects.corroded;
            statusDuration = 60f;
        }};
        crimsonCarpetWall = wallFor("crimson-carpet", "5a1018", 3);
        crimsonCarpet.wall = crimsonCarpetWall;

        whiteStalkCarpet = new CrimsonFloor("white-stalk-carpet") {{
            buildDamage = crimsonDamage;
            mapColor.set(Color.valueOf("cfd6de"));
            variants = 3;
            supportsOverlay = true;
            speedMultiplier = 0.94f;
            albedo = 0.55f;
        }};
        whiteStalkCarpetWall = wallFor("white-stalk-carpet", "8a9099", 3);
        whiteStalkCarpet.wall = whiteStalkCarpetWall;

        /* ================= 第二种墙壁生成用的墙 ================= */
        crimsonVeinWall = new StaticWall("crimson-vein-wall") {{
            mapColor.set(Color.valueOf("4a0c14"));
            variants = 3;
        }};
        crimsonWalls.add(crimsonVeinWall);

        crimsonBloomWall = new StaticWall("crimson-bloom-wall") {{
            mapColor.set(Color.valueOf("6b1420"));
            variants = 2;
        }};
        crimsonWalls.add(crimsonBloomWall);

        /* ================= 装饰 ================= */
        crimsonMushroomCluster = new Prop("crimson-mushroom-cluster") {{
            variants = 3;
        }};
        whiteStalkCluster = new Prop("white-stalk-cluster") {{
            variants = 3;
        }};
        crimsonRock = new Prop("crimson-rock") {{
            variants = 3;
        }};
        paleRock = new Prop("pale-rock") {{
            variants = 2;
        }};
        giantMushroomStump = new Prop("giant-mushroom-stump") {{
            variants = 2;
        }};

        giantMushroom = new TreeBlock("giant-crimson-mushroom");

        setupCrimsonDamage();
    }

    /* ================= 第二种墙壁生成（与黑暗度并存） ================= */

    /**
     * 峰脊墙：把 Simplex 折叠成 1 - |2n-1| 取脊线，长成细长蜿蜒的菌索墙脉。
     * threshold 越大墙越少，建议 0.82~0.92。
     * 纯函数：同一 position + seed 永远同一结果，区块重生成不变形。
     */
    public static boolean veinWall(Vec3 p, int seed, float scale, float threshold){
        float n = Simplex.noise3d(seed, 4, 0.55f, 1f / 3f, p.x * scale, p.y * scale, p.z * scale);
        float ridge = 1f - Math.abs(n * 2f - 1f);
        return ridge > threshold;
    }

    /** 团块墙：分形噪声阈值，得到成片的封闭菌室。建议 0.70~0.80 */
    public static boolean blobWall(Vec3 p, int seed, float scale, float threshold){
        return Simplex.noise3d(seed, 5, 0.5f, 1f / 2f, p.x * scale, p.y * scale, p.z * scale) > threshold;
    }

    /** 一步到位：返回这一格该盖哪种新墙，null 表示不盖 */
    public static Block pickWall(Vec3 p, int seed){
        if(veinWall(p, seed + 41, 9f, 0.86f)) return crimsonVeinWall;
        if(blobWall(p, seed + 77, 6f, 0.74f)) return crimsonBloomWall;
        return null;
    }

    /** 装饰散布判定：独立 seed 通道，不和墙体图案打架 */
    public static boolean scatter(Vec3 p, int seed, float scale, float threshold){
        return Simplex.noise3d(seed, 3, 0.6f, 1f / 4f, p.x * scale, p.y * scale, p.z * scale) > threshold;
    }

    /* ================= 地形伤害结算 ================= */

    /**
     * 每 crimsonDamageInterval 秒结算一次：
     *   1) 建筑脚下是 CrimsonFloor → 掉 crimsonDamage
     *   2) 建筑四邻有绯红菌墙   → 掉 crimsonWallDamage
     * 两者不叠加（取较大一档），避免贴墙站在地毯上瞬间暴死。
     */
    public static void setupCrimsonDamage(){
        if(damageTaskReady) return;
        damageTaskReady = true;
        Timer.schedule(AntichthonBlocks::tickCrimsonDamage, crimsonDamageInterval, crimsonDamageInterval);
    }

    private static void tickCrimsonDamage(){
        if(world == null) return;
        Groups.build.each(b -> {
            if(b == null || !b.isValid()) return;

            float dmg = 0f;

            Block floorBlock = b.tile.floor();
            if(floorBlock instanceof CrimsonFloor){
                dmg = ((CrimsonFloor) floorBlock).buildDamage;
            }
            if(dmg <= 0f && touchesCrimsonWall(b.tile)){
                dmg = crimsonWallDamage;
            }

            if(dmg > 0f){
                b.damage(dmg);
            }
        });
    }

    private static boolean touchesCrimsonWall(Tile t){
        for(int i = 0; i < 4; i++){
            int dx = i == 0 ? 1 : (i == 1 ? -1 : 0);
            int dy = i == 2 ? 1 : (i == 3 ? -1 : 0);
            Tile o = world.tile(t.x + dx, t.y + dy);
            if(o == null) continue;
            Block bl = o.block();
            if(bl != null && crimsonWalls.contains(bl)) return true;
        }
        return false;
    }

    /* ================= 工具 ================= */

    private static Floor floor(String name, String color, String wallColor, int variants){
        Floor floor = new Floor(name);
        floor.mapColor.set(Color.valueOf(color));
        floor.variants = variants;
        floor.supportsOverlay = true;

        StaticWall wall = new StaticWall(name + "-wall");
        wall.mapColor.set(Color.valueOf(wallColor));
        wall.variants = variants;
        floor.wall = wall;
        return floor;
    }

    private static StaticWall wallFor(String name, String color, int variants){
        StaticWall wall = new StaticWall(name + "-wall");
        wall.mapColor.set(Color.valueOf(color));
        wall.variants = variants;
        crimsonWalls.add(wall);
        return wall;
    }

    /** 自然矿物地板：只由本星球的生成器放置，不参与其它星球的 oreDefault 生成。 */
    private static OreBlock ore(String name, Item item, float threshold, float scale){
        OreBlock ore = new OreBlock(name, item);
        ore.oreDefault = false;
        ore.oreThreshold = threshold;
        ore.oreScale = scale;
        return ore;
    }

    /** 深液湖：会淹死单位，泵可以抽取 liquidDrop。 */
    private static Floor deepLiquid(String name, Liquid liquid, StatusEffect status, CacheLayer layer, String color){
        Floor floor = new Floor(name);
        floor.speedMultiplier = 0.18f;
        floor.variants = 0;
        floor.status = status;
        floor.statusDuration = 140f;
        floor.drownTime = 200f;
        floor.liquidDrop = liquid;
        floor.liquidMultiplier = 1.5f;
        floor.isLiquid = true;
        floor.cacheLayer = layer;
        floor.albedo = 0.9f;
        floor.supportsOverlay = true;
        floor.mapColor.set(Color.valueOf(color));
        return floor;
    }

    /** 浅液湖：可以涉水通过，同样带 liquidDrop，泵能抽。 */
    private static ShallowLiquid shallowLiquid(String name, Liquid liquid, CacheLayer layer, String color){
        ShallowLiquid floor = new ShallowLiquid(name);
        floor.speedMultiplier = 0.8f;
        floor.variants = 0;
        floor.statusDuration = 50f;
        floor.liquidDrop = liquid;
        floor.isLiquid = true;
        floor.shallow = true;
        floor.cacheLayer = layer;
        floor.albedo = 0.9f;
        floor.supportsOverlay = true;
        floor.mapColor.set(Color.valueOf(color));
        return floor;
    }
}