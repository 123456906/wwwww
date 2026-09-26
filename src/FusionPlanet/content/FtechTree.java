package FusionPlanet.content;

import arc.util.Log;
import mindustry.content.Items;
import mindustry.content.Planets;
import mindustry.content.TechTree;
import mindustry.game.EventType.ContentInitEvent;
import arc.Events;
import mindustry.type.ItemStack;

import java.lang.reflect.Field;

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

        // ========== superPlanet 共享 serpulo 科技树 + 解锁 f 星的节点 ==========
        Events.on(ContentInitEvent.class, e -> {
            if (Planets.serpulo == null || Planets.serpulo.techTree == null) {
                Log.err("[FtechTree] serpulo techTree is null, cannot attach unlock node.");
                return;
            }

            // superPlanet 直接复用 serpulo 的科技树
            superPlanet.techTree = Planets.serpulo.techTree;

            // 用反射把 context 指向 serpulo 的根节点，然后用官方 API 添加节点
            try {
                Field contextField = TechTree.class.getDeclaredField("context");
                contextField.setAccessible(true);

                Object oldContext = contextField.get(null);
                contextField.set(null, Planets.serpulo.techTree);

                TechTree.node(fusionPlanetUnlock, ItemStack.with(
                        copper, 1000,
                        lead, 1000,
                        titanium, 1000, 
                        silicon, 1000
                ), () -> {});

                contextField.set(null, oldContext);
                Log.info("[FtechTree] Added fusionPlanet unlock node to serpulo/super tech tree.");
            } catch (Exception ex) {
                Log.err("[FtechTree] Reflection failed: " + ex);
            }
        });
    }
}