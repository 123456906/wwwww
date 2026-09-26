package FusionPlanet.content;

import arc.Events;
import arc.util.Log;
import mindustry.content.Items;
import mindustry.content.Planets;
import mindustry.content.TechTree;
import mindustry.content.TechTree.TechNode;
import mindustry.game.EventType.ContentInitEvent;
import mindustry.type.ItemStack;

import static FusionPlanet.content.Fblocks.*;
import static FusionPlanet.content.Funits.*;
import static FusionPlanet.content.fPlanets.*;
import static mindustry.content.Items.*;

public class FtechTree {
    public static void load() {
        // ========== fusionPlanet 自己的科技树 ==========
        fusionPlanet.techTree = TechTree.nodeRoot("fusion-planet", coreEvoke, () -> {

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
                    copper, 500,
                    lead, 350,
                    Items.silicon, 250,
                    Items.titanium, 150,
                    Items.graphite, 100
            ), () -> {});

            TechTree.node(undevelopedZone, ItemStack.with(copper, 1000), () -> {
                TechTree.node(abandonedFerry, ItemStack.with(copper, 1000), () -> {
                    TechTree.node(myceliumBastion, ItemStack.with(copper, 1000), () -> {});
                });
            });

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

        // ========== superPlanet 独立科技树 ==========
        Events.on(ContentInitEvent.class, e -> {
            if (Planets.serpulo == null || Planets.serpulo.techTree == null) {
                Log.err("[FtechTree] serpulo techTree null, cannot build superPlanet tree");
                return;
            }

            // 1. 根节点：fusionPlanetUnlock，必须 requiresUnlock = true
            TechNode superRoot = new TechNode(null, fusionPlanetUnlock,
                    ItemStack.with(copper, 1000, lead, 1000, titanium, 1000, silicon, 1000));
            superRoot.requiresUnlock = true;

            // 2. 深拷贝赛普罗整棵科技树，挂到 superRoot 下
            deepCopyNode(Planets.serpulo.techTree, superRoot);

            // 3. 赋给 superPlanet
            superPlanet.techTree = superRoot;

            Log.info("[FtechTree] superPlanet techTree built, root = fusionPlanetUnlock");
        });
    }

    private static TechNode deepCopyNode(TechNode src, TechNode parent) {
        ItemStack[] reqs = src.requirements != null
                ? src.requirements.clone()
                : new ItemStack[0];
        TechNode copy = new TechNode(parent, src.content, reqs);
        copy.requiresUnlock = src.requiresUnlock;
        for (TechNode child : src.children) {
            deepCopyNode(child, copy);
        }
        return copy;
    }
}