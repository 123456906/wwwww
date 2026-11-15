package MirrorWorld.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.Ability;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.SapBulletType;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 反地球单位表。
 *
 * 原有：nova（原型机）、falcon（侦察机）
 * 新增 17 个：
 *   基础   mite 工兵 / crawler 爬行者 / dagger 短剑 / mace 铁锤 / sabre 军刀 / buster 破阵
 *   辅助   mender 修理车 / aegis 庇护者 / envoy 特使 / nomad 游牧
 *   空中   vulture 秃鹫 / wyvern 飞龙 / specter 幽影 / cargo 货运机
 *   主力   leviathan 太空战列舰（跃迁） / skylift 空中航母 / ark 普通航母
 *
 * 测试时可以用 Vars.content.units.get("mite").create(Team.sharded) 之类直接刷出来。
 */
public class MrUnits {

    /* ================= 能力（战列舰跃迁 / 航母放机 / 随行维修） ================= */

    /** 跃迁：每隔一段时间朝当前朝向前移一段距离，起点/终点各一次传送与冲击波特效 */
    public static class WarpAbility extends Ability {
        public float reload = 60f * 11f;
        public float distance = 220f;
        public Color warpColor = Color.valueOf("9fc4e8");
        float counter;

        @Override
        public Ability copy(){
            WarpAbility copy = new WarpAbility();
            copy.reload = reload;
            copy.distance = distance;
            copy.warpColor = warpColor;
            return copy;
        }

        @Override
        public void update(Unit unit){
            counter += Time.delta;
            if(counter < reload) return;
            counter = 0f;

            float ang = unit.rotation * Mathf.degRad;
            float nx = unit.x + Mathf.cos(ang) * distance;
            float ny = unit.y + Mathf.sin(ang) * distance;

            // 出发：传送消散 + 冲击波
            Fx.teleportOut.at(unit.x, unit.y, unit.rotation, warpColor);
            Fx.bigShockwave.at(unit.x, unit.y);

            unit.x = nx;
            unit.y = ny;
            unit.vel.setZero();

            // 到达：传送成形 + 冲击波
            Fx.teleport.at(nx, ny, unit.rotation, warpColor);
            Fx.bigShockwave.at(nx, ny);
        }
    }

    /**
     * 航母舰载机：
     * 先在航母上生产并待命；附近出现敌方单位后，按间隔从甲板滑行放出。
     */
    public static class CarrierAbility extends Ability {
        public UnitType type;
        public int max = 4;
        public float reload = 60f * 12f;
        public float detectRange = 260f;
        public float launchReload = 18f;
        public float launchSpeed = 2.4f;

        float buildCounter;
        float launchCounter;

        // 按航母实体ID分别保存舰载单位，避免不同航母共用舰载机列表。
        private static final Map<Integer, ArrayList<Unit>> stored = new HashMap<>();

        public CarrierAbility(UnitType type){
            this.type = type;
        }

        @Override
        public Ability copy(){
            CarrierAbility copy = new CarrierAbility(type);
            copy.max = max;
            copy.reload = reload;
            copy.detectRange = detectRange;
            copy.launchReload = launchReload;
            copy.launchSpeed = launchSpeed;
            return copy;
        }

