package FusionPlanet.content;

import mindustry.content.TechTree;
import mindustry.type.ItemStack;

import static FusionPlanet.content.Fblocks.*;
import static FusionPlanet.content.fPlanets.*;
import static mindustry.content.Items.*;

public class FtechTree {
    public static void load() {
        fPlanets.fusionPlanet.techTree = TechTree.nodeRoot("fusion-planet", coreEvoke, () -> {

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