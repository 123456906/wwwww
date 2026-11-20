package MirrorWorld.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;

import static MirrorWorld.content.MrItems.*;
import static mindustry.Vars.player;
import static mindustry.Vars.ui;

/**
 * 核心发射器（四档）。
 *
 * 规则：
 *   1. 每张地图 / 每个区块最多只能建造同型号的一台，可随时拆除；
 *   2. 只有建造了发射器的区块，才允许把核心发射到相邻 / 更远的区块；
 *   3. 发射范围随型号变大：小型 1 跳、中型 2 跳、大型 3 跳、终极全星球；
 *   4. 每档只能发射不大于自身尺寸的核心（4/5/6/7，即 4x4、5x5、6x6、7x7 及以下）。
 *
 * 判定与存档逻辑在 {@link CoreLaunchers}。
 */
public class CoreLauncher extends Block {

    public static CoreLauncher small, medium, large, finalLauncher;

    private static boolean loaded;

    /** 档位：1 小 / 2 中 / 3 大 / 4 终极。 */
    public int launcherLevel = 1;
    /** 可发射的区块跳数；<= 0 表示整颗星球任意区块。 */
    public int sectorRange = 1;
    /** 可发射的核心最大边长（4 表示 4x4 及以下）。 */
    public int maxCoreSize = 4;

    public Color glowColor = Color.valueOf("8fe4ff");
    public Color ringColor = Color.valueOf("ffd24a");

    public CoreLauncher(String name){
        super(name);
        update = true;
        solid = true;
        configurable = true;
        saveConfig = false;
        hasItems = false;
        canOverdrive = false;
        squareSprite = false;
    }

    public static void loadContent(){
        if(loaded) return;
        loaded = true;

        MrItems.load();

        small = new CoreLauncher("core-launcher-small"){{
            size = 4;
            health = 1600;
            launcherLevel = 1;
            sectorRange = 1;
            maxCoreSize = 4;
            glowColor = Color.valueOf("8fe4ff");
            ringColor = Color.valueOf("ffd24a");
            requirements(Category.effect, ItemStack.with(
                    mirrorShard, 400,
                    facetPlate, 250,
                    prismCrystal, 120
            ));
        }};

        medium = new CoreLauncher("core-launcher-medium"){{
            size = 5;
            health = 2600;
            launcherLevel = 2;
            sectorRange = 2;
            maxCoreSize = 5;
            glowColor = Color.valueOf("b39dff");
            ringColor = Color.valueOf("ffd24a");
            requirements(Category.effect, ItemStack.with(
                    facetPlate, 900,
                    prismCrystal, 600,
                    mirrorAlloy, 300,
                    echoChip, 200
            ));
        }};

        large = new CoreLauncher("core-launcher-large"){{
            size = 6;
            health = 4200;
            launcherLevel = 3;
            sectorRange = 3;
            maxCoreSize = 6;
            glowColor = Color.valueOf("8fd8e8");
            ringColor = Color.valueOf("ffb02e");
            requirements(Category.effect, ItemStack.with(
                    prismCrystal, 1400,
                    mirrorAlloy, 900,
                    phaseGlass, 450,
                    echoChip, 500
            ));
        }};

        finalLauncher = new CoreLauncher("core-launcher-final"){{
            size = 7;
            health = 6800;
            armor = 4f;
            launcherLevel = 4;
            sectorRange = 0; // 0 => 全星球
            maxCoreSize = 7;
            glowColor = Color.valueOf("ffd24a");
            ringColor = Color.valueOf("ffb02e");
            requirements(Category.effect, ItemStack.with(
                    voidOre, 1600,
                    abyssCore, 600,
                    resonantAlloy, 1000,
                    veilFiber, 800,
                    echoChip, 900
            ));
        }};

        CoreLaunchers.init();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(!super.canPlaceOn(tile, team, rotation)) return false;
        return !hasOne(team);
    }

