package FusionPlanet.content;

import mindustry.content.Items;
import mindustry.content.TechTree;
import mindustry.type.ItemStack;

import static FusionPlanet.content.Fblocks.*;
import static FusionPlanet.content.Funits.*;
import static mindustry.content.Items.*;

public class FtechTree {
    public static void load() {
        TechTree.nodeRoot("fusion-planet", coreEvoke, () -> {

            TechTree.node(coreEvoke, () -> {
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
    }
}