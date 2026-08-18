package FusionPlanet;

import FusionPlanet.content.fPlanets;
import arc.*;
import arc.struct.Seq;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.type.Planet;
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
        Planet fusion = Vars.content.planets().find(p -> p.name.equals("fusion-planet"));
        if (fusion == null) {
            Log.err("Fusion planet not found in content!");
            return;
        }
        Log.info("Fusion Planet loaded!");
        Events.on(ContentInitEvent.class, e -> {
            for (UnlockableContent c : Vars.content.blocks()) {
                if (c.minfo.mod == null) c.alwaysUnlocked = true;
                c.shownPlanets = Seq.with(fusion).asSet();
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
            Log.info("All vanilla content unlocked!");
        });


    }

}