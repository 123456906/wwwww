package FusionPlanet;

import FusionPlanet.content.Fblocks;
import FusionPlanet.content.FtechTree;
import FusionPlanet.content.Funits;
import FusionPlanet.content.fPlanets;
import FusionPlanet.content.PrecursorTeam;
import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
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

    public main(){
        Log.info("Loaded Fusion Planet mod constructor.");

        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, this::showWelcomeDialog);
        });
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
        BaseDialog dialog = new BaseDialog("Fusion Planet");
        dialog.setColor(Color.valueOf("0a0e1a"));

        Table panel = new Table();
        panel.setBackground(Styles.black8);

        panel.add(new Image(Core.atlas.white())).height(2f).width(380f)
                .color(Color.valueOf("7a8cbf"))
                .padTop(24f).padBottom(20f).row();

        Label title = new Label("FUSION PLANET");
        title.setFontScale(2.3f);
        title.setColor(Color.valueOf("7a8cbf"));
        panel.add(title).padBottom(8f).row();

        Label subtitle = new Label("融 合 世 界");
        subtitle.setFontScale(1.05f);
        subtitle.setColor(Color.valueOf("aabbdd"));
        panel.add(subtitle).padBottom(6f).row();

        Label tag = new Label("FUSION BOUNDARY  ·  WORLD REMADE");
        tag.setFontScale(0.7f);
        tag.setColor(Color.valueOf("5a6a8a"));
        panel.add(tag).padBottom(24f).row();

        TextureRegion logoRegion = Core.atlas.find("ui/logo");
        if (!logoRegion.found()) {
            logoRegion = Core.atlas.find("logo");
        }
        if (logoRegion.found()) {
            float aspect = logoRegion.height / (float) logoRegion.width;
            float logoWidth = 420f;
            float logoHeight = logoWidth * aspect;
            Image logo = new Image(logoRegion);
            panel.add(logo).size(logoWidth, logoHeight).pad(8f).padBottom(22f).row();
        }

        Label desc = new Label("黄队的火种已坠落于双环之下");
        desc.setFontScale(0.95f);
        desc.setColor(Color.valueOf("c8d0e0"));
        panel.add(desc).pad(2f).row();

        Label desc2 = new Label("幽灵在夜里游荡，白影在深处低语");
        desc2.setFontScale(0.85f);
        desc2.setColor(Color.valueOf("8898b8"));
        panel.add(desc2).pad(2f).padBottom(24f).row();

        Table mainRow = new Table();
        mainRow.button("开 始 探 索", dialog::hide).size(240f, 52f).padRight(8f);
        mainRow.button("背 景 故 事", this::showStoryDialog).size(180f, 52f);
        panel.add(mainRow).padBottom(10f).row();

        panel.button("加入 QQ 群 / 反馈", () -> {
            try {
                Core.app.openURI(QQ_URL);
            } catch (Exception ex) {
                Log.err("Failed to open URL: " + QQ_URL);
            }
        }).size(430f, 42f).padBottom(6f).row();

        Label version = new Label("v" + getModVersion() + "  ·  Fusion Planet  ·  by Fusion Team");
        version.setFontScale(0.62f);
        version.setColor(Color.valueOf("3d4a5e"));
        panel.add(version).padTop(14f).padBottom(18f).row();

        dialog.cont.add(panel).width(480f).pad(10f);

        dialog.cont.update(() -> {
            animTime += Time.delta;
            float p = (Mathf.sin(animTime * 20f) + 1f) * 0.5f;

            titleColor.set(Color.valueOf("7a8cbf")).lerp(Color.valueOf("eef4ff"), p * 0.5f);
            title.setColor(titleColor);

            subColor.set(Color.valueOf("aabbdd")).lerp(Color.valueOf("ffffff"), p * 0.3f);
            subtitle.setColor(subColor);
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

        addChapter(content, "第一章 · 逃离赛普罗",
                "黄队残部在赛普罗的最后一次围剿中全军溃败。CRUX 的红色洪流吞没了他们每一座核心，" +
                        "仅存的运输舰带着最后的种子与工程兵，跃入深空。他们不知道航向，只知道身后再无归途。");

        addChapter(content, "第二章 · 融合星球",
                "跃迁引擎在未知星域崩解。他们坠落到一颗被双环包裹的行星——大气中弥漫着赛普罗与埃里克尔" +
                        "都未曾记录的元素。这里的土壤会融合、会生长、会记忆。他们把这颗星球命名为「融合世界」。");

        addChapter(content, "第三章 · 幽灵部队",
                "第一次遭遇发生在第八个夜晚。哨塔报告「红色单位接近」，但雷达上没有任何信号。" +
                        "火力覆盖后，只剩下被腐蚀的地面和一串不属于任何已知队伍的编码。\n" +
                        "他们称其为「幽灵」——CRUX 的幽灵。\n" +
                        "诡异的是，CRUX 官方通讯中从未提及这颗星球，也从未承认派出过任何部队。" +
                        "幽灵从何而来？连 CRUX 自己都不知道。");

        addChapter(content, "第四章 · 白影踪迹",
                "更深的矿井里，黄队发现了不属于 CRUX、也不属于赛普罗的遗迹——纯白色的装甲、" +
                        "早已断电的核心、以及墙壁上刻着的陌生文字。\n" +
                        "先驱者。这颗星球的原住民。\n" +
                        "他们曾在这里建立文明，如今只剩下空荡的方尖碑与沉默的数据核心。" +
                        "他们是谁？他们为什么消失？");

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