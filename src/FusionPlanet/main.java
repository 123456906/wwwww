package FusionPlanet;

import FusionPlanet.content.Fblocks;
import FusionPlanet.content.FtechTree;
import FusionPlanet.content.Funits;
import FusionPlanet.content.fPlanets;
import FusionPlanet.content.PrecursorTeam;
import arc.*;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.type.Planet;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;
import mindustry.ctype.UnlockableContent;
import mindustry.Vars;
import mindustry.content.Planets;

public class main extends Mod {

    private float animTime = 0f;

    public main(){
        Log.info("Loaded Fusion Planet mod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, this::showWelcomeDialog);
        });
    }

    private void showWelcomeDialog(){
        BaseDialog dialog = new BaseDialog("Fusion Planet");
        dialog.setColor(Color.valueOf("141a2a"));

        Table titleTable = new Table();
        titleTable.setBackground(Styles.black6);

        Label title = new Label("FUSION PLANET");
        title.setFontScale(2.2f);
        title.setColor(Color.valueOf("7a8cbf"));
        titleTable.add(title).padTop(14f).padBottom(4f).padLeft(24f).padRight(24f).row();

        Label subtitle = new Label("融 合 世 界");
        subtitle.setFontScale(1.1f);
        subtitle.setColor(Color.valueOf("aabbdd"));
        titleTable.add(subtitle).padBottom(16f).row();

        dialog.cont.add(titleTable).width(420f).padBottom(18f).row();

        dialog.cont.image(Core.atlas.find("FusionPlanet-frog"))
                .size(150f)
                .pad(14f)
                .row();

        Label desc = new Label("新的世界已就绪，等待你的探索");
        desc.setColor(Color.valueOf("d0d0d0"));
        desc.setFontScale(0.95f);
        dialog.cont.add(desc).pad(10f).row();

        Label hint = new Label("点击下方按钮进入游戏");
        hint.setColor(Color.valueOf("888899"));
        hint.setFontScale(0.85f);
        dialog.cont.add(hint).padBottom(20f).row();

        dialog.cont.button("开 始 探 索", dialog::hide)
                .size(240f, 56f)
                .pad(18f);

        dialog.cont.update(() -> {
            animTime += Time.delta;
            float pulse = (Mathf.sin(animTime * 1.6f) + 1f) * 0.5f;
            title.setColor(Tmp.c1.set(Color.valueOf("7a8cbf")).lerp(Color.valueOf("ffffff"), pulse * 0.6f));
            subtitle.setColor(Tmp.c1.set(Color.valueOf("aabbdd")).lerp(Color.valueOf("ffffff"), pulse * 0.35f));
        });

        dialog.show();
    }

    @Override
    public void loadContent(){
        Log.info("Loading Fusion Planet content...");

        Funits.load();
        Fblocks.load();
        PrecursorTeam.load();
        fPlanets.load();
        FtechTree.load();

        Events.on(ContentInitEvent.class, e -> {
            Planet fusion = fPlanets.fusionPlanet;
            if (fusion == null) {
                Log.err("Fusion planet is null!");
                return;
            }

            ObjectSet<Planet> allPlanets = new ObjectSet<>();
            allPlanets.add(Planets.sun);
            allPlanets.add(Planets.serpulo);
            allPlanets.add(Planets.erekir);
            allPlanets.add(Planets.tantros);
            allPlanets.add(Planets.gier);
            allPlanets.add(Planets.notva);
            allPlanets.add(Planets.verilus);
            allPlanets.add(fusion);

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