package FusionPlanet;

import FusionPlanet.content.Fblocks;
import FusionPlanet.content.FtechTree;
import FusionPlanet.content.Funits;
import FusionPlanet.content.fPlanets;
import FusionPlanet.content.PrecursorTeam;
import arc.*;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
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
    private final Color titleColor = new Color();
    private final Color subColor = new Color();

    private static final String QQ_URL = "https://qm.qq.com/q/D9Z6lG8Urm";

    private Table linearFilterToggle;
    private TextButton linearToggleBtn;
    private static final float TOGGLE_W = 220f;
    private static final float TOGGLE_H = 48f;

    public main(){
        Log.info("Loaded Fusion Planet mod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            createLinearFilterToggle();
            setupFusionPlanetUnlockIcon();
            setupFusionPlanetIcon();
            applyInitialLockState();
            Time.runTask(10f, this::showWelcomeDialog);
        });
    }

    private void setupFusionPlanetUnlockIcon(){
        TextureRegion icon = Core.atlas.find("2");
        if (icon.found()) {
            Fblocks.fusionPlanetUnlock.uiIcon = icon;
            Fblocks.fusionPlanetUnlock.region = icon;
            Log.info("[FusionPlanet] fusionPlanetUnlock icon set to: 2");
        } else {
            Log.err("[FusionPlanet] icon '2' not found in atlas");
        }
    }

    private void setupFusionPlanetIcon(){
        if (fPlanets.fusionPlanet == null) return;
        TextureRegion icon = Core.atlas.find("3");
        if (icon.found()) {
            fPlanets.fusionPlanet.uiIcon = icon;
            fPlanets.fusionPlanet.iconColor = Color.white;
            Log.info("[FusionPlanet] fusionPlanet icon set to: 3");
        } else {
            Log.err("[FusionPlanet] icon '3' not found in atlas");
        }
    }

    private void applyInitialLockState(){
        if (Fblocks.fusionPlanetUnlock == null || fPlanets.fusionPlanet == null) return;

        if (Fblocks.fusionPlanetUnlock.alwaysUnlocked) {
            if (!fPlanets.fusionPlanet.alwaysUnlocked) {
                fPlanets.fusionPlanet.alwaysUnlocked = true;
                fPlanets.fusionPlanet.unlock();
            }
            Log.info("[FusionPlanet] init: fusionPlanet UNLOCKED (block alwaysUnlocked)");
        } else {
            fPlanets.fusionPlanet.alwaysUnlocked = false;
            Log.info("[FusionPlanet] init: fusionPlanet LOCKED (waiting for tech tree)");
        }
    }

    private void unlockFusionPlanetFromTech(){
        if (fPlanets.fusionPlanet == null) return;
        if (!fPlanets.fusionPlanet.alwaysUnlocked) {
            fPlanets.fusionPlanet.alwaysUnlocked = true;
            fPlanets.fusionPlanet.unlock();
            Log.info("[FusionPlanet] tech tree unlocked fusionPlanet!");
        }
    }

    private void createLinearFilterToggle(){
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

    private void updateToggleText(boolean on){
        if (linearToggleBtn != null) {
            linearToggleBtn.setText("抗锯齿: " + (on ? "开" : "关"));
        }
    }

    private void applyLinearFilter(boolean linear){
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

    private String getModVersion(){
        try {
            Mods.ModMeta m = Vars.mods.getMod(main.class).meta;
            if (m != null && m.version != null) {
                return m.version;
            }
        } catch (Exception ex) {
            Log.err("Failed to read mod version: " + ex);
        }
        return "dev";
    }

    private void showWelcomeDialog(){
        BaseDialog dialog = new BaseDialog("");
        dialog.setColor(Color.valueOf("07090f"));

        Table window = new Table();
        window.setBackground(Styles.black8);

        Table titleBar = new Table();
        titleBar.setBackground(Styles.black6);

        Label barTitle = new Label("  FUSION PLANET LAUNCHER");
        barTitle.setFontScale(0.78f);
        barTitle.setColor(Color.valueOf("8899bb"));
        titleBar.add(barTitle).left().padLeft(10f).padTop(8f).padBottom(8f);

        titleBar.add().expandX();

        Label dot1 = new Label("●");
        dot1.setColor(Color.valueOf("66cc88"));
        dot1.setFontScale(0.6f);
        titleBar.add(dot1).padRight(8f).padTop(8f).padBottom(8f);

        Label dot2 = new Label("●");
        dot2.setColor(Color.valueOf("ccaa44"));
        dot2.setFontScale(0.6f);
        titleBar.add(dot2).padRight(8f).padTop(8f).padBottom(8f);

        Table closeBtn = new Table();
        closeBtn.setBackground(Styles.black6);
        Label closeLabel = new Label("✕");
        closeLabel.setFontScale(1.1f);
        closeLabel.setColor(Color.valueOf("ff5555"));
        closeBtn.add(closeLabel).pad(6f).padLeft(10f).padRight(10f);
        closeBtn.clicked(dialog::hide);
        closeBtn.hovered(() -> closeBtn.setBackground(Styles.black3));
        titleBar.add(closeBtn).padTop(4f).padBottom(4f).padRight(4f);

        window.add(titleBar).width(760f).height(36f).colspan(2).row();

        Table body = new Table();

        Table left = new Table();
        left.setBackground(Styles.black6);

        TextureRegion logoRegion = Core.atlas.find("logo");
        if (!logoRegion.found()) {
            logoRegion = Core.atlas.find("ui/logo");
        }
        if (logoRegion.found()) {
            float aspect = logoRegion.height / (float) logoRegion.width;
            float w = 320f;
            float h = w * aspect;
            left.add(new Image(logoRegion)).size(w, h).padTop(24f).padBottom(16f).row();
        } else {
            left.add(new Label("FUSION")).fontScale(2.6f)
                    .color(Color.valueOf("7a8cbf"))
                    .padTop(40f).padBottom(8f).row();
            Log.err("[FusionPlanet] welcome logo 'logo' not found");
        }

        Label title = new Label("FUSION PLANET");
        title.setFontScale(1.55f);
        title.setColor(Color.valueOf("7a8cbf"));
        left.add(title).padBottom(4f).row();

        Label subtitle = new Label("融 合 世 界");
        subtitle.setFontScale(0.95f);
        subtitle.setColor(Color.valueOf("aabbdd"));
        left.add(subtitle).padBottom(4f).row();

        Label tag = new Label("FUSION BOUNDARY · WORLD REMADE");
        tag.setFontScale(0.6f);
        tag.setColor(Color.valueOf("5a6a8a"));
        left.add(tag).padBottom(20f).row();

        left.add(new Image(Core.atlas.white())).height(1f).width(240f)
                .color(Color.valueOf("2a3a5a")).padBottom(16f).row();

        Label statusLine = new Label("STATUS  ·  ONLINE");
        statusLine.setFontScale(0.62f);
        statusLine.setColor(Color.valueOf("66cc88"));
        left.add(statusLine).padBottom(6f).row();

        Label verLabel = new Label("v" + getModVersion() + "  ·  Fusion Team");
        verLabel.setFontScale(0.62f);
        verLabel.setColor(Color.valueOf("5a6a8a"));
        left.add(verLabel).padBottom(24f).row();

        Table right = new Table();
        right.setBackground(Styles.black6);

        Label welcome = new Label("WELCOME");
        welcome.setFontScale(1.3f);
        welcome.setColor(Color.valueOf("eef4ff"));
        right.add(welcome).left().padLeft(24f).padTop(24f).padBottom(4f).row();

        Label welcomeCn = new Label("欢迎回到融合世界");
        welcomeCn.setFontScale(0.85f);
        welcomeCn.setColor(Color.valueOf("8899bb"));
        right.add(welcomeCn).left().padLeft(24f).padBottom(20f).row();

        right.add(new Image(Core.atlas.white())).height(1f).width(320f)
                .color(Color.valueOf("2a3a5a")).padBottom(16f).left().padLeft(24f).row();

        Label desc = new Label("黄队的火种已坠落于双环之下。\n幽灵在夜里游荡，白影在深处低语。\n真相，埋在这颗星球的核心之下。");
        desc.setFontScale(0.8f);
        desc.setColor(Color.valueOf("b8c0d0"));
        desc.setAlignment(Align.left);
        right.add(desc).left().padLeft(24f).padRight(24f).padBottom(22f).row();

        Table btnRow = new Table();
        btnRow.button("开始探索", dialog::hide).size(180f, 44f).padRight(8f);
        btnRow.button("背景故事", this::showStoryDialog).size(140f, 44f);
        right.add(btnRow).left().padLeft(24f).padBottom(10f).row();

        right.button("加入 QQ 群 / 反馈", () -> {
            try {
                Core.app.openURI(QQ_URL);
            } catch (Exception ex) {
                Log.err("Failed to open URL: " + QQ_URL);
            }
        }).size(328f, 38f).left().padLeft(24f).padBottom(24f).row();

        body.add(left).width(340f).top();
        body.add(right).width(420f).top();

        window.add(body).width(760f).padTop(2f).colspan(2).row();

        Table statusBar = new Table();
        statusBar.setBackground(Styles.black6);

        Label statusDot = new Label("●");
        statusDot.setColor(Color.valueOf("66cc88"));
        statusDot.setFontScale(0.65f);
        statusBar.add(statusDot).padLeft(12f).padRight(6f);

        Label statusText = new Label("READY  ·  ALL SYSTEMS NOMINAL");
        statusText.setColor(Color.valueOf("88a8b8"));
        statusText.setFontScale(0.65f);
        statusBar.add(statusText).left();

        statusBar.add().expandX();

        Label clock = new Label("--:--:--");
        clock.setFontScale(0.65f);
        clock.setColor(Color.valueOf("88a8b8"));
        statusBar.add(clock).padRight(12f);

        window.add(statusBar).width(760f).padTop(2f).colspan(2).row();

        dialog.cont.add(window).width(760f).pad(0f);

        dialog.cont.update(() -> {
            animTime += Time.delta;
            float p = (Mathf.sin(animTime * Mathf.PI2) + 1f) * 0.5f;

            titleColor.set(Color.valueOf("7a8cbf")).lerp(Color.valueOf("eef4ff"), p * 0.5f);
            title.setColor(titleColor);

            subColor.set(Color.valueOf("aabbdd")).lerp(Color.valueOf("ffffff"), p * 0.3f);
            subtitle.setColor(subColor);

            long ms = System.currentTimeMillis();
            int sec = (int)((ms / 1000) % 60);
            int min = (int)((ms / 60000) % 60);
            int hour = (int)((ms / 3600000) % 24);
            clock.setText(String.format("%02d:%02d:%02d", hour, min, sec));
        });

        dialog.show();
    }

    private void showStoryDialog(){
        BaseDialog dialog = new BaseDialog("背 景 故 事");
        dialog.setColor(Color.valueOf("0a0e1a"));

        Table content = new Table();
        content.setBackground(Styles.black8);
        content.top().left();
        content.margin(18f);

        content.add(new Label("融 合 世 界  ·  编 年 史")).fontScale(1.3f)
                .color(Color.valueOf("aabbdd")).padBottom(6f).row();

        content.add(new Image(Core.atlas.white())).height(1f).width(480f)
                .color(Color.valueOf("3a4a6a")).padBottom(12f).row();

        addChapter(content, "第一章 · 先驱者",
                "赛普罗并非无主之地。在红与黄的旗帜升起之前，这颗星球属于先驱者——" +
                "一个早已被遗忘的名字。他们建立了第一批核心，点亮了第一座发射井。" +
                "后来他们分裂了。");

        TextureRegion chapter1 = Core.atlas.find("1");
        if (chapter1.found()) {
            float aspect = chapter1.height / (float) chapter1.width;
            float imgW = 490f;
            float imgH = imgW * aspect;
            content.add(new Image(chapter1))
                    .size(imgW, imgH)
                    .padTop(6f)
                    .padBottom(14f)
                    .row();
        } else {
            Log.err("[story] image '1' not found in atlas");
        }

        addChapter(content, "第二章 · 红与黄",
                "先驱者的血脉一分为二。红队执掌铁与火，黄队笃信光与秩序。" +
                "分歧演变为战争，战争又演变为流亡。当 CRUX 的红色洪流吞没最后一座黄队核心时，" +
                "残余的运输舰跃入深空——身后再无归途。");

        addChapter(content, "第三章 · 融合星球",
                "跃迁引擎在未知星域崩解。黄队坠落到一颗被双环包裹的行星——" +
                "大气中弥漫着赛普罗与埃里克尔都未曾记录的元素。土壤会融合、会生长、会记忆。" +
                "他们把这颗星球命名为「融合世界」。");

        addChapter(content, "第四章 · 幽灵部队",
                "第一次遭遇发生在第八个夜晚。哨塔报告「红色单位接近」，但雷达上没有任何信号。" +
                "火力覆盖后，只剩下腐蚀的地面和一串不属于任何已知队伍的编码。\n" +
                "他们称其为「幽灵」——CRUX 的幽灵。\n" +
                "诡异的是，CRUX 官方通讯中从未提及这颗星球，也从未承认派出过任何部队。" +
                "幽灵从何而来？连 CRUX 自己都不知道。");

        addChapter(content, "第五章 · 白影重临",
                "更深的矿井里，黄队发现了不属于 CRUX、也不属于当代赛普罗的遗迹——" +
                "纯白色的装甲、早已断电的核心、以及墙壁上刻着的陌生文字。\n" +
                "先驱者。这颗星球最早的居民。\n" +
                "他们曾经在这里建立文明，如今只剩下空荡的方尖碑与沉默的数据核心。" +
                "他们是谁？他们为什么消失？他们和幽灵之间，又有什么联系？");

        addChapter(content, "终章 · 融合边界",
                "黄队在这里竖起新的核心。他们不再是逃亡者——他们是这颗行星的新居民。\n" +
                "但幽灵仍在夜里游荡，白影仍在深处低语。融合边界已经打开，" +
                "而真相，埋在这颗星球的核心之下。");

        ScrollPane pane = new ScrollPane(content);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);

        dialog.cont.add(pane).width(540f).height(430f).pad(10f).row();

        dialog.cont.button("返 回", dialog::hide).size(200f, 46f).padBottom(12f);

        dialog.show();
    }

    private void addChapter(Table t, String title, String body){
        Label titleLabel = new Label(title);
        titleLabel.setFontScale(0.98f);
        titleLabel.setColor(Color.valueOf("aabbdd"));
        t.add(titleLabel).left().padTop(14f).padBottom(4f).row();

        Label bodyLabel = new Label(body);
        bodyLabel.setFontScale(0.85f);
        bodyLabel.setColor(Color.valueOf("b8c0d0"));
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.left);
        t.add(bodyLabel).width(490f).left().padBottom(8f).row();
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
            allPlanets.add(fPlanets.superPlanet);

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

        Events.on(UnlockEvent.class, e -> {
            Log.info("[FusionPlanet] UnlockEvent fired: " + e.content.name);
            if (e.content == Fblocks.fusionPlanetUnlock) {
                Log.info("[FusionPlanet] detected fusionPlanetUnlock, unlocking fusionPlanet");
                unlockFusionPlanetFromTech();
            }
        });

        Log.info("Fusion Planet loaded!");
    }
}