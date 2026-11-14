package MirrorWorld.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.*;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.part.RegionPart;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.draw.DrawTurret;

import static MirrorWorld.content.MrUnits.falcon;

public class MrBlocks {

    public static Block coreEvoke;
    public static Block assimilator;
    public static Block summonTurret;

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
            requirements = ItemStack.with(
                    Items.copper, 100,
                    Items.lead, 50,
                    Items.silicon, 25,
                    Items.graphite, 20
            );
        }};

        assimilator = new Assimilator("assimilator") {{
            size = 3;
            health = 400;
            range = 60f;
            description = "把血量在0到1000内的敌方单位在10到20秒内转化为我方单位并减少1/3的血量";
            requirements(Category.effect, ItemStack.with(
                    Items.copper, 3000,
                    Items.lead, 2000,
                    Items.silicon, 2000
            ));
            alwaysUnlocked = true;
        }};

        SummonBulletType copperBullet = new SummonBulletType(6f, 50) {{
            ammoMultiplier = 3;
        }};
        SummonBulletType graphiteBullet = new SummonBulletType(7f, 70) {{
            ammoMultiplier = 5;
            reloadMultiplier = 0.7f;
            rangeChange = 20f;
        }};
        SummonBulletType siliconBullet = new SummonBulletType(6.5f, 55) {{
            ammoMultiplier = 6;
            homingPower = 0.25f;
            reloadMultiplier = 1.4f;
        }};

        summonTurret = new SummonTurret("summon-turret") {{
            size = 4;
            health = 1000;
            range = 180f;
            reload = 15f;
            shootCone = 20f;
            targetAir = true;
            targetGround = true;
            ammoUseEffect = Fx.shootBig;
            shootSound = Sounds.shoot;
            recoil = 1f;
            shootY = 4f;
            rotateSpeed = 12f;
            inaccuracy = 3f;
            coolant = consumeCoolant(0.1f);
            coolantMultiplier = 2f;

            ammo(
                    Items.copper, copperBullet,
                    Items.graphite, graphiteBullet,
                    Items.silicon, siliconBullet
            );

            requirements(Category.turret, ItemStack.with(
                    Items.copper, 500,
                    Items.lead, 350,
                    Items.silicon, 250,
                    Items.titanium, 150,
                    Items.graphite, 100
            ));
            alwaysUnlocked = true;
            consumePower(50f);

            drawer = new DrawTurret() {{
                parts = new Seq<>(new RegionPart[] {
                        new RegionPart() {{
                            mirror = false;
                            x = 0;
                            y = 0;
                            suffix = "-1";
                            layer = 51;
                            moveX = -0.5f;
                            moveY = -0.5f;
                        }},
                        new RegionPart() {{
                            mirror = false;
                            x = 0;
                            y = 0;
                            suffix = "-2";
                            layer = 51;
                            moveX = 0.5f;
                            moveY = -0.5f;
                        }}
                });
            }};
        }};
    } // ★ 修复：补上 load() 方法缺失的闭合大括号

    public static class Assimilator extends Block {
        public float range = 60f;
        public float assimilateSpeed = 1f;

        public Assimilator(String name) {
            super(name);
            update = true;
            solid = true;
            configurable = true;
            hasPower = true;
            consumePower(50f);
        }

        public class AssimilatorBuild extends Building {
            private final ObjectMap<Unit, Float> progress = new ObjectMap<>();

            @Override
            public void updateTile() {
                Assimilator self = (Assimilator) block;
                if (power == null || power.graph.getPowerBalance() < 0.1f) return;

                Units.nearby(null, x, y, self.range, unit -> {
                    if (unit == null || unit.dead() || unit.team() == team) return;

                    float health = unit.maxHealth();
                    float required = requiredTime(health);
                    float prog = progress.get(unit, 0f);
                    prog += Time.delta * self.assimilateSpeed;

                    if (prog >= required) {
                        unit.team(team);
                        unit.health(unit.health() * 0.7f);
                        Fx.spawn.at(unit);
                        progress.remove(unit);
                    } else {
                        progress.put(unit, prog);
                    }
                });

                for (Unit u : progress.keys().toSeq()) {
                    if (u == null || u.dead() || u.team() == team || u.dst(x, y) > self.range) {
                        progress.remove(u);
                    }
                }
            }

            private float requiredTime(float health) {
                if (health <= 1000f) {
                    return 600f + 0.08f * (health - 500f);
                } else if (health <= 5000f) {
                    return 1200f + 0.01f * (health - 1000f);
                } else {
                    return 9999999f;
                }
            }

            @Override
            public void draw() {
                super.draw();

                // ★ 常驻光效：在被转化的单位头顶显示倒计时光环
                if (!progress.isEmpty()) {
                    float z = Draw.z();
                    Draw.z(Layer.effect);

                    for (Unit u : progress.keys()) {
                        if (u == null || u.dead()) continue;

                        float prog = progress.get(u, 0f);
                        float required = requiredTime(u.maxHealth());
                        float p = Mathf.clamp(prog / required);

                        float radius = u.hitSize() / 2f + 6f;
                        float pulse = 0.8f + Mathf.sin(Time.time * 0.1f) * 0.2f;

                        // 底层脉冲光晕
                        Drawf.circles(u.x, u.y, radius * 1.1f, Color.valueOf("aaddff").a(0.15f * pulse));
                        Drawf.circles(u.x, u.y, radius * 1.4f, Color.valueOf("bbddff").a(0.08f * pulse));

                        // 进度圆环
                        Lines.stroke(3f, Color.valueOf("aaddff").a(0.85f));
                        Lines.arc(u.x, u.y, radius, p, -90f);

                        // 进度高光
                        Lines.stroke(1.5f, Color.valueOf("ffffff").a(0.9f));
                        Lines.arc(u.x, u.y, radius, p, -90f);
                    }
                    Draw.z(z);
                }
            }

            @Override
            public void drawSelect() {
                Assimilator self = (Assimilator) block;

                float pulse = 0.75f + Mathf.sin(Time.time * 0.05f) * 0.25f;

                // ★ 仅点击时显示的范围虚线/光圈
                Drawf.circles(x, y, self.range * 1.10f, Color.valueOf("aaddff").a(0.05f * pulse));
                Drawf.circles(x, y, self.range * 1.04f, Color.valueOf("aaddff").a(0.09f * pulse));
                Drawf.circles(x, y, self.range * 0.55f, Color.valueOf("aaddff").a(0.04f * pulse));

                Lines.stroke(3.2f, Color.valueOf("aaddff").a(0.16f * pulse));
                Lines.circle(x, y, self.range);

                Lines.stroke(2f, Color.valueOf("bbddff").a(0.42f * pulse));
                Lines.circle(x, y, self.range);

                Lines.stroke(1f, Color.valueOf("ffffff").a(0.72f * pulse));
                Lines.circle(x, y, self.range * 0.98f);
            }
        }
    }

    public static class SummonBulletType extends BasicBulletType {
        public SummonBulletType(float speed, float damage) {
            super(speed, damage);
            width = 12f;
            height = 18f;
            lifetime = 60f;
            hitEffect = Fx.hitBulletColor;
            despawnEffect = Fx.hitBulletColor;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke2;
            trailLength = 12;
            trailWidth = 3f;
            backColor = Color.valueOf("ff9966");
            frontColor = Color.valueOf("ffcc88");
            trailColor = Color.valueOf("ff9966").a(0.6f);
            splashDamage = 20f;
            splashDamageRadius = 25f;
        }

        @Override
        public void hit(Bullet b, float x, float y) {
            super.hit(b, x, y);
            if (b.data instanceof SummonTurret.SummonTurretBuild) {
                ((SummonTurret.SummonTurretBuild) b.data).addDamage(b.damage);
            }
            Fx.shockwave.at(x, y, 15f, Color.valueOf("ffcc88"));
            Fx.spawn.at(x, y);
            Fx.hitBulletColor.at(x, y, 0, Color.valueOf("ff9966"));
        }
    }

    public static class SummonTurret extends ItemTurret {
        public float summonThreshold = 3000f;

        public SummonTurret(String name) {
            super(name);
        }

        public class SummonTurretBuild extends ItemTurretBuild {
            private float damageAccumulated = 0f;
            private float glowTime = 0f;

            public void addDamage(float damage) {
                damageAccumulated += damage;
                Fx.shootBigSmoke2.at(x, y, rotation);
                Fx.shootBig2.at(x, y);

                if (damageAccumulated >= summonThreshold) {
                    if (falcon != null) {
                        for (int i = 0; i < 8; i++) {
                            Fx.spawn.at(x + Mathf.random(-30f, 30f), y + Mathf.random(-30f, 30f));
                        }
                        Fx.flakExplosion.at(x, y);
                        Fx.shockwave.at(x, y, 40f, Color.valueOf("ffcc88"));
                        Unit unit = falcon.create(team);
                        unit.set(x, y);
                        unit.add();
                        Fx.spawn.at(unit);
                        Sounds.explosion.at(x, y);
                    }
                    damageAccumulated = 0f;
                    glowTime = 90f;
                }
            }

            @Override
            public void handleBullet(Bullet bullet, float x, float y, float angle) {
                super.handleBullet(bullet, x, y, angle);
                bullet.data = this;
            }

            @Override
            public void draw() {
                super.draw();

                // ★ 常驻光效：召唤爆发时的光环
                if (glowTime > 0f) {
                    float glow = Mathf.clamp(glowTime / 45f);

                    Drawf.circles(x, y, 60f * glow, Color.valueOf("ffcc88").a(0.12f * glow));
                    Drawf.circles(x, y, 40f * glow, Color.valueOf("ffaa66").a(0.15f * glow));

                    Lines.stroke(4f * glow, Color.valueOf("ffcc88").a(0.65f * glow));
                    Lines.circle(x, y, 50f * glow);

                    Lines.stroke(2f * glow, Color.valueOf("ffffff").a(0.5f * glow));
                    Lines.circle(x, y, 35f * glow);
                }
            }

            @Override
            public void drawSelect() {
                // ★ 仅点击时显示的射程虚线与进度条
                float progress = Mathf.clamp(damageAccumulated / summonThreshold);
                float pulse = 0.8f + Mathf.sin(Time.time * 0.07f) * 0.2f;

                Drawf.circles(x, y, range * 1.08f, Color.valueOf("ff9966").a(0.04f * pulse));
                Drawf.circles(x, y, range * 1.03f, Color.valueOf("ffaa66").a(0.07f * pulse));
                Drawf.circles(x, y, range * 0.55f, Color.valueOf("ffcc88").a(0.03f * pulse));

                Lines.stroke(3f, Color.valueOf("ff9966").a(0.18f * pulse));
                Lines.circle(x, y, range);

                Lines.stroke(2f, Color.valueOf("ffaa66").a(0.42f * pulse));
                Lines.circle(x, y, range);

                Lines.stroke(1f, Color.valueOf("ffcc88").a(0.7f * pulse));
                Lines.circle(x, y, range * 0.98f);

                if (progress > 0f) {
                    Lines.stroke(3.5f, Color.valueOf("ffcc88").a(0.85f));
                    Lines.arc(x, y, range * 0.96f, progress, 0f);

                    Lines.stroke(1.5f, Color.valueOf("ffffff").a(0.55f * progress));
                    Lines.arc(x, y, range * 0.96f, progress, 0f);
                }
            }

            @Override
            public void updateTile() {
                super.updateTile();
                if (glowTime > 0f) {
                    glowTime -= Time.delta;
                    if (glowTime < 0f) glowTime = 0f;
                }
            }
        }
    }
}