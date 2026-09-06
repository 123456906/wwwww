package FusionPlanet.content;

import mindustry.content.Items;
import mindustry.content.TechTree;
import mindustry.type.ItemStack;
import mindustry.type.SectorPreset;

import static FusionPlanet.content.Fblocks.*;
import static FusionPlanet.content.Funits.*;
import static FusionPlanet.content.fPlanets.*;
import static mindustry.content.Items.*;

public class FtechTree {
    public static void load() {
        TechTree.nodeRoot("fusion-planet", coreEvoke, () -> {

            // 原有子节点（方块、单位）
            TechTree.node(assimilator, ItemStack.with(
                    copper, 2000,
                    lead, 1500,
                    Items.silicon, 1000
            ), () -> {});

            TechTree.node(falcon, ItemStack.with(
                    copper, 3000,
                    lead, 2000,
                    Items.silicon, 2000,
                    Items.titanium, 800
            ), () -> {});

            TechTree.node(summonTurret, ItemStack.with(
                    copper, 5000,
                    lead, 3500,
                    Items.silicon, 2500,
                    Items.titanium, 1500,
                    Items.graphite, 1000
            ), () -> {});

            // 关卡链：核心 → 未开发区 → 废弃渡口 → 菌丝壁垒，每个需 1000 铜
            TechTree.node(undevelopedZone, ItemStack.with(copper, 1000), () -> {
                TechTree.node(abandonedFerry, ItemStack.with(copper, 1000), () -> {
                    TechTree.node(myceliumBastion, ItemStack.with(copper, 1000), () -> {});
                });
            });

            // 物品生产节点
            TechTree.nodeProduce(copper, () -> {});
            TechTree.nodeProduce(lead, () -> {});
            TechTree.nodeProduce(sand, () -> {});
            TechTree.nodeProduce(coal, () -> {});
            TechTree.nodeProduce(titanium, () -> {});
            TechTree.nodeProduce(thorium, () -> {});
            TechTree.nodeProduce(silicon, () -> {});
            TechTree.nodeProduce(graphite, () -> {});
            TechTree.nodeProduce(metaglass, () -> {});
            TechTree.nodeProduce(pyratite, () -> {});
            TechTree.nodeProduce(blastCompound, () -> {});
            TechTree.nodeProduce(sporePod, () -> {});
            TechTree.nodeProduce(plastanium, () -> {});
            TechTree.nodeProduce(phaseFabric, () -> {});
            TechTree.nodeProduce(surgeAlloy, () -> {});
            TechTree.nodeProduce(scrap, () -> {});
            TechTree.nodeProduce(beryllium, () -> {});
            TechTree.nodeProduce(tungsten, () -> {});
            TechTree.nodeProduce(oxide, () -> {});
            TechTree.nodeProduce(carbide, () -> {});
        });
    }
}