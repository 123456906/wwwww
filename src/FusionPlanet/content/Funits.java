package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.SapBulletType;
import mindustry.type.Weapon;
import mindustry.type.weapons.*;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.type.UnitType;

public class Funits {
    public static UnitType falcon;
    public static UnitType nova = new UnitType("nova") {{
        hitSize = 32f * 4f / 3f;
        health = 1200f;
        speed = 3.5f;
        accel = 0.08f;
        drag = 0.02f;
        flying = true;
        lowAltitude = true;
        rotateSpeed = 6f;
        baseRotateSpeed = 6f;
        mineSpeed = 4f;
        mineTier = 3;
        buildSpeed = 2f;
        itemCapacity = 60;

        engineOffset = 25f;
        engineSize = 4.5f;
        engineColor = Color.valueOf("ffffff");

        weapons.add(new Weapon(" -blaster") {{
            reload = 10;
            bullet = new BasicBulletType() {{
                damage = 35;
                speed = 6f;
                lifetime = 40;
                hitEffect = Fx.hitBulletColor;
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke2;
                ammoMultiplier = 1;
                backColor = Color.valueOf("ffffff");
                frontColor = Color.valueOf("eeeeff");
                width = 6f;
                height = 10f;
                collidesAir = true;
                collidesGround = true;
            }};
            x = 8f;
            y = 0f;
            shootSound = Sounds.unitExplode2;
        }});

        weapons.add(new Weapon("nova-laser") {{
            reload = 30;
            bullet = new LaserBulletType() {{
                damage = 55;
                length = 120f;
                lifetime = 20f;
                colors = new Color[]{
                        Color.valueOf("ffffff"),
                        Color.valueOf("eeeeff"),
                        Color.valueOf("ddddee")
                };
                hitEffect = Fx.hitLaser;
                shootEffect = Fx.hitLancer;
                smokeEffect = Fx.hitLancer;
                shootSound = Sounds.shootLaser;
                ammoMultiplier = 1;
            }};
            x = -6f;
            y = 0f;
            shootSound = Sounds.shootLaser;
        }});

        weapons.add(new Weapon("nova-sap") {{
            bullet = new SapBulletType() {{
                lifetime = 35f;
                damage = 20f;
                knockback = -1.24f;
                length = 200f;
                hitColor = Color.valueOf("ffffff");
                color = Color.valueOf("ffffff");
                lightRadius = 18f;
                width = 0.54f;
            }};
            mirror = true;
            rotate = true;
            baseRotation = 0f;
            reload = 28f;
            recoil = 2f;
            recoilTime = 28f;
            shootX = 0f;
            shootY = 4f;
            x = -8.5f;
            y = -1.5f;
            shootSound = Sounds.shootSap;
        }});

        deathExplosionEffect = Fx.bigShockwave;
        deathSound = Sounds.explosion;
        trailLength = 45;
        trailColor = Color.valueOf("ffffff").a(0.6f);

        alwaysUnlocked = false;
    }};

    public static void load() {
        falcon = new UnitType("falcon") {{
            hitSize = 32f;
            health = 800f;
            speed = 3.2f;
            accel = 0.08f;
            drag = 0.02f;
            flying = true;
            lowAltitude = true;
            rotateSpeed = 6f;
            baseRotateSpeed = 6f;
            mineSpeed = 3f;
            mineTier = 2;
            buildSpeed = 1.5f;
            itemCapacity = 40;

            engineOffset = 18f;
            engineSize = 3.5f;
            engineColor = Color.valueOf("8866ff");

            weapons.add(new Weapon("falcon-blaster") {{
                reload = 10;
                bullet = new BasicBulletType() {{
                    damage = 25;
                    speed = 6f;
                    lifetime = 40;
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootBig;
                    smokeEffect = Fx.shootBigSmoke2;
                    ammoMultiplier = 1;
                    backColor = Color.valueOf("8866ff");
                    frontColor = Color.valueOf("bb99ff");
                    width = 6f;
                    height = 10f;
                    shootSound = Sounds.unitExplode2;
                    collidesAir = true;
                    collidesGround = true;
                }};
                x = 8f;
                y = 0f;
                shootSound = Sounds.unitExplode2;
            }});

            weapons.add(new Weapon("falcon-laser") {{
                reload = 30;
                bullet = new LaserBulletType() {{
                    damage = 40;
                    length = 100f;
                    lifetime = 20f;
                    colors = new Color[]{
                            Color.valueOf("8866ff"),
                            Color.valueOf("bb99ff"),
                            Color.valueOf("ddccff")
                    };
                    hitEffect = Fx.hitLaser;
                    shootEffect = Fx.hitLancer;
                    smokeEffect = Fx.hitLancer;
                    shootSound = Sounds.shootLaser;
                    ammoMultiplier = 1;
                }};
                x = -6f;
                y = 0f;
                shootSound = Sounds.shootLaser;
            }});

            deathExplosionEffect = Fx.bigShockwave;
            deathSound = Sounds.explosion;
            trailLength = 40;
            trailColor = Color.valueOf("8866ff").a(0.6f);

            alwaysUnlocked = false;
        }};

    }
}