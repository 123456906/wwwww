package FusionPlanet;

import FusionPlanet.content.fPlanets;
import arc.*;
import arc.struct.ObjectSet;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.type.Planet;
import mindustry.ui.dialogs.*;
import mindustry.ctype.UnlockableContent;
import mindustry.Vars;
import mindustry.content.Planets;

public class main extends Mod{

    public main(){
        Log.info("Loaded ExampleJavaMod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("雷霆大青蛙").row();
                dialog.cont.image(Core.atlas.find("FusionPlanet-frog")).pad(20f).row();
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
            Planet fusion = fPlanets.fusionPlanet;
            if (fusion == null) {
                Log.err("Fusion planet is null!");
                return;
            }

            ObjectSet<Planet> allPlanets = ObjectSet.with(
                    Planets.sun,
                    Planets.serpulo,
                    Planets.erekir,
                    Planets.tantros,
                    Planets.gier,
                    Planets.notva,
                    Planets.verilus,
                    fusion
            );

            for (UnlockableContent c : Vars.content.blocks()) {
                if (c.minfo.mod == null) {
                    c.alwaysUnlocked = true;
                    c.shownPlanets = allPlanets;
                }
            }
            for (UnlockableContent c : Vars.content.items()) {
                if (c.minfo.mod == null) {
                    c.alwaysUnlocked = true;
                    c.shownPlanets = allPlanets;
                }
            }
            for (UnlockableContent c : Vars.content.liquids()) {
                if (c.minfo.mod == null) {
                    c.alwaysUnlocked = true;
                    c.shownPlanets = allPlanets;
                }
            }
            for (UnlockableContent c : Vars.content.units()) {
                if (c.minfo.mod == null) {
                    c.alwaysUnlocked = true;
                    c.shownPlanets = allPlanets;
                }
            }
            for (UnlockableContent c : Vars.content.statusEffects()) {
                if (c.minfo.mod == null) {
                    c.alwaysUnlocked = true;
                    c.shownPlanets = allPlanets;
                }
            }
            Log.info("All vanilla content unlocked on all planets!");
        });

        Log.info("Fusion Planet loaded!");
    }

}