    public boolean hasOne(Team team){
        for(Building b : Groups.build){
            if(b.block == this && b.team == team) return true;
        }
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        if(player != null && hasOne(player.team())){
            drawPlaceText(Core.bundle.get("mirrorworld.launcher.exists"), x, y, false);
        }
    }

    public class CoreLauncherBuild extends Building {

        public float time;

        public int launcherLevel(){
            return CoreLauncher.this.launcherLevel;
        }

        @Override
        public void updateTile(){
            time += Time.delta * (0.5f + efficiency);
            if(time > 3600f) time %= 3600f;
        }

        @Override
        public void draw(){
            super.draw();

            float z = Draw.z();
            Draw.z(Layer.blockAdditive);

            float base = block.size * 4f;
            float pulse = 0.5f + 0.5f * Mathf.absin(Time.time, 24f, 1f);

            // ---- 暗层：琥珀黄，宽线低透明，多层叠加近似高斯模糊 ----
            for(int i = 0; i < 3; i++){
                Draw.color(ringColor, 0.05f + 0.045f * pulse);
                Lines.stroke(7f - i * 2f);
                Lines.square(x, y, base * (0.70f + i * 0.16f), 45f + time * (0.35f + i * 0.12f));
            }
            Draw.color(ringColor, 0.10f + 0.06f * pulse);
            Lines.stroke(3.6f);
            Lines.square(x, y, base * 1.02f, 45f + time * 0.50f);

            // ---- 亮层：纯白细线，低不透明度，缓慢反向旋转 ----
            Draw.color(Color.white, 0.28f + 0.20f * pulse);
            Lines.stroke(1.1f);
            Lines.square(x, y, base * 0.99f, 45f - time * 0.62f);
            Draw.color(Color.white, 0.14f + 0.12f * pulse);
            Lines.stroke(0.8f);
            Lines.square(x, y, base * 1.16f, 45f + time * 0.30f);

            // ---- 四个旋转箭头 ----
            for(int i = 0; i < 4; i++){
                float rot = i * 90f + 45f + time * 18f;
                float ax = x + Angles.trnsx(rot, base * 1.04f);
                float ay = y + Angles.trnsy(rot, base * 1.04f);
                float len = 8f + 2f * pulse;
                Draw.color(ringColor, 0.55f + 0.25f * pulse);
                Drawf.tri(ax, ay, 3.4f, len, rot + 90f);
                Draw.color(Color.white, 0.55f);
                Drawf.tri(ax, ay, 1.2f, len * 0.55f, rot + 90f);
            }

            // ---- 中心核心 ----
            Draw.color(glowColor, 0.16f + 0.10f * pulse);
            Lines.stroke(2f);
            Lines.square(x, y, base * 0.40f, 45f + time * 0.9f);
            Draw.color(Color.white, 0.55f + 0.25f * pulse);
            Lines.stroke(1.2f);
            Lines.square(x, y, base * 0.22f, 45f - time * 1.4f);

            // ---- 软光晕 ----
            Drawf.light(x, y, base * 2.2f * (0.95f + 0.08f * pulse), glowColor, 0.30f + 0.18f * pulse);

            Draw.reset();
            Draw.z(z);
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            Drawf.dashCircle(x, y, block.size * 4f + 6f, glowColor.cpy().a(0.45f));
        }

        @Override
        public void buildConfiguration(Table table){
            CoreLauncher l = CoreLauncher.this;
            table.table(t -> {
                t.defaults().left().padBottom(4f);
                t.add(Core.bundle.format("mirrorworld.launcher.level", l.launcherLevel)).row();
                t.add(Core.bundle.format("mirrorworld.launcher.range",
                        l.sectorRange <= 0 ? Core.bundle.get("mirrorworld.launcher.all") : String.valueOf(l.sectorRange))).row();
                t.add(Core.bundle.format("mirrorworld.launcher.cores", l.maxCoreSize)).row();
                t.button(Core.bundle.get("mirrorworld.launcher.openmap"), Icon.map, () -> {
                    deselect();
                    ui.planet.show();
                }).size(200f, 44f).padTop(6f).row();
            }).pad(10f);
        }
    }
}
