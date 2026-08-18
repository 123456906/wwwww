package FusionPlanet;

import FusionPlanet.content.fPlanets;
import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.ctype.UnlockableContent;
import mindustry.Vars;

public class ExampleJavaMod extends Mod{

    public ExampleJavaMod(){
        Log.info("Loaded ExampleJavaMod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("雷霆大青蛙").row();
                dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("I am blind", dialog::hide).size(200f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        Log.info("Loading some example content.");

        fPlanets.load();

        Events.on(ContentInitEvent.class, e -> {
            // 解锁所有原版内容，所有行星均可使用
            for (UnlockableContent c : Vars.content.blocks()) {
                if (c.minfo.mod == null) c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.items()) {
                if (c.minfo.mod == null) c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.liquids()) {
                if (c.minfo.mod == null) c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.units()) {
                if (c.minfo.mod == null) c.alwaysUnlocked = true;
            }
            for (UnlockableContent c : Vars.content.statusEffects()) {
                if (c.minfo.mod == null) c.alwaysUnlocked = true;
            }
            Log.info("All vanilla content unlocked for all planets!");
        });

        Log.info("Fusion Planet loaded!");
    }

}