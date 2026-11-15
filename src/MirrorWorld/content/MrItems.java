package MirrorWorld.content;

import arc.graphics.Color;
import mindustry.content.StatusEffects;
import mindustry.type.Item;
import mindustry.type.Liquid;

/**
 * 镜潮（Mirror Tide）物品与流体表。
 *
 * 题材：棱晶、镜面、回声、深渊。命名沿用原版 kebab-case 规则。
 *
 * 原矿（5）  mirror-shard / prism-crystal / lumen-crystal / void-ore / abyss-ore
 * 加工（10） facet-plate / mirror-alloy / lumen-thread / prism-lens / echo-chip
 *            phase-glass / veil-fiber / resonance-cell / resonant-alloy / abyss-core
 * 流体（7）  mirror-fluid / lumen-solution / cryo-prism / void-essence
 *            molten-mirror / echo-plasma / phase-solvent
 *
 * 说明：本表只定义内容。MrBlocks 已把这些物品/流体接入建造需求、炮塔弹药、
 * 工厂配方与单位工厂计划；MrItems.load() 带幂等保护，MrBlocks.load() 会自动调用。
 * 缺少贴图时游戏会使用占位图标，流体缺少贴图时同样会显示占位。
 */
public class MrItems {

    /* ================= 原矿 ================= */
    public static Item mirrorShard, prismCrystal, lumenCrystal, voidOre, abyssOre;

    /* ================= 加工材料 ================= */
    public static Item facetPlate, mirrorAlloy, lumenThread, prismLens, echoChip,
            phaseGlass, veilFiber, resonanceCell, resonantAlloy, abyssCore;

    /* ================= 流体 ================= */
    public static Liquid mirrorFluid, lumenSolution, cryoPrism, voidEssence,
            moltenMirror, echoPlasma, phaseSolvent;

    private static boolean loaded;