        @Override
        public void update(Unit carrier){
            ArrayList<Unit> hangar = stored.computeIfAbsent(carrier.id, id -> new ArrayList<>());

            // 航母死亡后，清除尚未放出的舰载机，避免残留引用。
            if(carrier.dead || carrier.health <= 0f){
                stored.remove(carrier.id);
                return;
            }

            // 统计场上同队同类单位 + 航母内部待命单位。
            int active = hangar.size();
            for(Unit other : Groups.unit){
                if(other.type == type && other.team == carrier.team) active++;
            }

            // 舰载机先在航母内部生产，不加入战场实体组。
            buildCounter += Time.delta;
            if(active < max && buildCounter >= reload){
                buildCounter = 0f;
                Unit aircraft = type.create(carrier.team);
                aircraft.x = carrier.x;
                aircraft.y = carrier.y;
                aircraft.rotation = carrier.rotation;
                hangar.add(aircraft);
                Fx.unitAssemble.at(carrier.x, carrier.y, carrier.rotation, type);
                Fx.unitPickup.at(carrier.x, carrier.y);
            }

            if(hangar.isEmpty()) return;

            // 检测范围内是否存在敌方单位。
            boolean enemyNearby = false;
            for(Unit other : Groups.unit){
                if(other.team != carrier.team && !other.dead &&
                    Mathf.dst(carrier.x, carrier.y, other.x, other.y) <= detectRange){
                    enemyNearby = true;
                    break;
                }
            }
            if(!enemyNearby) return;

            // 有敌人时，逐架从甲板滑出，避免一次性全部堆在同一位置。
            launchCounter += Time.delta;
            if(launchCounter < launchReload) return;
            launchCounter = 0f;

            Unit aircraft = hangar.remove(0);
            float angle = carrier.rotation;
            float rad = angle * Mathf.degRad;

            // 从航母尾部稍偏侧方的位置离舰。
            aircraft.x = carrier.x - Mathf.cos(rad) * (carrier.hitSize / 2f + aircraft.hitSize / 2f + 3f)
                + Mathf.cos(rad + Mathf.PI / 2f) * Mathf.range(5f);
            aircraft.y = carrier.y - Mathf.sin(rad) * (carrier.hitSize / 2f + aircraft.hitSize / 2f + 3f)
                + Mathf.sin(rad + Mathf.PI / 2f) * Mathf.range(5f);
            aircraft.rotation = angle;
            aircraft.vel.set(Mathf.cos(rad) * launchSpeed, Mathf.sin(rad) * launchSpeed);
            Fx.spawn.at(aircraft.x, aircraft.y);
            Fx.unitDrop.at(aircraft.x, aircraft.y);
            Fx.landShock.at(aircraft.x, aircraft.y);
            aircraft.add();
        }
    }

    /** 随行维修：定期给范围内同队单位回血 */
    public static class HealAbility extends Ability {
        public float reload = 60f;
        public float amount = 9f;
        public float range = 76f;
        float counter;

        @Override
        public Ability copy(){
            HealAbility copy = new HealAbility();
            copy.reload = reload;
            copy.amount = amount;
            copy.range = range;
            return copy;
        }

        @Override
        public void update(Unit unit){
            counter += Time.delta;
            if(counter < reload) return;
            counter = 0f;

            // 维修范围波纹
            Fx.healWaveDynamic.at(unit.x, unit.y, range);

            for(Unit other : Groups.unit){
                if(other == unit || other.team != unit.team) continue;
                if(other.health >= other.maxHealth) continue;
                if(Mathf.dst(unit.x, unit.y, other.x, other.y) > range) continue;
                other.health = Math.min(other.maxHealth, other.health + amount);
                Fx.heal.at(other.x, other.y);
            }
        }
    }

    /* ================= 单位字段 ================= */

    public static UnitType falcon;

    // 基础
    public static UnitType mite, crawler, dagger, mace, sabre, buster;
    // 辅助
    public static UnitType mender, aegis, envoy, nomad;
    // 空中
    public static UnitType vulture, wyvern, specter, cargo;
    // 主力
    public static UnitType leviathan, skylift, ark;

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

        weapons.add(new Weapon("nova-blaster") {{
            reload = 10;
            ejectEffect = Fx.casing1;
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
                trailEffect = Fx.colorTrail;
                trailLength = 5;
                trailWidth = 1.4f;
                trailColor = Color.valueOf("eeeeff");
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
                hitEffect = Fx.hitLaserColor;
                shootEffect = Fx.shootSmallColor;
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

        deathExplosionEffect = Fx.titanExplosion;
        deathSound = Sounds.explosion;
        trailLength = 45;
        trailColor = Color.valueOf("ffffff").a(0.6f);

        alwaysUnlocked = false;
    }};

    private static boolean loaded;

    public static void load() {
        if(loaded) return;
        loaded = true;

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

            deathExplosionEffect = Fx.blastExplosion;
            deathSound = Sounds.explosion;
            trailLength = 40;
            trailColor = Color.valueOf("8866ff").a(0.6f);

            alwaysUnlocked = false;
        }};

        /* ======================= 基础单位 ======================= */

