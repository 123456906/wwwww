package FusionPlanet.content;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

public class Fblocks {
    public static Block coreEvoke;

    public static void load() {
        coreEvoke = new CoreBlock("core-evoke") {{
            size = 4;
            squareSprite = false;
            health = 3600;
            itemCapacity = 4000;
            unitType = UnitTypes.evoke;
            requirements(Category.effect, ItemStack.with(
                    Items.copper, 5000,
                    Items.lead, 3000,
                    Items.titanium, 1500
            ));
            alwaysUnlocked = true;
//            buildCost = 5f;
            requirements = ItemStack.with(
                    Items.copper, 100,
                    Items.lead, 50,
                    Items.silicon, 25,
                    Items.graphite, 20
            );
        }};
    }
}