    public static void load() {
        if (loaded) return;
        loaded = true;

        /* ============================================================
         *  原矿
         * ============================================================ */

        // 镜屑：最常见的浅色晶屑，硬度很低，用回声钻头即可开采。
        mirrorShard = new Item("mirror-shard", Color.valueOf("cdeeff")) {{
            hardness = 1;
            cost = 0.7f;
            healthScaling = 0.1f;
            flammability = 0f;
        }};

        // 棱晶：带轻微电荷的紫色晶体，需要棱晶钻头。
        prismCrystal = new Item("prism-crystal", Color.valueOf("b39dff")) {{
            hardness = 2;
            cost = 1.0f;
            charge = 0.1f;
            healthScaling = 0.3f;
        }};

        // 流明晶：会自发微光的淡黄晶体，需要流明钻头。
        lumenCrystal = new Item("lumen-crystal", Color.valueOf("f4ffb0")) {{
            hardness = 3;
            cost = 1.4f;
            charge = 0.25f;
            healthScaling = 0.4f;
        }};

        // 虚空矿：深紫色的高密度矿石，带弱放射性，需要深渊钻头。
        voidOre = new Item("void-ore", Color.valueOf("6a4a8a")) {{
            hardness = 4;
            cost = 2.0f;
            radioactivity = 0.4f;
            explosiveness = 0.1f;
            healthScaling = 0.6f;
        }};

        // 深渊矿：镜潮矿脉最深处的产物，需要镜面钻头。
        abyssOre = new Item("abyss-ore", Color.valueOf("4a2a5a")) {{
            hardness = 5;
            cost = 3.0f;
            radioactivity = 0.8f;
            explosiveness = 0.2f;
            healthScaling = 1.0f;
        }};

        /* ============================================================
         *  加工材料
         * ============================================================ */

        // 棱面板：最基础的板材，替代铜/铅板在镜潮建筑中的位置。
        facetPlate = new Item("facet-plate", Color.valueOf("9db9c9")) {{
            cost = 0.6f;
            healthScaling = 0.5f;
        }};

        // 镜合金：中期的通用结构材料，轻且韧。
        mirrorAlloy = new Item("mirror-alloy", Color.valueOf("8fd8e8")) {{
            cost = 1.3f;
            healthScaling = 1.2f;
        }};

        // 流明丝：导光又导电的细丝，用于电子与炮塔。
        lumenThread = new Item("lumen-thread", Color.valueOf("d8ffb0")) {{
            cost = 1.1f;
            charge = 0.4f;
            healthScaling = 0.8f;
        }};

        // 棱透镜：聚焦光束的透镜，激光炮塔的核心部件。
        prismLens = new Item("prism-lens", Color.valueOf("a8d8ff")) {{
            cost = 1.2f;
            charge = 0.2f;
            healthScaling = 1.0f;
        }};

        // 回声芯片：记录与重放射击信号的芯片，用于高级瞄准与召唤设备。
        echoChip = new Item("echo-chip", Color.valueOf("e0c8ff")) {{
            cost = 1.5f;
            charge = 0.6f;
            flammability = 0.05f;
            healthScaling = 1.2f;
        }};

        // 相位玻璃：半透明的相变玻璃，可吸收激光。
        phaseGlass = new Item("phase-glass", Color.valueOf("7fe8d8")) {{
            cost = 1.4f;
            healthScaling = 1.4f;
        }};

        // 帷幕纤维：极细的相位纤维，用于护盾与维修设备。
        veilFiber = new Item("veil-fiber", Color.valueOf("c8b0e8")) {{
            cost = 1.8f;
            flammability = 0.1f;
            healthScaling = 1.6f;
        }};

        // 共振单元：把电力压缩进晶体谐振腔的储能单元。
        resonanceCell = new Item("resonance-cell", Color.valueOf("ff9de0")) {{
            cost = 2.2f;
            charge = 1.0f;
            explosiveness = 0.3f;
            healthScaling = 1.8f;
        }};

        // 共振合金：后期主力合金，兼顾强度与导能。
        resonantAlloy = new Item("resonant-alloy", Color.valueOf("ffd8a0")) {{
            cost = 2.8f;
            charge = 1.4f;
            healthScaling = 2.2f;
        }};

        // 深渊核心：镜潮科技的终点材料，带强放射性与不稳定性。
        abyssCore = new Item("abyss-core", Color.valueOf("4a2a6a")) {{
            cost = 4.0f;
            radioactivity = 1.2f;
            explosiveness = 0.6f;
            healthScaling = 3.0f;
        }};

        /* ============================================================
         *  流体
         * ============================================================ */

        // 镜液：清澈的浅蓝冷却液，性质接近水但导热更好。
        mirrorFluid = new Liquid("mirror-fluid", Color.valueOf("9fd8ff")) {{
            heatCapacity = 0.65f;
            viscosity = 0.35f;
            temperature = 0.42f;
            coolant = true;
            lightColor = Color.valueOf("9fd8ff").a(0.25f);
        }};

        // 流明溶液：带荧光的浅绿液体，可作冷却液，也可参与发光材料合成。
        lumenSolution = new Liquid("lumen-solution", Color.valueOf("d8ff9f")) {{
            heatCapacity = 0.55f;
            viscosity = 0.4f;
            temperature = 0.5f;
            flammability = 0.15f;
            coolant = true;
            lightColor = Color.valueOf("e8ffb0").a(0.35f);
        }};

        // 霜棱液：极低温冷却液，替代原版冷冻液。
        cryoPrism = new Liquid("cryo-prism", Color.valueOf("b8f4ff")) {{
            heatCapacity = 0.95f;
            viscosity = 0.35f;
            temperature = 0.08f;
            coolant = true;
            lightColor = Color.valueOf("b8f4ff").a(0.3f);
        }};

        // 虚空精粹：深紫色的高能液体，不稳定且易燃易爆。
        voidEssence = new Liquid("void-essence", Color.valueOf("6a3a9a")) {{
            heatCapacity = 0.7f;
            viscosity = 0.6f;
            temperature = 0.35f;
            explosiveness = 0.45f;
            coolant = true;
            blockReactive = false;
            lightColor = Color.valueOf("9a5ad8").a(0.3f);
            effect = StatusEffects.none;
        }};

        // 熔镜：熔融态的镜质材料，温度极高，不能作为冷却液。
        moltenMirror = new Liquid("molten-mirror", Color.valueOf("ffb066")) {{
            heatCapacity = 0.45f;
            viscosity = 0.75f;
            temperature = 1f;
            flammability = 0.2f;
            explosiveness = 0.1f;
            coolant = false;
            lightColor = Color.valueOf("ffb066").a(0.4f);
            effect = StatusEffects.melting;
        }};

        // 回声等离子：气态流体，不形成液洼，用于终局反应堆与相位设备。
        echoPlasma = new Liquid("echo-plasma", Color.valueOf("e0d0ff")) {{
            gas = true;
            gasColor = Color.valueOf("e8e0ff");
            heatCapacity = 0.3f;
            viscosity = 0.1f;
            temperature = 0.9f;
            explosiveness = 0.25f;
            coolant = false;
            lightColor = Color.valueOf("e0d0ff").a(0.45f);
            effect = StatusEffects.shocked;
        }};

        // 相位溶剂：溶解相位纤维的淡蓝溶剂，可冷却，不与方块反应。
        phaseSolvent = new Liquid("phase-solvent", Color.valueOf("8fa8ff")) {{
            heatCapacity = 0.6f;
            viscosity = 0.3f;
            temperature = 0.3f;
            flammability = 0.05f;
            coolant = true;
            blockReactive = false;
            lightColor = Color.valueOf("8fa8ff").a(0.25f);
        }};
    }
}
