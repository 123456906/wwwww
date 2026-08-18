package FusionPlanet;

import FusionPlanet.content.fPlanets;
import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.ctype.UnlockableContent;
import mindustry.Vars;
import mindustry.content.*;

public class ExampleJavaMod extends Mod{

    public ExampleJavaMod(){
        Log.info("Loaded ExampleJavaMod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("雷霆大青蛙");
                dialog.cont.add("behold").row();
                dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("I am blind", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        Log.info("Loading some example content.");

        // ★ 解锁所有内容 ★
        Events.on(ContentInitEvent.class, e -> {
            for (UnlockableContent c : Vars.content.blocks()) {
                c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.items()) {
                c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.liquids()) {
                c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.units()) {
                c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.statusEffects()) {
                c.alwaysUnlocked = true;
            }
            Log.info("All content unlocked!");
        });

        fPlanets.load();
        Log.info("Fusion Planet loaded!");
    }

}