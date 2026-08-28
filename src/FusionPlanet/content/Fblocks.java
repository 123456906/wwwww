package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.storage.CoreBlock;

import static FusionPlanet.content.Funits.falcon;

public class Fblocks {
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

        summonTurret = new SummonTurret("summon-turret") {{
            size = 3;
            health = 800;
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
                    Items.copper, new BasicBulletType(6f, 50) {{
                        width = 8f;
                        height = 12f;
                        lifetime = 60f;
                        ammoMultiplier = 3;
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                        hitColor = backColor = trailColor = Pal.copperAmmoBack;
                        frontColor = Pal.copperAmmoFront;
                        trailLength = 8;
                        trailWidth = 2f;
                        splashDamage = 20f;
                        splashDamageRadius = 20f;
                    }},
                    Items.graphite, new BasicBulletType(7f, 70) {{
                        width = 10f;
                        height = 14f;
                        ammoMultiplier = 5;
                        lifetime = 55f;
                        reloadMultiplier = 0.7f;
                        rangeChange = 20f;
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                        hitColor = backColor = trailColor = Pal.graphiteAmmoBack;
                        frontColor = Pal.graphiteAmmoFront;
                        trailLength = 10;
                        trailWidth = 2.5f;
                        splashDamage = 30f;
                        splashDamageRadius = 25f;
                    }},
                    Items.silicon, new BasicBulletType(6.5f, 55) {{
                        width = 8f;
                        height = 10f;
                        homingPower = 0.25f;
                        reloadMultiplier = 1.4f;
                        ammoMultiplier = 6;
                        lifetime = 65f;
                        trailLength = 12;
                        trailWidth = 2f;
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                        hitColor = backColor = trailColor = Pal.siliconAmmoBack;
                        frontColor = Pal.siliconAmmoFront;
                        splashDamage = 15f;
                        splashDamageRadius = 15f;
                    }}
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
        }};
    }

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
                Assimilator self = (Assimilator) block;
                Drawf.dashCircle(x, y, self.range, Color.white);
                Drawf.circles(x, y, self.range, Color.valueOf("ffffff").a(0.08f));
            }
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

            @Override
            public void handleBullet(Bullet bullet, float x, float y, float angle) {
                super.handleBullet(bullet, x, y, angle);
                damageAccumulated += bullet.damage;

                Fx.shootBigSmoke2.at(x, y, angle);
                Fx.shootSmall.at(x, y);

                if (damageAccumulated >= summonThreshold) {
                    if (falcon != null) {
                        for (int i = 0; i < 5; i++) {
                            Fx.spawn.at(x + Mathf.random(-20f, 20f), y + Mathf.random(-20f, 20f));
                        }
                        Fx.flakExplosion.at(x, y);
                        Fx.shockwave.at(x, y, 30f, Color.valueOf("ffcc88"));
                        Unit unit = falcon.create(team);
                        unit.set(x, y);
                        unit.add();
                        Fx.spawn.at(unit);
                        Sounds.explosion.at(x, y);
                    }
                    damageAccumulated = 0f;
                    glowTime = 60f;
                }
            }

            @Override
            public void draw() {
                super.draw();
                float progress = Mathf.clamp(damageAccumulated / summonThreshold);
                Drawf.circles(x, y, range * progress, Color.valueOf("ffcc88").a(0.15f));
                Drawf.dashCircle(x, y, range, Color.valueOf("ff9966").a(0.25f));

                if (glowTime > 0f) {
                    float glow = Mathf.clamp(glowTime / 30f);
                    Drawf.circles(x, y, 40f * glow, Color.valueOf("ffcc88").a(0.15f * glow));
                    Drawf.circles(x, y, 25f * glow, Color.valueOf("ff9966").a(0.2f * glow));
                    Drawf.dashCircle(x, y, 50f * glow, Color.valueOf("ffaa77").a(0.2f * glow));
                    glowTime -= Time.delta;
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