        // 工兵：采矿+速建，只有一把小口径自卫枪
        mite = new UnitType("mite") {{
            health = 230f;
            speed = 1.95f;
            hitSize = 9f;
            armor = 1f;
            mineSpeed = 2.6f;
            mineTier = 1;
            buildSpeed = 1.6f;
            itemCapacity = 30;

            weapons.add(new Weapon("mite-gun") {{
                reload = 22f;
                x = 4f;
                y = 2f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3.4f, 9f) {{
                    width = 4f;
                    height = 6f;
                    lifetime = 18f;
                    backColor = Color.valueOf("cfd6de");
                    frontColor = Color.valueOf("ffffff");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                }};
            }});

            alwaysUnlocked = true;
        }};

        // 爬行者：最便宜的自杀冲锋单位，短射程高频率
        crawler = new UnitType("crawler") {{
            health = 160f;
            speed = 2.6f;
            hitSize = 8f;
            armor = 0f;

            weapons.add(new Weapon("crawler-claw") {{
                reload = 9f;
                x = 3f;
                y = 0f;
                bullet = new BasicBulletType(5f, 7f) {{
                    width = 3f;
                    height = 5f;
                    lifetime = 9f;
                    backColor = Color.valueOf("8e1f2e");
                    frontColor = Color.valueOf("ff8a96");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                }};
            }});

            alwaysUnlocked = true;
        }};

        // 短剑：标准步兵，双联机枪
        dagger = new UnitType("dagger") {{
            health = 320f;
            speed = 1.75f;
            hitSize = 11f;
            armor = 2f;

            weapons.add(new Weapon("dagger-gun") {{
                reload = 12f;
                x = 5f;
                y = 3f;
                mirror = true;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(4.2f, 13f) {{
                    width = 5f;
                    height = 7f;
                    lifetime = 22f;
                    backColor = Color.valueOf("8e1f2e");
                    frontColor = Color.valueOf("ff8a96");
                    trailEffect = Fx.colorTrail;
                    trailLength = 6;
                    trailWidth = 1.2f;
                    trailColor = Color.valueOf("ff8a96");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                    smokeEffect = Fx.shootBigSmoke2;
                    ammoMultiplier = 2;
                }};
            }});

            alwaysUnlocked = true;
        }};

        // 铁锤：装甲方块，主炮单发高伤
        mace = new UnitType("mace") {{
            health = 900f;
            speed = 1.05f;
            hitSize = 16f;
            armor = 6f;

            weapons.add(new Weapon("mace-cannon") {{
                reload = 46f;
                x = 7f;
                y = 0f;
                recoil = 3f;
                recoilTime = 20f;
                ejectEffect = Fx.casing2;
                bullet = new BasicBulletType(3.6f, 46f) {{
                    width = 9f;
                    height = 12f;
                    lifetime = 34f;
                    backColor = Color.valueOf("6a3025");
                    frontColor = Color.valueOf("d9774a");
                    trailEffect = Fx.colorTrail;
                    trailLength = 9;
                    trailWidth = 1.8f;
                    trailColor = Color.valueOf("d9774a");
                    hitEffect = Fx.hitBulletBig;
                    shootEffect = Fx.shootBig;
                    smokeEffect = Fx.shootBigSmoke2;
                }};
            }});

            alwaysUnlocked = false;
        }};

        // 军刀：高速近战突袭，射程极短
        sabre = new UnitType("sabre") {{
            health = 380f;
            speed = 3.1f;
            hitSize = 10f;
            armor = 1f;

            weapons.add(new Weapon("sabre-blade") {{
                reload = 7f;
                x = 4f;
                y = 0f;
                bullet = new BasicBulletType(8f, 15f) {{
                    width = 3f;
                    height = 8f;
                    lifetime = 6f;
                    backColor = Color.valueOf("cfd6de");
                    frontColor = Color.valueOf("ffffff");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                }};
            }});

            alwaysUnlocked = false;
        }};

        // 破阵：攻城单位，超长装填，一炮打穿一排
        buster = new UnitType("buster") {{
            health = 760f;
            speed = 0.92f;
            hitSize = 17f;
            armor = 5f;

            weapons.add(new Weapon("buster-siege") {{
                reload = 92f;
                x = 8f;
                y = 0f;
                recoil = 5f;
                recoilTime = 30f;
                ejectEffect = Fx.casing2;
                bullet = new BasicBulletType(3f, 95f) {{
                    width = 13f;
                    height = 16f;
                    lifetime = 44f;
                    backColor = Color.valueOf("4a0c14");
                    frontColor = Color.valueOf("ff6b53");
                    trailEffect = Fx.missileTrail;
                    trailLength = 14;
                    trailWidth = 3f;
                    trailColor = Color.valueOf("ff6b53");
                    hitEffect = Fx.bigShockwave;
                    despawnEffect = Fx.bigShockwave;
                    shootEffect = Fx.shootBig;
                    smokeEffect = Fx.shootBigSmoke2;
                }};
            }});

            alwaysUnlocked = false;
        }};

        /* ======================= 辅助单位 ======================= */

        // 修理车：范围回血，几乎无攻击
        mender = new UnitType("mender") {{
            health = 420f;
            speed = 1.65f;
            hitSize = 12f;
            armor = 2f;
            buildSpeed = 1.2f;
            itemCapacity = 40;

            abilities.add(new HealAbility() {{
                reload = 45f;
                amount = 11f;
                range = 84f;
            }});

            weapons.add(new Weapon("mender-tool") {{
                reload = 40f;
                x = 4f;
                y = 0f;
                bullet = new BasicBulletType(3f, 4f) {{
                    width = 3f;
                    height = 6f;
                    lifetime = 16f;
                    backColor = Color.valueOf("cfd6de");
                    frontColor = Color.valueOf("ffffff");
                    hitEffect = Fx.heal;
                    shootEffect = Fx.heal;
                }};
            }});

            alwaysUnlocked = false;
        }};

        // 庇护者：重甲，范围内大幅回血，自己的枪很弱
        aegis = new UnitType("aegis") {{
            health = 1650f;
            speed = 0.95f;
            hitSize = 19f;
            armor = 9f;

            abilities.add(new HealAbility() {{
                reload = 40f;
                amount = 18f;
                range = 110f;
            }});

            weapons.add(new Weapon("aegis-gun") {{
                reload = 30f;
                x = 7f;
                y = 0f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3.8f, 12f) {{
                    width = 5f;
                    height = 7f;
                    lifetime = 26f;
                    backColor = Color.valueOf("cfd6de");
                    frontColor = Color.valueOf("ffffff");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                }};
            }});

            alwaysUnlocked = false;
        }};

        // 特使：支援压制，弹体带减速，伤害很低
        envoy = new UnitType("envoy") {{
            health = 500f;
            speed = 2.1f;
            hitSize = 12f;
            armor = 3f;

            weapons.add(new Weapon("envoy-suppress") {{
                reload = 26f;
                x = 5f;
                y = 2f;
                mirror = true;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(4.6f, 6f) {{
                    width = 4f;
                    height = 8f;
                    lifetime = 30f;
                    backColor = Color.valueOf("5a6a98");
                    frontColor = Color.valueOf("a8c8ff");
                    trailEffect = Fx.colorTrail;
                    trailLength = 8;
                    trailWidth = 1.4f;
                    trailColor = Color.valueOf("a8c8ff");
                    status = StatusEffects.slow;
                    statusDuration = 150f;
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                    smokeEffect = Fx.shootBigSmoke2;
                }};
            }});

            alwaysUnlocked = false;
        }};

        // 游牧：地面后勤，背货多、挖得快，无武装
        nomad = new UnitType("nomad") {{
            health = 700f;
            speed = 1.5f;
            hitSize = 14f;
            armor = 3f;
            mineSpeed = 3.4f;
            mineTier = 2;
            buildSpeed = 2.2f;
            itemCapacity = 110;

            alwaysUnlocked = false;
        }};

        /* ======================= 空中单位 ======================= */

        // 秃鹫：廉价空优
        vulture = new UnitType("vulture") {{
            health = 360f;
            speed = 3.4f;
            accel = 0.06f;
            drag = 0.02f;
            flying = true;
            lowAltitude = true;
            rotateSpeed = 7f;
            baseRotateSpeed = 7f;
            hitSize = 11f;
            armor = 1f;

            engineOffset = 9f;
            engineSize = 2.4f;
            engineColor = Color.valueOf("bb99ff");

            weapons.add(new Weapon("vulture-gun") {{
                reload = 11f;
                x = 5f;
                y = 0f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(6f, 12f) {{
                    width = 4f;
                    height = 7f;
                    lifetime = 26f;
                    backColor = Color.valueOf("8866ff");
                    frontColor = Color.valueOf("ccbbff");
                    trailEffect = Fx.colorTrail;
                    trailLength = 7;
                    trailWidth = 1.3f;
                    trailColor = Color.valueOf("bb99ff");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                    collidesAir = true;
                    collidesGround = true;
                }};
            }});

            trailLength = 18;
            trailColor = Color.valueOf("8866ff").a(0.5f);
            alwaysUnlocked = false;
        }};

        // 飞龙：重型空袭，双联激光
        wyvern = new UnitType("wyvern") {{
            health = 1450f;
            speed = 2.3f;
            accel = 0.05f;
            drag = 0.02f;
            flying = true;
            rotateSpeed = 4.4f;
            baseRotateSpeed = 4.4f;
            hitSize = 22f;
            armor = 5f;

            engineOffset = 18f;
            engineSize = 3.6f;
            engineColor = Color.valueOf("ff8a96");

            weapons.add(new Weapon("wyvern-laser") {{
                reload = 34f;
                x = 8f;
                y = 5f;
                mirror = true;
                shoot.firstShotDelay = 15f;
                bullet = new LaserBulletType() {{
                    damage = 42;
                    length = 150f;
                    lifetime = 22f;
                    chargeEffect = Fx.lancerLaserCharge;
                    lightColor = Color.valueOf("ff8a96");
                    sideAngle = 15f;
                    sideWidth = 1.5f;
                    sideLength = 26f;
                    colors = new Color[]{
                            Color.valueOf("ff6b53"),
                            Color.valueOf("ff8a96"),
                            Color.valueOf("ffd0c0")
                    };
                    hitEffect = Fx.hitLaser;
                    shootEffect = Fx.hitLancer;
                    smokeEffect = Fx.hitLancer;
                }};
            }});

            deathExplosionEffect = Fx.blastExplosion;
            deathSound = Sounds.explosion;
            trailLength = 34;
            trailColor = Color.valueOf("ff6b53").a(0.55f);
            alwaysUnlocked = false;
        }};

        // 幽影：高速骚扰，单发穿甲
        specter = new UnitType("specter") {{
            health = 420f;
            speed = 4.6f;
            accel = 0.09f;
            drag = 0.015f;
            flying = true;
            rotateSpeed = 9f;
            baseRotateSpeed = 9f;
            hitSize = 10f;
            armor = 0f;

            engineOffset = 7f;
            engineSize = 2f;
            engineColor = Color.valueOf("a8c8ff");

            weapons.add(new Weapon("specter-shot") {{
                reload = 28f;
                x = 5f;
                y = 0f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(9f, 34f) {{
                    width = 4f;
                    height = 10f;
                    lifetime = 24f;
                    backColor = Color.valueOf("3a4a78");
                    frontColor = Color.valueOf("a8c8ff");
                    trailEffect = Fx.colorTrail;
                    trailLength = 13;
                    trailWidth = 1.8f;
                    trailColor = Color.valueOf("a8c8ff");
                    hitEffect = Fx.hitLaserBlast;
                    shootEffect = Fx.shootSmall;
                }};
            }});

            trailLength = 22;
            trailColor = Color.valueOf("a8c8ff").a(0.45f);
            alwaysUnlocked = false;
        }};

        // 货运机：空中后勤，容量最大
        cargo = new UnitType("cargo") {{
            health = 620f;
            speed = 2.6f;
            accel = 0.05f;
            drag = 0.02f;
            flying = true;
            rotateSpeed = 5f;
            baseRotateSpeed = 5f;
            hitSize = 15f;
            armor = 2f;
            mineSpeed = 3.8f;
            mineTier = 2;
            buildSpeed = 2.6f;
            itemCapacity = 160;

            engineOffset = 14f;
            engineSize = 3.2f;
            engineColor = Color.valueOf("cfd6de");

            trailLength = 24;
            trailColor = Color.valueOf("cfd6de").a(0.4f);
            alwaysUnlocked = false;
        }};

        /* ======================= 主力舰 ======================= */

        // 太空战列舰：超大血量，主炮+副炮，自带跃迁能力
        leviathan = new UnitType("leviathan") {{
            health = 7800f;
            speed = 1.15f;
            accel = 0.03f;
            drag = 0.02f;
            flying = true;
            rotateSpeed = 1.9f;
            baseRotateSpeed = 1.9f;
            hitSize = 42f;
            armor = 16f;
            itemCapacity = 200;

            engineOffset = 34f;
            engineSize = 6f;
            engineColor = Color.valueOf("c8d8e8");

            abilities.add(new WarpAbility() {{
                reload = 60f * 11f;
                distance = 220f;
            }});

            weapons.add(new Weapon("leviathan-main") {{
                reload = 55f;
                x = 16f;
                y = 0f;
                recoil = 6f;
                recoilTime = 40f;
                shoot.firstShotDelay = 22f;
                bullet = new LaserBulletType() {{
                    damage = 130;
                    length = 260f;
                    lifetime = 26f;
                    width = 12f;
                    chargeEffect = Fx.lancerLaserCharge;
                    largeHit = true;
                    lightColor = Color.valueOf("9fc4e8");
                    sideAngle = 18f;
                    sideWidth = 2f;
                    sideLength = 40f;
                    colors = new Color[]{
                            Color.valueOf("4a5a6a"),
                            Color.valueOf("9fc4e8"),
                            Color.valueOf("e8f4ff")
                    };
                    hitEffect = Fx.hitLaser;
                    shootEffect = Fx.hitLancer;
                    smokeEffect = Fx.hitLancer;
                }};
            }});

            weapons.add(new Weapon("leviathan-broadside") {{
                reload = 22f;
                x = 2f;
                y = 14f;
                mirror = true;
                rotate = true;
                baseRotation = -90f;
                ejectEffect = Fx.casing2;
                bullet = new BasicBulletType(5.5f, 28f) {{
                    width = 6f;
                    height = 10f;
                    lifetime = 38f;
                    backColor = Color.valueOf("4a5a6a");
                    frontColor = Color.valueOf("c8d8e8");
                    trailEffect = Fx.colorTrail;
                    trailLength = 9;
                    trailWidth = 1.6f;
                    trailColor = Color.valueOf("c8d8e8");
                    hitEffect = Fx.hitBulletBig;
                    shootEffect = Fx.shootBig;
                    collidesAir = true;
                    collidesGround = true;
                }};
            }});

            deathExplosionEffect = Fx.massiveExplosion;
            deathSound = Sounds.explosion;
            trailLength = 60;
            trailColor = Color.valueOf("c8d8e8").a(0.5f);
            alwaysUnlocked = false;
        }};

        // 空中航母：定期放秃鹫，自己带一门近防炮
        skylift = new UnitType("skylift") {{
            health = 4200f;
            speed = 1.3f;
            accel = 0.035f;
            drag = 0.02f;
            flying = true;
            rotateSpeed = 2.4f;
            baseRotateSpeed = 2.4f;
            hitSize = 34f;
            armor = 10f;
            itemCapacity = 120;

            engineOffset = 26f;
            engineSize = 5f;
            engineColor = Color.valueOf("9fc4e8");

            abilities.add(new CarrierAbility(vulture) {{
                max = 4;
                reload = 60f * 10f;
            }});

            weapons.add(new Weapon("skylift-ciws") {{
                reload = 9f;
                x = 6f;
                y = 0f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(6.5f, 11f) {{
                    width = 4f;
                    height = 6f;
                    lifetime = 24f;
                    backColor = Color.valueOf("3a4a78");
                    frontColor = Color.valueOf("a8c8ff");
                    trailEffect = Fx.colorTrail;
                    trailLength = 6;
                    trailWidth = 1.2f;
                    trailColor = Color.valueOf("a8c8ff");
                    hitEffect = Fx.hitBulletColor;
                    shootEffect = Fx.shootSmall;
                    collidesAir = true;
                }};
            }});

            deathExplosionEffect = Fx.titanExplosion;
            deathSound = Sounds.explosion;
            trailLength = 42;
            trailColor = Color.valueOf("9fc4e8").a(0.45f);
            alwaysUnlocked = false;
        }};

        // 普通航母：地面重甲平台，定期放爬行者，靠副炮自卫
        ark = new UnitType("ark") {{
            health = 5200f;
            speed = 0.8f;
            hitSize = 38f;
            armor = 13f;
            itemCapacity = 150;
            buildSpeed = 1.5f;

            abilities.add(new CarrierAbility(crawler) {{
                max = 6;
                reload = 60f * 9f;
            }});

            weapons.add(new Weapon("ark-turret") {{
                reload = 16f;
                x = 10f;
                y = 0f;
                rotate = true;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(4.8f, 20f) {{
                    width = 6f;
                    height = 9f;
                    lifetime = 30f;
                    backColor = Color.valueOf("4a0c14");
                    frontColor = Color.valueOf("ff8a96");
                    trailEffect = Fx.colorTrail;
                    trailLength = 8;
                    trailWidth = 1.5f;
                    trailColor = Color.valueOf("ff8a96");
                    hitEffect = Fx.hitBulletBig;
                    shootEffect = Fx.shootBig;
                    smokeEffect = Fx.shootBigSmoke2;
                }};
            }});

            deathExplosionEffect = Fx.titanExplosion;
            deathSound = Sounds.explosion;
            alwaysUnlocked = false;
        }};
    }
}