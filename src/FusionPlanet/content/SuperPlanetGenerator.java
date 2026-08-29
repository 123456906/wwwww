package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
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

import static FusionPlanet.content.Fblocks.*;
import static FusionPlanet.content.Fblocks.indigoSand;
import static mindustry.Vars.*;

/**
 * 宇宙无敌超级大霹力星 地形生成器
 * 主色调：蓝、紫、青
 * 风格：生机勃勃的外星生命世界
 * 
 * 设计思路：
 * - 使用 7x7 地形表，由纬度和高度决定基础方块
 * - 叠加噪声扰动，增加自然过渡
 * - 包含水域、低地、高地、极地、特殊地形
 */
public class SuperPlanetGenerator extends PlanetGenerator {

    // ---------- 基础参数 ----------
    public float heightScale = 2.5f;      // 地形起伏幅度
    public float waterLevel = 0.12f;      // 水位线（决定水域范围）
    public float iceLevel = 0.55f;        // 冰原出现的海拔阈值
    public float mossLevel = 0.35f;       // 苔藓出现的海拔上限

    // ---------- 地形表 (7行 x 7列) ----------
    // 行索引：纬度 (0=赤道, 6=极地)
    // 列索引：高度 (0=最低, 6=最高)
    // 地形材质：蓝/紫/青主题
    private final Block[][] terrainTable = {
            // 赤道附近 (低纬度)
            {cyanWater, cyanWater, indigoSand, indigoSand, blueGrass, blueGrass, purpleStone},
            {cyanWater, indigoSand, indigoSand, blueGrass, blueGrass, purpleStone, purpleStone},
            {indigoSand, indigoSand, blueGrass, blueGrass, purpleStone, purpleStone, magentaMoss},
            {indigoSand, blueGrass, blueGrass, purpleStone, purpleStone, magentaMoss, magentaMoss},
            {blueGrass, blueGrass, purpleStone, purpleStone, magentaMoss, magentaMoss, indigoIce},
            {blueGrass, purpleStone, purpleStone, magentaMoss, indigoIce, indigoIce, indigoIce},
            // 极地 (高纬度)
            {purpleStone, magentaMoss, indigoIce, indigoIce, indigoIce, indigoIce, indigoIce}
    };

    // ---------- 装饰映射 ----------
    // 根据地板类型放置装饰块（比如草地上的花、石头上的晶体等）
    private final ObjectMap<Floor, Block> decorMap = new ObjectMap<>();

    public SuperPlanetGenerator() {
        // 映射装饰
        // 这里可自定义，此处示例
        decorMap.put(Fblocks.blueGrass, Fblocks.glowSpore);
        // 暂不添加更多，可后续扩展
    }

    // ---------- 核心高度计算 ----------
    private float rawHeight(Vec3 pos) {
        float h = Simplex.noise3d(seed, 8, 0.6f, 0.25f,
                pos.x * heightScale,
                pos.y * heightScale + 10,
                pos.z * heightScale);
        // 增加一些起伏细节
        float detail = Simplex.noise3d(seed + 1, 4, 0.5f, 0.5f,
                pos.x * heightScale * 1.5,
                pos.y * heightScale * 1.5 + 20,
                pos.z * heightScale * 1.5) * 0.2f;
        return Mathf.clamp(h * 0.5f + 0.5f + detail);
    }

    @Override
    public float getHeight(Vec3 pos) {
        float h = rawHeight(pos);
        // 保证至少为水位线，避免陆地完全淹没
        return Math.max(h, waterLevel * 0.8f);
    }

    // ---------- 方块选择 ----------
    private Block getBlock(Vec3 pos) {
        float h = rawHeight(pos);
        // 纬度因子：0=赤道，1=极地
        float lat = Mathf.clamp(Math.abs(pos.y) * 1.8f);
        // 高度因子：0~1
        float height = Mathf.clamp(h * 1.1f);

        // 根据纬度和高度查表
        int row = (int) (lat * (terrainTable.length - 1));
        int col = (int) (height * (terrainTable[0].length - 1));
        row = Mathf.clamp(row, 0, terrainTable.length - 1);
        col = Mathf.clamp(col, 0, terrainTable[0].length - 1);

        Block block = terrainTable[row][col];

        // ---- 特殊规则 ----
        // 如果高度超过冰原阈值且纬度较高，替换为冰
        if (h > iceLevel && lat > 0.5f) {
            block = Fblocks.indigoIce;
        }
        // 如果高度在 mossLevel 以下且地势低洼，可能变成苔藓
        if (h < mossLevel && h > waterLevel + 0.02f && block == Fblocks.blueGrass) {
            // 加入随机扰动，使苔藓呈斑块分布
            float mossNoise = Simplex.noise3d(seed + 3, 3, 0.5f, 0.3f,
                    pos.x * 3, pos.y * 3 + 5, pos.z * 3);
            if (mossNoise > 0.2f) {
                block = Fblocks.magentaMoss;
            }
        }
        // 在低洼沿海区域生成沙地
        if (h < waterLevel + 0.03f && h > waterLevel - 0.02f) {
            block = indigoSand;
        }

        return block;
    }

    @Override
    public void getColor(Vec3 pos, Color out) {
        Block block = getBlock(pos);
        // 对于自定义方块，直接使用其 mapColor
        out.set(block.mapColor);
        // 增加亮度变化模拟光照
        float light = 0.9f + 0.1f * Simplex.noise3d(seed + 99, 2, 0.4f, 0.1f,
                pos.x * 2, pos.y * 2 + 5, pos.z * 2);
        out.mul(light);
        out.a = 1f;
    }

    @Override
    public void genTile(Vec3 pos, TileGen tile) {
        Block floor = getBlock(pos);
        tile.floor = floor;

        // 对于水域，不放置墙壁
        if (floor == cyanWater) {
            tile.block = Blocks.air;
            return;
        }

        // 墙壁生成（使用地板对应的墙壁）
        Block wall = floor.asFloor().wall;
        if (wall != null && wall != Blocks.air) {
            tile.block = wall;
            // 随机移除一些墙壁，增加自然感
            if (rand.chance(0.2f)) {
                tile.block = Blocks.air;
            }
        } else {
            tile.block = Blocks.air;
        }

        // 装饰生成（在草地上放置孢子/花）
        if (floor == Fblocks.blueGrass && rand.chance(0.03f)) {
            // 如果装饰块存在，放置
            Block deco = null;
            if (deco != null && tile.block == Blocks.air) {
                tile.block = deco;
            }
        }
    }

    @Override
    public void postGenerate(Tiles tiles) {
        // 可添加额外处理，如放置核心、设置规则等
        if (tiles == null) return;
        int w = tiles.width, h = tiles.height;
        int cx = w / 2, cy = h / 2;

        // 简单放置核心在地图中心（实际应寻找合适位置）
        // 这里仅做演示，更复杂的逻辑可参考 Serpulo
        Schematics.placeLaunchLoadout(cx, cy);

        // 设置波次规则
        state.rules.waves = true;
        state.rules.waveSpacing = 60 * 60 * 2;
        state.rules.env = Env.terrestrial;
        state.rules.winWave = 15;
        state.rules.placeRangeCheck = true;
        state.rules.attackMode = false;
    }

    @Override
    public float getSizeScl() {
        return 2500f; // 区块大小
    }
}