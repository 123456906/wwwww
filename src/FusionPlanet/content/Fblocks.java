package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;

import static FusionPlanet.content.Funits.falcon;

public class Fblocks {

    public static Block coreEvoke;
    public static Block assimilator;
    public static Block summonTurret;

    public static Floor blueGrass;
    public static Floor purpleStone;
    public static Floor cyanWater;
    public static Floor indigoSand;
    public static Floor magentaMoss;
    public static Floor glowSpore;
    public static Floor indigoIce;

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
        }};

        blueGrass = new Floor("blue-grass") {{
            variants = 3;
            speedMultiplier = 1f;
            mapColor.set(Color.valueOf("6688cc"));
            supportsOverlay = true;
        }};

        purpleStone = new Floor("purple-stone") {{
            variants = 3;
            speedMultiplier = 0.8f;
            mapColor.set(Color.valueOf("9966cc"));
        }};

        cyanWater = new Floor("cyan-water") {{
            speedMultiplier = 0.2f;
            variants = 0;
            liquidDrop = Liquids.water;
            liquidMultiplier = 1.5f;
            isLiquid = true;
            status = StatusEffects.wet;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("44ccdd"));
        }};

        indigoSand = new Floor("indigo-sand") {{
            variants = 3;
            speedMultiplier = 0.9f;
            mapColor.set(Color.valueOf("4455aa"));
        }};

        magentaMoss = new Floor("magenta-moss") {{
            variants = 3;
            speedMultiplier = 0.85f;
            mapColor.set(Color.valueOf("cc88dd"));
        }};

        glowSpore = new Floor("glow-spore") {{
            variants = 2;
            speedMultiplier = 0.95f;
            mapColor.set(Color.valueOf("88ddff"));
        }};

        indigoIce = new Floor("indigo-ice") {{
            variants = 2;
            speedMultiplier = 0.7f;
            mapColor.set(Color.valueOf("334477"));
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
                float progress = Mathf.clamp(damageAccumulated / summonThreshold);
                Drawf.circles(x, y, range * progress, Color.valueOf("ffcc88").a(0.15f));
                Drawf.dashCircle(x, y, range, Color.valueOf("ff9966").a(0.25f));

                if (glowTime > 0f) {
                    float glow = Mathf.clamp(glowTime / 45f);
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