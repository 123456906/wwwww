package MirrorWorld;

import MirrorWorld.content.AntichthonBlocks;
import MirrorWorld.content.AntichthonGenerator;
import MirrorWorld.content.CoreLaunchers;
import MirrorWorld.content.MrBlocks;
import MirrorWorld.content.MrTechTree;
import MirrorWorld.content.MrUnits;
import MirrorWorld.content.MirrorSystemPlanets;
import MirrorWorld.content.PrecursorTeam;
import arc.*;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.type.Planet;
import mindustry.ui.Styles;
import mindustry.world.Block;

public class MirrorWorld extends Mod {

    private Table linearFilterToggle;
    private TextButton linearToggleBtn;
    private static final float TOGGLE_W = 220f;
    private static final float TOGGLE_H = 48f;

    private static final String SETTING_SHOW_LAUNCHER = "mirrorworld-showlauncher";

    public MirrorWorld() {
        Log.info("Loaded Mirror World mod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            createLinearFilterToggle();
            setupSettings();
            CoreLaunchers.install();
            if (Core.settings.getBool(SETTING_SHOW_LAUNCHER, true)) {
                Time.runTask(10f, this::showLauncherDialog);
            }
        });
    }

    private void setupSettings() {
        try {
            Vars.ui.settings.game.checkPref(SETTING_SHOW_LAUNCHER, true);
        } catch (Exception ex) {
            Log.err("Failed to add settings pref: " + ex);
        }
    }

    // ================= 启动界面 =================
    private void showLauncherDialog() {
        Dialog dialog = new Dialog("", Styles.defaultDialog);
        dialog.setBackground(Styles.black8);
        dialog.setFillParent(true);
        dialog.cont.clear();
        dialog.cont.margin(0f);

        Table root = new Table();
        root.setFillParent(true);
        dialog.cont.add(root).grow();

        TextureRegion logo = Core.atlas.find("logo");
        if (!logo.found()) logo = Core.atlas.find("ui/logo");
        if (logo.found()) {
            float aspect = logo.height / (float) logo.width;
            root.add(new Image(logo)).size(400f, 400f * aspect).padTop(20f).padBottom(10f).row();
        } else {
            root.add(new Label("MIRROR WORLD")).fontScale(2.5f).pad(20f).row();
        }

        Table body = new Table();
        body.top();

        Table leftList = new Table();
        leftList.top().left();
        ScrollPane leftScroll = new ScrollPane(leftList);
        leftScroll.setScrollingDisabled(true, false);
        leftScroll.setFadeScrollBars(false);

        Table rightDetail = new Table();
        rightDetail.top().left();
        ScrollPane rightScroll = new ScrollPane(rightDetail);
        rightScroll.setScrollingDisabled(true, false);
        rightScroll.setFadeScrollBars(false);

        Seq<Block> modBlocks = new Seq<>();
        Mods.LoadedMod thisMod = Vars.mods.getMod(MirrorWorld.class);
        if (thisMod != null) {
            for (Block b : Vars.content.blocks()) {
                if (b.minfo != null && b.minfo.mod == thisMod) {
                    if (!b.isHidden() && b.uiIcon != null) {
                        modBlocks.add(b);
                    }
                }
            }
        }

        // 使用标准 lambda 排序，不再依赖 Structs
        modBlocks.sort((a, b) -> a.localizedName.compareTo(b.localizedName));

        if (modBlocks.isEmpty()) {
            rightDetail.add(new Label("暂无建筑")).pad(20f);
        } else {
            for (int i = 0; i < modBlocks.size; i++) {
                Block b = modBlocks.get(i);
                Table item = new Table();
                item.setBackground(Styles.black6);
                item.margin(4f);

                TextureRegion icon = b.uiIcon;
                if (icon != null && icon.found()) {
                    item.add(new Image(icon)).size(32f).padRight(8f);
                }
                item.add(new Label(b.localizedName)).left().growX();

                item.clicked(() -> updateDetail(rightDetail, b));
                leftList.add(item).growX().padBottom(2f).row();
            }

            updateDetail(rightDetail, modBlocks.get(0));
        }

        body.add(leftScroll).width(320f).growY().pad(10f);
        body.add(rightScroll).growX().growY().pad(10f);
        root.add(body).grow().row();

        Table bottom = new Table();
        CheckBox showAgain = new CheckBox("不再显示");
        showAgain.setChecked(!Core.settings.getBool(SETTING_SHOW_LAUNCHER, true));
        showAgain.changed(() -> {
            Core.settings.put(SETTING_SHOW_LAUNCHER, !showAgain.isChecked());
        });
        bottom.add(showAgain).padRight(20f);
        bottom.button("进入游戏", dialog::hide).size(180f, 50f).pad(10f);
        bottom.button("加入 QQ 群 / 反馈", () -> {
            try {
                Core.app.openURI("https://qm.qq.com/q/D9Z6lG8Urm");
            } catch (Exception e) {
                Log.err("Failed to open URL: " + e);
            }
        }).size(200f, 50f).pad(10f);

        root.add(bottom).padBottom(20f).row();

        dialog.show();
    }

    private void updateDetail(Table detail, Block b) {
        detail.clear();
        detail.top().left();
        detail.margin(10f);

        if (b.uiIcon != null && b.uiIcon.found()) {
            detail.add(new Image(b.uiIcon)).size(128f).pad(10f).row();
        }
        detail.add(new Label(b.localizedName)).fontScale(1.5f).pad(10f).row();

        String desc = b.description;
        if (desc == null || desc.isEmpty()) {
            desc = "暂无描述";
        }
        Label descLabel = new Label(desc);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.left);
        detail.add(descLabel).width(600f).pad(10f).row();
    }

    // ================= 抗锯齿开关（主菜单右下角） =================
    private void createLinearFilterToggle() {
        linearFilterToggle = new Table();
        linearFilterToggle.name = "linearFilterToggle";

        boolean current = Core.settings.getBool("linear", false);
        linearToggleBtn = new TextButton("", Styles.flatt);
        linearToggleBtn.clicked(() -> {
            boolean next = !Core.settings.getBool("linear", false);
            Core.settings.put("linear", next);
            applyLinearFilter(next);
            updateToggleText(next);
            Log.info("Anti-aliasing (linear filter) toggled: " + next);
        });
        updateToggleText(current);

        linearFilterToggle.add(linearToggleBtn).size(TOGGLE_W, TOGGLE_H);
        linearFilterToggle.setSize(TOGGLE_W, TOGGLE_H);
        linearFilterToggle.visible = false;

        applyLinearFilter(current);

        Events.run(Trigger.update, () -> {
            if (linearFilterToggle == null) return;
            if (Core.scene == null || Core.scene.root == null) return;

            boolean inMenu = Vars.state.isMenu();
            linearFilterToggle.visible = inMenu;

            if (linearFilterToggle.parent == null) {
                Core.scene.root.addChild(linearFilterToggle);
            }

            if (inMenu && linearFilterToggle.parent != null) {
                linearFilterToggle.toFront();
                float pad = 24f;
                float x = Core.scene.getWidth() - TOGGLE_W - pad;
                float y = pad;
                linearFilterToggle.setPosition(x, y);
            }
        });
    }

    private void updateToggleText(boolean on) {
        if (linearToggleBtn != null) {
            linearToggleBtn.setText("抗锯齿: " + (on ? "开" : "关"));
        }
    }

    private void applyLinearFilter(boolean linear) {
        TextureFilter filter = linear ? TextureFilter.linear : TextureFilter.nearest;
        try {
            for (Texture tex : Core.atlas.getTextures()) {
                tex.setFilter(filter);
            }
            Log.info("Applied " + filter + " to all textures.");
        } catch (Exception ex) {
            Log.err("Failed to apply filter: " + ex);
        }
    }

    // ================= 内容加载 =================
    @Override
    public void loadContent() {
        Log.info("Loading Mirror World content...");

        MrUnits.load();
        MrBlocks.load();
        AntichthonBlocks.load();
        AntichthonGenerator.loadBlueprints();
        PrecursorTeam.load();
        MirrorSystemPlanets.load();
        MrTechTree.load();

        Events.on(ContentInitEvent.class, e -> {
            Planet antichthon = MirrorSystemPlanets.antichthon;
            if (antichthon == null) {
                Log.err("Antichthon planet is null!");
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
            allPlanets.add(antichthon);

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

        Log.info("Mirror World loaded!");
    }
}