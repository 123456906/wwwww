package MirrorWorld.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.RegionPart;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.ConsumeItemFlammable;
import mindustry.world.draw.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;

import static MirrorWorld.content.MrItems.*;
import static MirrorWorld.content.MrUnits.falcon;
import static mindustry.Vars.*;

/**
 * 反地球（MirrorWorld）建筑表。
 *
 * 题材：镜潮（Mirror Tide）——一套以镜面、棱晶、回声为核心的晶体生物机械造物。
 * 命名全部采用原版的小写 kebab-case 规则，按原版分类组织：
 *
 *   存储/核心   core-echo（早期特殊核心） / core-mirror（后期特殊核心）
 *   防御       shard / facet / mirror / prism / veil 系列墙与门
 *   生产       echo / prism / lumen / abyss / mirror 系列钻头
 *   物流       mirage / prism 传送带、facet 路由器与分拣器、echo / mirror 桥
 *   液体       tide / prism 导管、echo / abyss 泵
 *   电力       lumen 节点、shard / prism 电池、glow / aurora 太阳能、ember 热能、void 反应堆
 *   制造       shard / facet / prism / crystal / mirror / alloy / veil 系列工厂与分离机
 *   效果       veil 维修、halo / aurora 超频、mirror 护盾、shard 地雷
 *   炮塔       glint / shard / facet / mirage / echo / prism / abyss / halo / veil 系列
 *   单位       shard-assembler / sky-forge 工厂，facet / prism / abyss 升级器，abyss-foundry 终局工厂
 *
 * 特殊核心（HiveCore）：
 *   1. 本体带一门可旋转的自动炮塔，会自动索敌开火；
 *   2. 只能维持 1 个“核心卫队”单位，单位死亡后从头计时重新生产；
 *   3. 核心方块写在 MrBlocks，卫队单位定义在 MrUnits，二者完全分离；
 *      guardianName 用内容名延迟解析，因此不受两个类的 load() 顺序影响。
 *
 * 注意：本文件只使用原版已验证的方块类与字段；没有提供贴图，缺少贴图时游戏会显示占位图。
 */
public class MrBlocks {

    /* ================= 原有内容 ================= */
    public static Block coreEvoke;
    public static Block arkCore;
    public static Block assimilator;
    public static Block summonTurret;

    /* ================= 特殊核心 ================= */
    public static Block coreEcho, coreMirror;

    /* ================= 防御 ================= */
    public static Block shardWall, shardWallLarge, facetWall, facetWallLarge, mirrorWall,
            prismWall, prismWallLarge, veilWall, veilWallLarge;
    public static Block shardDoor, facetDoor;

    /* ================= 生产（钻头） ================= */
    public static Block echoDrill, prismDrill, lumenDrill, abyssBore, mirrorBore;

    /* ================= 物流 ================= */
    public static Block mirageConveyor, prismConveyor, shardJunction, facetRouter, facetSorter,
            echoBridge, mirrorBridge;
    public static Block tideConduit, prismConduit, tideBridge, echoPump, abyssPump;

    /* ================= 电力 ================= */
    public static Block lumenNode, lumenTower, shardBattery, prismBattery,
            glowPanel, auroraPanel, emberVent, emberGenerator, voidReactor;

    /* ================= 制造 ================= */
    public static Block shardKiln, facetPress, prismSmelter, crystalCrucible, mirrorFoundry,
            alloyForge, veilWeaver, facetMixer, pyraMixer, cryoMixer, facetSeparator;
    public static Block echoFabricator;

    /* ================= 流体生产工厂 ================= */
    public static Block mirrorStill, lumenLeach, voidRefinery, mirrorMelter,
            phaseDissolver, echoReactor;

    /* ================= 存储 ================= */
    public static Block echoContainer, mirrorVault, abyssVault;

    /* ================= 效果 ================= */
    public static Block veilNode, haloProjector, auroraDome, mirrorShield, shardMine;
    public static Block terraformer;

    /* ================= 炮塔 ================= */
    public static Block glintTurret, shardScatter, facetLancer, mirageArc, echoRipple,
            prismCyclone, abyssLance, haloBeam, veilSegment, prismRepair;

    /* ================= 单位生产 ================= */
    public static Block shardAssembler, skyForge, facetUpgrader, prismUpgrader,
            abyssReconstructor, abyssFoundry;

    /* 核心炮塔使用的弹药 */
    public static BulletType echoCoreBullet, mirrorCoreBullet;

    private static boolean loaded;

    public static void load() {
        if(loaded) return;
        loaded = true;

        // 建造需求与配方直接引用 MrItems 的字段，确保物品/流体先创建（MrItems.load() 幂等）
        MrItems.load();

        /* ============================================================
         *  核心弹药
         * ============================================================ */
        echoCoreBullet = new BasicBulletType(6f, 36f) {{
            width = 7f;
            height = 11f;
            lifetime = 46f;
            ammoMultiplier = 1f;
            backColor = Color.valueOf("4fbfe8");
            frontColor = Color.valueOf("d8f6ff");
            trailColor = Color.valueOf("8fe4ff");
            trailEffect = Fx.colorTrail;
            trailLength = 8;
            trailWidth = 1.8f;
            hitEffect = Fx.hitBulletColor;
            despawnEffect = Fx.hitBulletColor;
            shootEffect = Fx.shootSmallColor;
            smokeEffect = Fx.shootBigSmoke2;
            collidesAir = true;
            collidesGround = true;
            splashDamage = 10f;
            splashDamageRadius = 16f;
        }};

        mirrorCoreBullet = new BasicBulletType(7f, 70f) {{
            width = 10f;
            height = 15f;
            lifetime = 55f;
            ammoMultiplier = 1f;
            backColor = Color.valueOf("8a5ad8");
            frontColor = Color.valueOf("f0e0ff");
            trailColor = Color.valueOf("c9a8ff");
            trailEffect = Fx.colorTrail;
            trailLength = 12;
            trailWidth = 2.4f;
            hitEffect = Fx.hitBulletBig;
            despawnEffect = Fx.hitBulletBig;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke2;
            collidesAir = true;
            collidesGround = true;
            splashDamage = 25f;
            splashDamageRadius = 28f;
        }};

        /* ============================================================
         *  特殊核心：炮塔 + 单单位生产
         * ============================================================ */
        coreEcho = new HiveCore("core-echo") {{
            size = 4;
            squareSprite = false;
            health = 4200;
            armor = 6f;
            itemCapacity = 8000;
            unitType = UnitTypes.alpha;
            unitCapModifier = 16;
            isFirstTier = true;
            // 根核心保持解锁，保证战役/沙盒开局有核心可用；其余方块交给科技树
            alwaysUnlocked = true;

            guardianName = "sabre";
            guardianType = MrUnits.sabre;
            guardBuildTime = 60f * 18f;
            turretRange = 190f;
            turretReload = 40f;
            turretLength = 24f;
            rotateSpeed = 4f;
            bulletType = echoCoreBullet;
            turretColor = Color.valueOf("8fe4ff");

            requirements(Category.effect, ItemStack.with(
                    mirrorShard, 6000,
                    facetPlate, 4000,
                    prismCrystal, 2500,
                    mirrorAlloy, 1200
            ));
        }};

        coreMirror = new HiveCore("core-mirror") {{
            size = 6;
            squareSprite = false;
            health = 24000;
            armor = 12f;
            itemCapacity = 16000;
            unitType = UnitTypes.gamma;
            unitCapModifier = 24;

            guardianName = "aegis";
            guardianType = MrUnits.aegis;
            guardBuildTime = 60f * 40f;
            turretRange = 280f;
            turretReload = 22f;
            turretLength = 36f;
            rotateSpeed = 3f;
            bulletType = mirrorCoreBullet;
            turretColor = Color.valueOf("c9a8ff");

            requirements(Category.effect, ItemStack.with(
                    mirrorShard, 20000,
                    facetPlate, 15000,
                    prismCrystal, 10000,
                    lumenCrystal, 6000,
                    phaseGlass, 3000,
                    resonantAlloy, 2000
            ));
        }};

        /* ============================================================
         *  原有：voke 核心 / 同化器 / 召唤炮
         * ============================================================ */
        coreEvoke = new CoreBlock("core-evoke") {{
            size = 4;
            squareSprite = false;
            health = 3600;
            itemCapacity = 4000;
            unitType = UnitTypes.evoke;
            requirements(Category.effect, ItemStack.with(
                    mirrorShard, 5000,
                    facetPlate, 3000,
                    mirrorAlloy, 1500
            ));
            requirements = ItemStack.with(
                    mirrorShard, 100,
                    facetPlate, 50,
                    prismCrystal, 25,
                    prismLens, 20
            );
        }};

        /* 可部署核心舱：航母核心代理在无核心时部署，提供真实物品存储与建造范围。不在建造菜单中显示。 */
        arkCore = new CoreBlock("ark-core") {{
            size = 3;
            squareSprite = false;
            health = 3000;
            itemCapacity = 800;
            unitType = UnitTypes.alpha;
            floating = true;
            placeableLiquid = true;
            alwaysUnlocked = false;
            buildVisibility = BuildVisibility.hidden;
        }};

        assimilator = new Assimilator("assimilator") {{
            size = 3;
            health = 400;
            range = 60f;
            description = "把血量在0到1000内的敌方单位在10到20秒内转化为我方单位并减少1/3的血量";
            requirements(Category.effect, ItemStack.with(
                    mirrorShard, 3000,
                    facetPlate, 2000,
                    prismCrystal, 2000
            ));
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
                    mirrorShard, copperBullet,
                    facetPlate, graphiteBullet,
                    prismCrystal, siliconBullet
            );

            requirements(Category.turret, ItemStack.with(
                    mirrorShard, 500,
                    facetPlate, 350,
                    prismCrystal, 250,
                    mirrorAlloy, 150,
                    prismLens, 100
            ));
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

        /* ============================================================
         *  防御：镜潮墙体
         * ============================================================ */
        shardWall = new Wall("shard-wall") {{
            requirements(Category.defense, ItemStack.with(mirrorShard, 6, facetPlate, 2));
            health = 320;
        }};

        shardWallLarge = new Wall("shard-wall-large") {{
            requirements(Category.defense, ItemStack.mult(shardWall.requirements, 4));
            health = 1280;
            size = 2;
        }};

        facetWall = new Wall("facet-wall") {{
            requirements(Category.defense, ItemStack.with(facetPlate, 6, prismCrystal, 3));
            health = 520;
            chanceDeflect = 5f;
        }};

        facetWallLarge = new Wall("facet-wall-large") {{
            requirements(Category.defense, ItemStack.mult(facetWall.requirements, 4));
            health = 2080;
            size = 2;
            chanceDeflect = 5f;
        }};

        mirrorWall = new Wall("mirror-wall") {{
            requirements(Category.defense, ItemStack.with(mirrorAlloy, 8, facetPlate, 6, prismLens, 4));
            health = 760;
            insulated = true;
            absorbLasers = true;
        }};

        prismWall = new Wall("prism-wall") {{
            requirements(Category.defense, ItemStack.with(prismCrystal, 8, phaseGlass, 6, prismLens, 4));
            health = 1400;
            insulated = true;
            absorbLasers = true;
            lightningChance = 0.04f;
        }};

        prismWallLarge = new Wall("prism-wall-large") {{
            requirements(Category.defense, ItemStack.mult(prismWall.requirements, 4));
            health = 5600;
            size = 2;
            insulated = true;
            absorbLasers = true;
            lightningChance = 0.04f;
        }};

        veilWall = new Wall("veil-wall") {{
            requirements(Category.defense, ItemStack.with(veilFiber, 8, resonantAlloy, 4, phaseGlass, 6));
            health = 2600;
            chanceDeflect = 15f;
            flashHit = true;
            insulated = true;
            absorbLasers = true;
        }};

        veilWallLarge = new Wall("veil-wall-large") {{
            requirements(Category.defense, ItemStack.mult(veilWall.requirements, 4));
            health = 10400;
            size = 2;
            chanceDeflect = 15f;
            flashHit = true;
            insulated = true;
            absorbLasers = true;
        }};

        shardDoor = new Door("shard-door") {{
            requirements(Category.defense, ItemStack.with(mirrorShard, 6, prismCrystal, 4));
            health = 400;
        }};

        facetDoor = new Door("facet-door") {{
            requirements(Category.defense, ItemStack.mult(shardDoor.requirements, 4));
            health = 1600;
            size = 2;
            openfx = Fx.dooropenlarge;
            closefx = Fx.doorcloselarge;
        }};

        /* ============================================================
         *  生产：回声 / 棱晶 / 流明 / 深渊钻头
         * ============================================================ */
        echoDrill = new Drill("echo-drill") {{
            requirements(Category.production, ItemStack.with(mirrorShard, 16, facetPlate, 10));
            tier = 2;
            drillTime = 540f;
            size = 2;
            itemCapacity = 10;
            consumeLiquid(Liquids.water, 0.05f).boost();
        }};

        prismDrill = new Drill("prism-drill") {{
            requirements(Category.production, ItemStack.with(mirrorShard, 24, facetPlate, 14, prismCrystal, 10));
            tier = 3;
            drillTime = 360f;
            size = 2;
            itemCapacity = 12;
            consumeLiquid(Liquids.water, 0.07f).boost();
        }};

        lumenDrill = new Drill("lumen-drill") {{
            requirements(Category.production, ItemStack.with(mirrorShard, 40, prismCrystal, 35, lumenThread, 35, lumenCrystal, 25));
            tier = 4;
            drillTime = 250f;
            size = 3;
            hasPower = true;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;
            rotateSpeed = 5f;
            consumePower(1.5f);
            consumeLiquid(cryoPrism, 0.08f).boost();
        }};

        abyssBore = new BurstDrill("abyss-bore") {{
            requirements(Category.production, ItemStack.with(prismCrystal, 90, lumenCrystal, 70, voidOre, 45));
            tier = 5;
            drillTime = 60f * 10f;
            size = 4;
            hasPower = true;
            itemCapacity = 40;
            shake = 3f;
            arrows = 3;
            arrowSpacing = 5f;
            arrowOffset = 1f;
            glowColor.a = 0.55f;
            drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam, Fx.mineBig);
            liquidBoostIntensity = 1.7f;
            consumePower(4f);
            consumeLiquid(cryoPrism, 0.12f).boost();
        }};

        mirrorBore = new BurstDrill("mirror-bore") {{
            requirements(Category.production, ItemStack.with(prismCrystal, 180, lumenCrystal, 140, voidOre, 100, phaseGlass, 40));
            tier = 6;
            drillTime = 60f * 8f;
            size = 5;
            hasPower = true;
            itemCapacity = 60;
            shake = 4f;
            arrows = 4;
            arrowSpacing = 6f;
            arrowOffset = 2f;
            glowColor.a = 0.65f;
            drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam, Fx.mineHuge);
            liquidBoostIntensity = 1.9f;
            consumePower(7f);
            consumeLiquid(cryoPrism, 0.16f).boost();
        }};

        /* ============================================================
         *  物流：传送带与路由
         * ============================================================ */
        mirageConveyor = new Conveyor("mirage-conveyor") {{
            requirements(Category.distribution, ItemStack.with(mirrorShard, 1, facetPlate, 1));
            health = 60;
            speed = 0.055f;
            displayedSpeed = 7f;
        }};

        prismConveyor = new Conveyor("prism-conveyor") {{
            requirements(Category.distribution, ItemStack.with(prismCrystal, 1, phaseGlass, 1, lumenThread, 1));
            health = 95;
            speed = 0.09f;
            displayedSpeed = 12f;
        }};

        shardJunction = new Junction("shard-junction") {{
            requirements(Category.distribution, ItemStack.with(mirrorShard, 4, facetPlate, 2));
            speed = 32f;
            capacity = 8;
            health = 45;
        }};

        facetRouter = new Router("facet-router") {{
            requirements(Category.distribution, ItemStack.with(mirrorShard, 4, facetPlate, 2));
            health = 90;
        }};

        facetSorter = new Sorter("facet-sorter") {{
            requirements(Category.distribution, ItemStack.with(facetPlate, 3, mirrorShard, 3, prismCrystal, 1));
            health = 90;
        }};

        echoBridge = new BufferedItemBridge("echo-bridge") {{
            requirements(Category.distribution, ItemStack.with(facetPlate, 6, mirrorShard, 6, prismLens, 2));
            range = 6;
            speed = 80f;
            bufferCapacity = 16;
            arrowSpacing = 6f;
            fadeIn = moveArrows = false;
        }};

        mirrorBridge = new ItemBridge("mirror-bridge") {{
            requirements(Category.distribution, ItemStack.with(phaseGlass, 4, lumenThread, 8, mirrorAlloy, 6));
            range = 14;
            transportTime = 2f;
            arrowPeriod = 0.9f;
            arrowTimeScl = 2.75f;
            pulse = true;
            hasPower = true;
            consumePower(0.25f);
        }};

        /* ============================================================
         *  液体：潮汐导管与泵
         * ============================================================ */
        tideConduit = new Conduit("tide-conduit") {{
            requirements(Category.liquid, ItemStack.with(phaseGlass, 1, facetPlate, 1));
            liquidCapacity = 30f;
            health = 50;
        }};

        prismConduit = new Conduit("prism-conduit") {{
            requirements(Category.liquid, ItemStack.with(mirrorAlloy, 2, phaseGlass, 2));
            liquidCapacity = 60f;
            liquidPressure = 1.05f;
            health = 110;
        }};

        tideBridge = new LiquidBridge("tide-bridge") {{
            requirements(Category.liquid, ItemStack.with(prismLens, 5, phaseGlass, 8));
            range = 6;
            liquidCapacity = 120f;
            floating = true;
            fadeIn = moveArrows = false;
            arrowSpacing = 6f;
        }};

        echoPump = new Pump("echo-pump") {{
            requirements(Category.liquid, ItemStack.with(mirrorShard, 18, phaseGlass, 12));
            pumpAmount = 8f / 60f;
            liquidCapacity = 30f;
        }};

        abyssPump = new Pump("abyss-pump") {{
            requirements(Category.liquid, ItemStack.with(mirrorShard, 80, phaseGlass, 90, lumenThread, 40, mirrorAlloy, 60, voidOre, 35));
            pumpAmount = 0.24f;
            liquidCapacity = 240f;
            size = 3;
            hasPower = true;
            consumePower(1.5f);
        }};

        /* ============================================================
         *  电力：流明网络
         * ============================================================ */
        lumenNode = new PowerNode("lumen-node") {{
            requirements(Category.power, ItemStack.with(mirrorShard, 3, facetPlate, 6));
            maxNodes = 12;
            laserRange = 8f;
            underBullets = true;
        }};

        lumenTower = new PowerNode("lumen-tower") {{
            requirements(Category.power, ItemStack.with(mirrorAlloy, 8, facetPlate, 12, lumenThread, 6));
            size = 2;
            maxNodes = 18;
            laserRange = 22f;
        }};

        shardBattery = new Battery("shard-battery") {{
            requirements(Category.power, ItemStack.with(mirrorShard, 8, facetPlate, 20));
            consumePowerBuffered(6000f);
            baseExplosiveness = 2f;
        }};

        prismBattery = new Battery("prism-battery") {{
            requirements(Category.power, ItemStack.with(mirrorAlloy, 30, facetPlate, 60, lumenThread, 40, resonanceCell, 10));
            size = 3;
            consumePowerBuffered(60000f);
            baseExplosiveness = 6f;
        }};

        glowPanel = new SolarGenerator("glow-panel") {{
            requirements(Category.power, ItemStack.with(mirrorShard, 20, lumenThread, 12, phaseGlass, 10));
            powerProduction = 0.7f;
        }};

        auroraPanel = new SolarGenerator("aurora-panel") {{
            requirements(Category.power, ItemStack.with(mirrorAlloy, 60, lumenThread, 45, phaseGlass, 50, resonanceCell, 10));
            size = 3;
            powerProduction = 3.4f;
        }};

        emberVent = new ThermalGenerator("ember-vent") {{
            requirements(Category.power, ItemStack.with(mirrorShard, 50, prismLens, 40, facetPlate, 50, lumenThread, 40, phaseGlass, 45));
            powerProduction = 2.4f;
            generateEffect = Fx.redgeneratespark;
            effectChance = 0.011f;
            size = 2;
            floating = true;
            ambientSound = Sounds.loopHum;
            ambientSoundVolume = 0.06f;
        }};

        emberGenerator = new ConsumeGenerator("ember-generator") {{
            requirements(Category.power, ItemStack.with(mirrorShard, 30, facetPlate, 20, prismLens, 10));
            powerProduction = 1.8f;
            itemDuration = 100f;
            explosionShake = 1f;
            generateEffect = Fx.generatespark;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.03f;
            consume(new ConsumeItemFlammable());
            drawer = new DrawMulti(new DrawDefault(), new DrawWarmupRegion());
        }};

        voidReactor = new ImpactReactor("void-reactor") {{
            requirements(Category.power, ItemStack.with(facetPlate, 500, lumenThread, 300, prismLens, 400, voidOre, 120, resonantAlloy, 250, phaseGlass, 250));
            size = 4;
            health = 950;
            powerProduction = 95f;
            itemDuration = 170f;
            liquidCapacity = 100f;
            ambientSound = Sounds.loopPulse;
            ambientSoundVolume = 0.08f;
            consumePower(22f);
            consumeItem(resonanceCell);
            consumeLiquid(cryoPrism, 0.22f);
        }};

        /* ============================================================
         *  制造：镜潮工厂
         * ============================================================ */
        shardKiln = new GenericCrafter("shard-kiln") {{
            requirements(Category.crafting, ItemStack.with(Items.copper, 60, Items.lead, 25));
            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(mirrorShard, 2);
            craftTime = 60f;
            size = 2;
            hasItems = true;
            consumeItems(ItemStack.with(Items.sand, 2, Items.copper, 1));
        }};

        facetPress = new GenericCrafter("facet-press") {{
            requirements(Category.crafting, ItemStack.with(mirrorShard, 100, Items.lead, 100, Items.copper, 50));
            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(facetPlate, 2);
            craftTime = 50f;
            itemCapacity = 20;
            size = 3;
            hasItems = true;
            hasPower = true;
            consumePower(1.6f);
            consumeItem(mirrorShard, 3);
        }};

        prismSmelter = new GenericCrafter("prism-smelter") {{
            requirements(Category.crafting, ItemStack.with(mirrorShard, 35, facetPlate, 30));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(prismCrystal, 2);
            craftTime = 40f;
            size = 2;
            hasItems = true;
            hasPower = true;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame(Color.valueOf("ffef99")));
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;
            consumeItems(ItemStack.with(mirrorShard, 3, Items.lead, 1));
            consumePower(0.6f);
        }};

        crystalCrucible = new GenericCrafter("crystal-crucible") {{
            requirements(Category.crafting, ItemStack.with(prismCrystal, 120, facetPlate, 80, Items.titanium, 60));
            craftEffect = Fx.smeltsmoke;
            outputItems = ItemStack.with(lumenCrystal, 2, resonanceCell, 1);
            craftTime = 80f;
            size = 3;
            itemCapacity = 30;
            hasItems = true;
            hasPower = true;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame(Color.valueOf("ffef99")));
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;
            consumeItems(ItemStack.with(prismCrystal, 2, Items.sand, 3));
            consumePower(3f);
        }};

        mirrorFoundry = new GenericCrafter("mirror-foundry") {{
            requirements(Category.crafting, ItemStack.with(lumenCrystal, 60, prismCrystal, 40, facetPlate, 30));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(phaseGlass, 2);
            craftTime = 70f;
            size = 2;
            hasItems = true;
            hasPower = true;
            consumeItems(ItemStack.with(lumenCrystal, 1, facetPlate, 2));
            consumePower(1.2f);
        }};

        alloyForge = new GenericCrafter("alloy-forge") {{
            requirements(Category.crafting, ItemStack.with(facetPlate, 150, prismCrystal, 120, lumenCrystal, 80, phaseGlass, 60));
            craftEffect = Fx.smeltsmoke;
            outputItems = ItemStack.with(mirrorAlloy, 2, resonantAlloy, 1);
            craftTime = 120f;
            size = 3;
            itemCapacity = 30;
            hasItems = true;
            hasPower = true;
            hasLiquids = true;
            liquidCapacity = 60f;
            consumeItems(ItemStack.with(facetPlate, 4, prismCrystal, 2, phaseGlass, 1));
            consumeLiquid(cryoPrism, 0.1f);
            consumePower(4f);
        }};

        veilWeaver = new GenericCrafter("veil-weaver") {{
            requirements(Category.crafting, ItemStack.with(mirrorAlloy, 120, lumenCrystal, 100, phaseGlass, 80, prismLens, 60));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(veilFiber, 1);
            craftTime = 150f;
            size = 3;
            itemCapacity = 30;
            hasItems = true;
            hasPower = true;
            hasLiquids = true;
            liquidCapacity = 60f;
            consumeItems(ItemStack.with(lumenCrystal, 3, mirrorAlloy, 1));
            consumeLiquid(cryoPrism, 0.12f);
            consumePower(5f);
        }};

        facetMixer = new GenericCrafter("facet-mixer") {{
            requirements(Category.crafting, ItemStack.with(prismCrystal, 60, lumenCrystal, 40, facetPlate, 30));
            craftEffect = Fx.smeltsmoke;
            outputItems = ItemStack.with(prismLens, 1, echoChip, 1);
            craftTime = 60f;
            size = 2;
            hasItems = true;
            hasPower = true;
            consumeItems(ItemStack.with(prismCrystal, 2, mirrorAlloy, 1));
            consumePower(1.5f);
        }};

        pyraMixer = new GenericCrafter("pyra-mixer") {{
            requirements(Category.crafting, ItemStack.with(prismCrystal, 50, facetPlate, 30));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(lumenThread, 1);
            craftTime = 50f;
            size = 2;
            hasItems = true;
            hasPower = true;
            consumeItems(ItemStack.with(lumenCrystal, 1, prismLens, 1, Items.coal, 1));
            consumePower(0.8f);
        }};

        cryoMixer = new GenericCrafter("cryo-mixer") {{
            requirements(Category.crafting, ItemStack.with(lumenCrystal, 80, prismCrystal, 60, phaseGlass, 40));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(cryoPrism, 0.3f);
            craftTime = 60f;
            size = 2;
            liquidCapacity = 60f;
            hasLiquids = true;
            hasPower = true;
            consumeItems(ItemStack.with(lumenCrystal, 1));
            consumeLiquid(Liquids.water, 0.3f);
            consumePower(1f);
        }};

        /* ============================================================
         *  流体生产工厂：把矿石/晶体加工成地形湖里能抽到的镜潮流体
         * ============================================================ */
        mirrorStill = new GenericCrafter("mirror-still") {{
            requirements(Category.crafting, ItemStack.with(facetPlate, 60, mirrorShard, 40, Items.lead, 40));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(mirrorFluid, 0.25f);
            craftTime = 50f;
            size = 2;
            itemCapacity = 20;
            liquidCapacity = 60f;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            consumeItems(ItemStack.with(mirrorShard, 2));
            consumeLiquid(Liquids.water, 0.3f);
            consumePower(1.2f);
        }};

        lumenLeach = new GenericCrafter("lumen-leach") {{
            requirements(Category.crafting, ItemStack.with(prismCrystal, 80, facetPlate, 60, Items.lead, 40));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(lumenSolution, 0.25f);
            craftTime = 70f;
            size = 3;
            itemCapacity = 20;
            liquidCapacity = 60f;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            consumeItems(ItemStack.with(lumenCrystal, 1, Items.sand, 1));
            consumeLiquid(Liquids.water, 0.35f);
            consumePower(2f);
        }};

        voidRefinery = new GenericCrafter("void-refinery") {{
            requirements(Category.crafting, ItemStack.with(mirrorAlloy, 80, phaseGlass, 60, prismLens, 40));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(voidEssence, 0.2f);
            craftTime = 80f;
            size = 3;
            itemCapacity = 20;
            liquidCapacity = 60f;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            consumeItems(ItemStack.with(voidOre, 2));
            consumeLiquid(cryoPrism, 0.1f);
            consumePower(4f);
        }};

        mirrorMelter = new GenericCrafter("mirror-melter") {{
            requirements(Category.crafting, ItemStack.with(facetPlate, 80, prismCrystal, 50, Items.titanium, 80));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(moltenMirror, 0.2f);
            craftTime = 60f;
            size = 2;
            itemCapacity = 20;
            liquidCapacity = 60f;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;
            consumeItems(ItemStack.with(mirrorShard, 3));
            consumePower(2.5f);
        }};

        phaseDissolver = new GenericCrafter("phase-dissolver") {{
            requirements(Category.crafting, ItemStack.with(phaseGlass, 100, mirrorAlloy, 60, echoChip, 20));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(phaseSolvent, 0.3f);
            craftTime = 60f;
            size = 3;
            itemCapacity = 20;
            liquidCapacity = 60f;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            consumeItems(ItemStack.with(phaseGlass, 1));
            consumeLiquid(Liquids.water, 0.4f);
            consumePower(2f);
        }};

        echoReactor = new GenericCrafter("echo-reactor") {{
            requirements(Category.crafting, ItemStack.with(resonantAlloy, 120, resonanceCell, 40, echoChip, 50));
            craftEffect = Fx.smeltsmoke;
            outputLiquid = new LiquidStack(echoPlasma, 0.15f);
            craftTime = 100f;
            size = 4;
            itemCapacity = 20;
            liquidCapacity = 80f;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;
            consumeItems(ItemStack.with(echoChip, 1));
            consumeLiquid(voidEssence, 0.2f);
            consumePower(8f);
        }};

        facetSeparator = new Separator("facet-separator") {{
            requirements(Category.crafting, ItemStack.with(facetPlate, 40, prismCrystal, 35, lumenCrystal, 25));
            results = ItemStack.with(
                    mirrorShard, 6,
                    prismCrystal, 4,
                    lumenCrystal, 2,
                    voidOre, 2,
                    abyssOre, 1,
                    abyssCore, 1
            );
            hasPower = true;
            craftTime = 35f;
            size = 2;
            consumePower(1.2f);
            consumeLiquid(Liquids.slag, 4f / 60f);
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(), new DrawRegion("-spinner", 3, true), new DrawDefault());
        }};

        /* ============================================================
         *  多配方工厂：点击方块在配方间切换，介绍页列出全部配方与制作时间
         * ============================================================ */
        echoFabricator = new Fabricator("echo-fabricator") {{
            requirements(Category.crafting, ItemStack.with(facetPlate, 120, prismCrystal, 80, echoChip, 40));
            size = 3;
            scaledHealth = 150;
            itemCapacity = 40;

            recipes = new Recipe[]{
                    new Recipe(
                            ItemStack.with(facetPlate, 2, prismCrystal, 1),
                            new ItemStack(prismLens, 1),
                            60f
                    ),
                    new Recipe(
                            ItemStack.with(prismLens, 1, lumenThread, 2),
                            new ItemStack(echoChip, 1),
                            90f
                    ),
                    new Recipe(
                            ItemStack.with(mirrorAlloy, 2, phaseGlass, 1),
                            new ItemStack(veilFiber, 1),
                            120f
                    )
            };
        }};

        /* ============================================================
         *  存储
         * ============================================================ */
        echoContainer = new StorageBlock("echo-container") {{
            requirements(Category.effect, ItemStack.with(facetPlate, 120, prismCrystal, 60));
            size = 2;
            itemCapacity = 400;
            scaledHealth = 60;
        }};

        mirrorVault = new StorageBlock("mirror-vault") {{
            requirements(Category.effect, ItemStack.with(mirrorAlloy, 300, lumenCrystal, 150, prismLens, 200));
            size = 3;
            itemCapacity = 1500;
            scaledHealth = 60;
        }};

        abyssVault = new StorageBlock("abyss-vault") {{
            requirements(Category.effect, ItemStack.with(voidOre, 600, phaseGlass, 200, resonantAlloy, 150, prismLens, 500, echoChip, 200, abyssCore, 120));
            size = 4;
            itemCapacity = 5000;
            scaledHealth = 60;
        }};

        /* ============================================================
         *  效果：维修 / 超频 / 护盾 / 地雷
         * ============================================================ */
        veilNode = new MendProjector("veil-node") {{
            requirements(Category.effect, ItemStack.with(facetPlate, 40, mirrorShard, 30, lumenThread, 20));
            consumePower(0.4f);
            size = 1;
            reload = 180f;
            range = 55f;
            healPercent = 6f;
            phaseBoost = 5f;
            phaseRangeBoost = 25f;
            health = 100;
            consumeItem(prismCrystal).boost();
        }};

        haloProjector = new OverdriveProjector("halo-projector") {{
            requirements(Category.effect, ItemStack.with(facetPlate, 120, mirrorAlloy, 80, lumenThread, 80, phaseGlass, 35));
            consumePower(2.5f);
            size = 2;
            range = 100f;
            speedBoost = 1.8f;
            useTime = 260f;
            consumeItem(veilFiber).boost();
            ambientSoundVolume = 0.08f;
        }};

        auroraDome = new OverdriveProjector("aurora-dome") {{
            requirements(Category.effect, ItemStack.with(facetPlate, 250, mirrorAlloy, 160, lumenThread, 160, phaseGlass, 100, resonantAlloy, 150, echoChip, 100));
            consumePower(9f);
            size = 3;
            range = 220f;
            speedBoost = 2.6f;
            useTime = 320f;
            hasBoost = false;
            consumeItems(ItemStack.with(veilFiber, 1, lumenThread, 1));
            ambientSoundVolume = 0.12f;
        }};

        mirrorShield = new ForceProjector("mirror-shield") {{
            requirements(Category.effect, ItemStack.with(facetPlate, 150, mirrorAlloy, 100, lumenThread, 150, phaseGlass, 80));
            size = 3;
            phaseRadiusBoost = 90f;
            radius = 110f;
            shieldHealth = 1200f;
            cooldownNormal = 1.5f;
            cooldownLiquid = 1.2f;
            cooldownBrokenBase = 0.3f;
            itemConsumer = consumeItem(veilFiber).boost();
            consumePower(5f);
        }};

        shardMine = new ShockMine("shard-mine") {{
            requirements(Category.effect, ItemStack.with(mirrorShard, 30, prismCrystal, 15, prismLens, 10));
            hasShadow = false;
            health = 60;
            damage = 30f;
            tileDamage = 8f;
            length = 12;
            tendrils = 5;
        }};

        /* ============================================================
         *  地形改造器：消耗物品，把范围内会扣血的菌丝地板/墙体换成安全方块
         * ============================================================ */
        terraformer = new Terraformer("terraformer") {{
            requirements(Category.effect, ItemStack.with(facetPlate, 80, prismCrystal, 40, lumenCrystal, 20));
            size = 2;
            scaledHealth = 120;
            itemCapacity = 10;

            useItem = facetPlate;
            itemsPerUse = 1;
            useTime = 120f;
            range = 3;
            replaceFloor = AntichthonBlocks.redSand;

            targetFloors.add(
                    AntichthonBlocks.crimsonCarpet,
                    AntichthonBlocks.whiteStalkCarpet
            );
            targetWalls.add(
                    AntichthonBlocks.crimsonCarpetWall,
                    AntichthonBlocks.whiteStalkCarpetWall,
                    AntichthonBlocks.crimsonVeinWall,
                    AntichthonBlocks.crimsonBloomWall
            );
        }};

        /* ============================================================
         *  炮塔：微光 / 棱晶 / 回响 / 深渊 / 光晕
         * ============================================================ */
        glintTurret = new ItemTurret("glint-turret") {{
            requirements(Category.turret, ItemStack.with(mirrorShard, 50, facetPlate, 35, prismCrystal, 25));
            size = 2;
            health = 260;
            range = 170f;
            reload = 20f;
            shootCone = 15f;
            rotateSpeed = 10f;
            recoil = 1.5f;
            shootY = 3f;
            inaccuracy = 2f;
            ammoUseEffect = Fx.casing1;
            shootSound = Sounds.shootDuo;
            coolant = consumeCoolant(0.1f);
            coolantMultiplier = 5f;

            ammo(
                    mirrorShard, new BasicBulletType(4f, 14) {{
                        width = 6f;
                        height = 9f;
                        lifetime = 55f;
                        ammoMultiplier = 2f;
                        backColor = Color.valueOf("4fbfe8");
                        frontColor = Color.valueOf("d8f6ff");
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                    }},
                    prismCrystal, new BasicBulletType(5f, 26) {{
                        width = 8f;
                        height = 11f;
                        lifetime = 55f;
                        ammoMultiplier = 4f;
                        reloadMultiplier = 0.8f;
                        rangeChange = 16f;
                        backColor = Color.valueOf("6a6a7a");
                        frontColor = Color.valueOf("d8d8e8");
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                    }},
                    lumenThread, new BasicBulletType(4.5f, 18) {{
                        width = 6f;
                        height = 9f;
                        lifetime = 58f;
                        ammoMultiplier = 5f;
                        reloadMultiplier = 1.4f;
                        homingPower = 0.2f;
                        homingRange = 40f;
                        trailLength = 6;
                        trailWidth = 1.4f;
                        backColor = Color.valueOf("8a5ad8");
                        frontColor = Color.valueOf("f0e0ff");
                        hitEffect = despawnEffect = Fx.hitBulletColor;
                    }}
            );
        }};

        shardScatter = new ItemTurret("shard-scatter") {{
            requirements(Category.turret, ItemStack.with(mirrorShard, 100, facetPlate, 55, prismCrystal, 30));
            size = 2;
            scaledHealth = 240;
            range = 185f;
            reload = 18f;
            shootCone = 25f;
            rotateSpeed = 9f;
            recoil = 1.5f;
            shootSound = Sounds.shootScatter;
            targetAir = true;
            targetGround = true;

            ammo(
                    mirrorShard, new FlakBulletType(4f, 4) {{
                        width = 7f;
                        height = 9f;
                        lifetime = 60f;
                        ammoMultiplier = 5f;
                        reloadMultiplier = 0.6f;
                        splashDamage = 8f;
                        splashDamageRadius = 18f;
                        shootEffect = Fx.shootSmall;
                        hitEffect = despawnEffect = Fx.flakExplosion;
                    }},
                    phaseGlass, new FlakBulletType(4.5f, 7) {{
                        width = 7f;
                        height = 9f;
                        lifetime = 60f;
                        ammoMultiplier = 5f;
                        splashDamage = 12f;
                        splashDamageRadius = 22f;
                        shootEffect = Fx.shootSmall;
                        hitEffect = despawnEffect = Fx.flakExplosion;
                    }},
                    lumenCrystal, new FlakBulletType(4.2f, 9) {{
                        width = 8f;
                        height = 10f;
                        lifetime = 60f;
                        ammoMultiplier = 4f;
                        splashDamage = 16f;
                        splashDamageRadius = 26f;
                        status = StatusEffects.burning;
                        statusDuration = 120f;
                        shootEffect = Fx.shootSmall;
                        hitEffect = despawnEffect = Fx.flakExplosion;
                    }}
            );
        }};

        facetLancer = new PowerTurret("facet-lancer") {{
            requirements(Category.turret, ItemStack.with(mirrorShard, 80, facetPlate, 90, prismCrystal, 80, mirrorAlloy, 40));
            size = 2;
            scaledHealth = 280;
            range = 175f;
            reload = 80f;
            shootCone = 15f;
            rotateSpeed = 6f;
            recoil = 2f;
            shake = 2f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            heatColor = Color.valueOf("a8e8ff");
            chargeSound = Sounds.chargeLancer;
            shootSound = Sounds.shootLancer;
            moveWhileCharging = false;
            accurateDelay = false;
            consumePower(6f);
            coolant = consumeCoolant(0.2f);
            shoot.firstShotDelay = 40f;

            shootType = new LaserBulletType(130f) {{
                colors = new Color[]{
                        Color.valueOf("8fe4ff").a(0.4f),
                        Color.valueOf("8fe4ff"),
                        Color.white
                };
                chargeEffect = Fx.lancerLaserCharge;
                buildingDamageMultiplier = 0.25f;
                armorMultiplier = 4f;
                hitEffect = Fx.hitLancer;
                hitSize = 4f;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 190f;
                ammoMultiplier = 1f;
                pierceCap = 4;
            }};
        }};

        mirageArc = new PowerTurret("mirage-arc") {{
            requirements(Category.turret, ItemStack.with(mirrorShard, 70, facetPlate, 70, prismCrystal, 50));
            size = 2;
            scaledHealth = 230;
            range = 120f;
            reload = 32f;
            shootCone = 40f;
            rotateSpeed = 8f;
            targetAir = false;
            shootSound = Sounds.shootArc;
            consumePower(3f);

            shootType = new LightningBulletType() {{
                damage = 15f;
                lightningLength = 24;
                collidesAir = false;
                ammoMultiplier = 1f;
                buildingDamageMultiplier = 0.4f;
                pierceCap = 5;
                lightningType = new BulletType(0.0001f, 0f) {{
                    lifetime = Fx.lightning.lifetime;
                    hitEffect = Fx.hitLancer;
                    despawnEffect = Fx.none;
                    status = StatusEffects.shocked;
                    hittable = false;
                    lightColor = Color.white;
                    collidesAir = false;
                    buildingDamageMultiplier = 0.4f;
                    shieldDamageMultiplier = 0.2f;
                }};
            }};
        }};

        echoRipple = new ItemTurret("echo-ripple") {{
            requirements(Category.turret, ItemStack.with(mirrorShard, 180, prismCrystal, 150, mirrorAlloy, 80));
            size = 3;
            scaledHealth = 350;
            range = 310f;
            reload = 90f;
            shootCone = 10f;
            rotateSpeed = 4f;
            targetAir = false;
            shootSound = Sounds.shootRipple;

            ammo(
                    prismCrystal, new ArtilleryBulletType(3f, 45) {{
                        hitEffect = new MultiEffect(Fx.flakExplosion, Fx.shockwave);
                        knockback = 0.8f;
                        lifetime = 90f;
                        width = 12f;
                        height = 14f;
                        collidesTiles = false;
                        splashDamageRadius = 26f;
                        splashDamage = 75f;
                        backColor = Color.valueOf("6a6a7a");
                        frontColor = Color.valueOf("d8d8e8");
                        despawnEffect = Fx.hitBulletColor;
                    }},
                    lumenCrystal, new ArtilleryBulletType(3f, 45) {{
                        hitEffect = new MultiEffect(Fx.flakExplosion, Fx.shockwave);
                        knockback = 0.8f;
                        lifetime = 90f;
                        width = 12f;
                        height = 14f;
                        collidesTiles = false;
                        splashDamageRadius = 28f;
                        splashDamage = 70f;
                        reloadMultiplier = 1.2f;
                        ammoMultiplier = 3f;
                        homingPower = 0.08f;
                        homingRange = 50f;
                        trailLength = 9;
                        trailWidth = 3f;
                        backColor = Color.valueOf("8a5ad8");
                        frontColor = Color.valueOf("f0e0ff");
                        despawnEffect = Fx.hitBulletColor;
                    }}
            );
        }};

        prismCyclone = new ItemTurret("prism-cyclone") {{
            requirements(Category.turret, ItemStack.with(mirrorAlloy, 200, prismCrystal, 150, phaseGlass, 80));
            size = 3;
            scaledHealth = 300;
            range = 220f;
            reload = 10f;
            shootCone = 30f;
            rotateSpeed = 8f;
            inaccuracy = 8f;
            shootSound = Sounds.shootCyclone;
            targetAir = true;
            targetGround = true;
            coolant = consumeCoolant(0.1f);

            ammo(
                    phaseGlass, new FlakBulletType(5f, 8) {{
                        width = 8f;
                        height = 10f;
                        lifetime = 60f;
                        ammoMultiplier = 5f;
                        splashDamage = 14f;
                        splashDamageRadius = 24f;
                        hitEffect = despawnEffect = Fx.flakExplosion;
                    }},
                    lumenCrystal, new FlakBulletType(4.8f, 12) {{
                        width = 9f;
                        height = 11f;
                        lifetime = 60f;
                        ammoMultiplier = 4f;
                        splashDamage = 20f;
                        splashDamageRadius = 30f;
                        status = StatusEffects.burning;
                        statusDuration = 150f;
                        hitEffect = despawnEffect = Fx.flakExplosion;
                    }}
            );
        }};

        abyssLance = new ItemTurret("abyss-lance") {{
            requirements(Category.turret, ItemStack.with(voidOre, 350, prismCrystal, 300, phaseGlass, 200, resonantAlloy, 100));
            size = 4;
            scaledHealth = 420;
            range = 430f;
            reload = 130f;
            shootCone = 8f;
            rotateSpeed = 3f;
            targetAir = false;
            shake = 3f;
            shootSound = Sounds.shootArtillerySmall;

            ammo(
                    voidOre, new ArtilleryBulletType(3.4f, 140) {{
                        hitEffect = new MultiEffect(Fx.blastExplosion, Fx.bigShockwave);
                        despawnEffect = Fx.bigShockwave;
                        knockback = 1.2f;
                        lifetime = 120f;
                        width = 16f;
                        height = 20f;
                        collidesTiles = false;
                        splashDamageRadius = 45f;
                        splashDamage = 220f;
                        trailLength = 14;
                        trailWidth = 4f;
                        backColor = Color.valueOf("4a2a6a");
                        frontColor = Color.valueOf("c9a8ff");
                    }},
                    resonantAlloy, new ArtilleryBulletType(3.6f, 160) {{
                        hitEffect = new MultiEffect(Fx.blastExplosion, Fx.bigShockwave);
                        despawnEffect = Fx.bigShockwave;
                        knockback = 1.2f;
                        lifetime = 125f;
                        width = 16f;
                        height = 20f;
                        collidesTiles = false;
                        splashDamageRadius = 52f;
                        splashDamage = 260f;
                        reloadMultiplier = 0.85f;
                        ammoMultiplier = 3f;
                        trailLength = 16;
                        trailWidth = 4.5f;
                        backColor = Color.valueOf("6a4a1a");
                        frontColor = Color.valueOf("ffe8a8");
                    }}
            );
        }};

        haloBeam = new LaserTurret("halo-beam") {{
            requirements(Category.turret, ItemStack.with(mirrorShard, 900, facetPlate, 350, prismCrystal, 300, resonantAlloy, 250, prismLens, 300, echoChip, 250));
            size = 4;
            scaledHealth = 220;
            range = 250f;
            reload = 100f;
            shootCone = 40f;
            rotateSpeed = 1.6f;
            recoil = 4f;
            shake = 2f;
            shootEffect = Fx.shootBigSmoke2;
            shootSound = Sounds.shootMeltdown;
            loopSound = Sounds.beamMeltdown;
            loopSoundVolume = 2f;
            firingMoveFract = 0.5f;
            shootDuration = 240f;
            liquidCapacity = 80f;
            coolant = consumeCoolant(0.4f);
            consumePower(18f);

            shootType = new ContinuousLaserBulletType(230f) {{
                length = 260f;
                hitEffect = Fx.hitLaserBlast;
                hitColor = Color.valueOf("ffb3e6");
                status = StatusEffects.melting;
                drawSize = 460f;
                timescaleDamage = true;
                incendChance = 0.4f;
                incendSpread = 5f;
                incendAmount = 1;
                ammoMultiplier = 1f;
            }};
        }};

        veilSegment = new PointDefenseTurret("veil-segment") {{
            requirements(Category.turret, ItemStack.with(prismCrystal, 160, voidOre, 90, veilFiber, 50, mirrorAlloy, 50));
            scaledHealth = 250;
            range = 210f;
            hasPower = true;
            consumePower(8f);
            size = 2;
            shootLength = 5f;
            bulletDamage = 35f;
            reload = 6f;
        }};

        prismRepair = new RepairTurret("prism-repair") {{
            requirements(Category.units, ItemStack.with(prismCrystal, 120, voidOre, 100, phaseGlass, 70));
            size = 2;
            length = 6f;
            repairSpeed = 2.8f;
            repairRadius = 140f;
            powerUse = 4f;
            beamWidth = 1.1f;
            pulseRadius = 6f;
            coolantUse = 0.14f;
            coolantMultiplier = 1.5f;
            acceptCoolant = true;
            ambientSoundVolume = 1.1f;
        }};

        /* ============================================================
         *  单位生产：镜潮孵化与升级链
         * ============================================================ */
        shardAssembler = new UnitFactory("shard-assembler") {{
            requirements(Category.units, ItemStack.with(mirrorShard, 80, facetPlate, 140, prismCrystal, 100));
            size = 3;
            consumePower(1.2f);
            plans = Seq.with(
                    new UnitPlan(MrUnits.mite, 60f * 12f, ItemStack.with(facetPlate, 10)),
                    new UnitPlan(MrUnits.crawler, 60f * 9f, ItemStack.with(mirrorShard, 8, prismCrystal, 8)),
                    new UnitPlan(MrUnits.dagger, 60f * 16f, ItemStack.with(prismCrystal, 12, facetPlate, 10))
            );
        }};

        skyForge = new UnitFactory("sky-forge") {{
            requirements(Category.units, ItemStack.with(mirrorShard, 100, facetPlate, 120, prismCrystal, 110, mirrorAlloy, 40));
            size = 3;
            consumePower(1.6f);
            plans = Seq.with(
                    new UnitPlan(MrUnits.vulture, 60f * 18f, ItemStack.with(prismCrystal, 15)),
                    new UnitPlan(MrUnits.wyvern, 60f * 35f, ItemStack.with(prismCrystal, 25, mirrorAlloy, 20)),
                    new UnitPlan(MrUnits.specter, 60f * 50f, ItemStack.with(prismCrystal, 40, mirrorAlloy, 35))
            );
        }};

        facetUpgrader = new Reconstructor("facet-upgrader") {{
            requirements(Category.units, ItemStack.with(mirrorShard, 250, facetPlate, 180, prismCrystal, 120));
            size = 3;
            hasPower = true;
            hasItems = true;
            consumePower(3f);
            consumeItems(ItemStack.with(prismCrystal, 30, prismLens, 20));
            constructTime = 60f * 12f;

            upgrades.addAll(
                    new UnitType[]{MrUnits.mite, MrUnits.nomad},
                    new UnitType[]{MrUnits.crawler, MrUnits.sabre},
                    new UnitType[]{MrUnits.dagger, MrUnits.mace}
            );
        }};

        prismUpgrader = new Reconstructor("prism-upgrader") {{
            requirements(Category.units, ItemStack.with(facetPlate, 700, prismCrystal, 500, mirrorAlloy, 400, lumenCrystal, 300));
            size = 5;
            hasPower = true;
            hasItems = true;
            consumePower(7f);
            consumeItems(ItemStack.with(prismCrystal, 120, mirrorAlloy, 80, phaseGlass, 40));
            constructTime = 60f * 30f;
            createSound = Sounds.unitCreateBig;

            upgrades.addAll(
                    new UnitType[]{MrUnits.sabre, MrUnits.buster},
                    new UnitType[]{MrUnits.mace, MrUnits.aegis},
                    new UnitType[]{MrUnits.nomad, MrUnits.mender}
            );
        }};

        abyssReconstructor = new Reconstructor("abyss-reconstructor") {{
            requirements(Category.units, ItemStack.with(facetPlate, 2200, prismCrystal, 1200, mirrorAlloy, 2000, voidOre, 900, phaseGlass, 500, veilFiber, 600, echoChip, 800, abyssCore, 300));
            size = 7;
            hasPower = true;
            hasItems = true;
            consumePower(14f);
            consumeItems(ItemStack.with(prismCrystal, 850, mirrorAlloy, 750, phaseGlass, 650));
            consumeLiquid(cryoPrism, 1f);
            constructTime = 60f * 90f;
            createSound = Sounds.unitCreateBig;

            upgrades.addAll(
                    new UnitType[]{MrUnits.buster, MrUnits.leviathan},
                    new UnitType[]{MrUnits.aegis, MrUnits.ark},
                    new UnitType[]{MrUnits.wyvern, MrUnits.skylift}
            );
        }};

        abyssFoundry = new UnitFactory("abyss-foundry") {{
            requirements(Category.units, ItemStack.with(voidOre, 800, prismCrystal, 700, mirrorAlloy, 600, veilFiber, 300, resonantAlloy, 200, echoChip, 400, abyssCore, 200));
            size = 5;
            consumePower(4f);
            plans = Seq.with(
                    new UnitPlan(MrUnits.aegis, 60f * 60f, ItemStack.with(prismCrystal, 80, mirrorAlloy, 60, voidOre, 40)),
                    new UnitPlan(MrUnits.leviathan, 60f * 120f, ItemStack.with(prismCrystal, 200, mirrorAlloy, 150, voidOre, 100, veilFiber, 40)),
                    new UnitPlan(MrUnits.ark, 60f * 150f, ItemStack.with(prismCrystal, 250, mirrorAlloy, 200, voidOre, 120, veilFiber, 60))
            );
        }};

        /* ============================================================
         *  核心发射器：建成后才允许把核心发射到其他区块
         * ============================================================ */
        CoreLauncher.loadContent();

    } // load()

    /* ================================================================
     *  同化器：把敌方单位转化为我方单位
     * ================================================================ */
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
                        MrFx.polyBurst.at(unit);
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

                // 常驻光效：在被转化的单位头顶显示倒计时光环
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

                        // 底层六边形脉冲光晕
                        MrFx.polyGlow(u.x, u.y, radius * 1.4f, Color.valueOf("aaddff").a(0.15f * pulse));

                        // 转化进度改为几何直线条，位于单位下方
                        MrFx.bar(u.x, u.y - radius - 8f, radius * 2.4f, p, Color.valueOf("aaddff").a(0.85f));
                    }
                    Draw.z(z);
                }
            }

            @Override
            public void drawSelect() {
                Assimilator self = (Assimilator) block;

                float pulse = 0.75f + Mathf.sin(Time.time * 0.05f) * 0.25f;

                // 仅点击时显示六边形范围框
                MrFx.polyGlow(x, y, self.range, Color.valueOf("aaddff").a(0.10f * pulse));
                MrFx.hexRing(x, y, self.range, Color.valueOf("bbddff").a(0.42f * pulse));
                MrFx.hexRing(x, y, self.range * 0.55f, Color.valueOf("ffffff").a(0.30f * pulse));
            }
        }
    }

    /* ================================================================
     *  召唤炮弹药
     * ================================================================ */
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
            MrFx.polyBurst.at(x, y);
            MrFx.summonRift.at(x, y);
            Fx.hitBulletColor.at(x, y, 0, Color.valueOf("ff9966"));
        }
    }

    /* ================================================================
     *  召唤炮：累计伤害后召唤一架 falcon
     * ================================================================ */
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
                        MrFx.summonRift.at(x, y);
                        MrFx.polyBurst.at(x, y);
                        Unit unit = falcon.create(team);
                        unit.set(x, y);
                        unit.add();
                        MrFx.mirrorWarpIn.at(x, y);
                        MrFx.unitPrint.at(x, y, 0f, falcon);
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

                // 常驻光效：召唤爆发时的六边形光环
                if (glowTime > 0f) {
                    float glow = Mathf.clamp(glowTime / 45f);

                    MrFx.polyGlow(x, y, 50f * glow, Color.valueOf("ffcc88").a(0.35f * glow));
                    MrFx.hexRing(x, y, 35f * glow, Color.valueOf("ffffff").a(0.5f * glow));
                }
            }

            @Override
            public void drawSelect() {
                // 仅点击时显示的射程虚线与进度条
                float progress = Mathf.clamp(damageAccumulated / summonThreshold);
                float pulse = 0.8f + Mathf.sin(Time.time * 0.07f) * 0.2f;

                MrFx.polyGlow(x, y, range, Color.valueOf("ff9966").a(0.07f * pulse));
                MrFx.hexRing(x, y, range, Color.valueOf("ffaa66").a(0.42f * pulse));
                MrFx.hexRing(x, y, range * 0.55f, Color.valueOf("ffcc88").a(0.25f * pulse));

                if (progress > 0f) {
                    MrFx.bar(x, y - range * 0.5f - 8f, range * 0.8f, progress, Color.valueOf("ffcc88").a(0.85f));
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

    /* ================================================================
     *  特殊核心：自带炮塔，并且只维持一个卫队单位
     *
     *  - guardianName：卫队单位的内容名（定义在 MrUnits，核心与单位分离）
     *  - 卫队死亡后 buildCounter 归零，重新走完整生产时间
     *  - 炮塔自动索敌、平滑转向、有射界时开火
     * ================================================================ */
    public static class HiveCore extends CoreBlock {
        public UnitType guardianType;
        public String guardianName;
        public float guardBuildTime = 60f * 20f;
        public float turretRange = 200f;
        public float turretReload = 30f;
        public float turretLength = 26f;
        public float rotateSpeed = 3.5f;
        public BulletType bulletType;
        public Color turretColor = Color.valueOf("9fe8ff");

        public HiveCore(String name) {
            super(name);
            update = true;
        }

        public class HiveCoreBuild extends CoreBuild {
            public Unit guardian;
            public float buildCounter = 0f;
            public float rotation = 90f;
            public float reloadCounter = 0f;

            /** 延迟解析单位名，避免 MrUnits / MrBlocks 的加载顺序问题。
             *  模组内容会被加上“模组名-”前缀，所以先按原名查，再按后缀匹配。 */
            UnitType resolveGuardian() {
                if (guardianType != null) return guardianType;
                if (guardianName == null) return null;

                UnitType direct = content.unit(guardianName);
                if (direct != null) return guardianType = direct;

                for (UnitType u : content.units()) {
                    if (u.name.equals(guardianName) || u.name.endsWith("-" + guardianName)) {
                        return guardianType = u;
                    }
                }
                return null;
            }

            @Override
            public void updateTile() {
                super.updateTile();

                // 卫队死亡 → 清空引用并从头计时
                if (guardian != null && guardian.dead) {
                    guardian = null;
                    buildCounter = 0f;
                }

                UnitType type = resolveGuardian();

                // 读档后 guardian 引用会丢失，先认领附近同类型卫队，避免重复生产
                if (guardian == null && type != null) {
                    Unit existing = Units.closest(team, x, y, Math.max(turretRange, 160f), u -> !u.dead && u.type == type);
                    if (existing != null) {
                        guardian = existing;
                        buildCounter = 0f;
                    }
                }

                if (guardian == null && type != null) {
                    buildCounter += Time.delta;

                    if (buildCounter >= guardBuildTime && !net.client()) {
                        buildCounter = 0f;

                        Unit spawned = type.create(team);
                        if (spawned != null) {
                            // 从核心外沿的炮口方向放出，避免生成在实心核心内部
                            float spawnDist = block.size * 4f + 10f;
                            spawned.set(
                                    x + Angles.trnsx(rotation, spawnDist) + Mathf.range(4f),
                                    y + Angles.trnsy(rotation, spawnDist) + Mathf.range(4f)
                            );
                            spawned.rotation = rotation;
                            spawned.add();

                            guardian = spawned;

                            MrFx.summonRift.at(spawned.x, spawned.y, rotation);
                            MrFx.unitPrint.at(spawned.x, spawned.y, rotation, type);
                        }
                    }
                } else if (guardian != null) {
                    // 已有唯一卫队，保持计时为零，死亡后重新计时
                    buildCounter = 0f;
                }

                // ===== 炮塔逻辑 =====
                reloadCounter += Time.delta;

                Unit target = Units.closestEnemy(team, x, y, turretRange, u -> !u.dead);
                if (target != null) {
                    float targetAngle = angleTo(target.x, target.y);
                    rotation = Angles.moveToward(rotation, targetAngle, rotateSpeed * Time.delta);

                    if (reloadCounter >= turretReload && Angles.angleDist(rotation, targetAngle) <= 12f) {
                        reloadCounter = 0f;

                        float bx = x + Angles.trnsx(rotation, turretLength);
                        float by = y + Angles.trnsy(rotation, turretLength);

                        if (!net.client() && bulletType != null) {
                            bulletType.create(this, team, bx, by, rotation);
                        }

                        Fx.shootBig.at(bx, by, rotation);
                        Sounds.shootDuo.at(bx, by);
                    }
                } else {
                    // 无目标时缓慢回正
                    rotation = Angles.moveToward(rotation, 90f, rotateSpeed * 0.5f * Time.delta);
                }
            }

            @Override
            public void draw() {
                super.draw();

                float z = Draw.z();
                Draw.z(Layer.turret);

                float pulse = 0.75f + Mathf.absin(Time.time, 9f, 0.25f);

                // 核心底座光晕（六边形）
                MrFx.polyGlow(x, y, block.size * 3.5f, turretColor.cpy().a(0.20f * pulse));

                // 炮管
                float bx = x + Angles.trnsx(rotation, turretLength);
                float by = y + Angles.trnsy(rotation, turretLength);

                Lines.stroke(6f, turretColor.cpy().a(0.30f * pulse));
                Lines.line(x, y, bx, by);

                Lines.stroke(2.6f, turretColor);
                Lines.line(x, y, bx, by);

                Lines.stroke(2f, Color.white.cpy().a(0.75f * pulse));
                Lines.square(bx, by, 2.4f);

                Draw.reset();
                Draw.z(z);
            }

            @Override
            public void drawSelect() {
                super.drawSelect();

                MrFx.hexRing(x, y, turretRange, turretColor.cpy().a(0.45f));
                MrFx.hexRing(x, y, turretRange * 0.62f, turretColor.cpy().a(0.20f));

                // 卫队生产进度条（几何直线）
                if (guardian == null && guardBuildTime > 0f) {
                    float p = Mathf.clamp(buildCounter / guardBuildTime);
                    MrFx.bar(x, y - block.size * 4f - 8f, block.size * 8f, p, turretColor.cpy().a(0.9f));
                }
            }
        }
    }

    /* ============================================================
     *  多配方工厂：一套方块内含多套配方，点击方块在配方间切换
     * ============================================================ */
    public static class Fabricator extends Block {
        public Recipe[] recipes = {};
        public Effect craftEffect = MrFx.craftFlash;

        public Fabricator(String name){
            super(name);
            update = true;
            solid = true;
            hasItems = true;
            configurable = true;
            saveConfig = true;
            itemCapacity = 40;

            config(Integer.class, (FabricatorBuild b, Integer i) -> {
                if(i != null) b.recipeIndex = i;
            });
        }

        public static class Recipe {
            public final ItemStack[] input;
            public final ItemStack output;
            public final float craftTime;

            public Recipe(ItemStack[] input, ItemStack output, float craftTime){
                this.input = input;
                this.output = output;
                this.craftTime = craftTime;
            }
        }

        @Override
        public void setStats(){
            super.setStats();

            stats.add(Stat.output, (StatValue) table -> {
                table.defaults().left().padTop(4f).growX();
                for(Recipe r : recipes){
                    Table row = new Table();

                    for(ItemStack s : r.input){
                        row.image(s.item.fullIcon).size(22f).padRight(2f);
                        row.add(s.amount + "x").left().color(Color.lightGray).padRight(6f);
                    }

                    row.add("->").left().color(Color.gray).padLeft(2f).padRight(6f);

                    row.image(r.output.item.fullIcon).size(22f).padRight(2f);
                    row.add(r.output.amount + "x " + r.output.item.localizedName).left();
                    row.add("(" + (int)r.craftTime + "s)").left().color(Color.gray).padLeft(6f);

                    table.add(row).growX().row();
                }
            });
        }

        public class FabricatorBuild extends Building {
            public int recipeIndex = -1;
            public float progress = 0f;

            public Recipe recipe(){
                return recipeIndex >= 0 && recipeIndex < recipes.length ? recipes[recipeIndex] : null;
            }

            @Override
            public void updateTile(){
                Recipe r = recipe();
                if(r == null) return;

                boolean hasInputs = true;
                for(ItemStack s : r.input){
                    if(items.get(s.item) < s.amount){
                        hasInputs = false;
                        break;
                    }
                }

                boolean canCraft = hasInputs && items.get(r.output.item) < itemCapacity;

                if(canCraft){
                    progress += edelta() / r.craftTime;
                }else{
                    progress = Math.max(0f, progress - Time.delta / r.craftTime);
                }

                if(progress >= 1f){
                    for(ItemStack s : r.input){
                        items.remove(s.item, s.amount);
                    }
                    progress = 0f;

                    for(int i = 0; i < r.output.amount; i++){
                        offload(r.output.item);
                    }

                    if(wasVisible){
                        craftEffect.at(x, y);
                    }
                }
            }

            @Override
            public boolean acceptItem(Building source, Item item){
                return items.get(item) < itemCapacity;
            }

            @Override
            public void buildConfiguration(Table table){
                ButtonGroup<TextButton> group = new ButtonGroup<>();
                group.setMinCheckCount(0);

                for(int i = 0; i < recipes.length; i++){
                    Recipe r = recipes[i];
                    int idx = i;

                    TextButton b = new TextButton(r.output.item.localizedName);
                    b.changed(() -> configure(b.isChecked() ? idx : -1));
                    group.add(b);
                    table.add(b).size(190f, 46f).pad(4f).left().row();
                }
            }

            @Override
            public Integer config(){
                return recipeIndex;
            }

            @Override
            public void write(Writes write){
                super.write(write);
                write.i(recipeIndex);
            }

            @Override
            public void read(Reads read, byte revision){
                super.read(read, revision);
                recipeIndex = read.i();
            }
        }
    }

    /* ============================================================
     *  地形改造器：消耗物品，把范围内会扣血的菌丝地板/墙体换成安全方块
     * ============================================================ */
    public static class Terraformer extends Block {
        public Item useItem;
        public int itemsPerUse = 1;
        public float useTime = 120f;
        public int range = 3;
        public Block replaceFloor;
        public Seq<Block> targetFloors = new Seq<>();
        public Seq<Block> targetWalls = new Seq<>();

        public Terraformer(String name){
            super(name);
            update = true;
            solid = true;
            hasItems = true;
            itemCapacity = 10;
        }

        public class TerraformerBuild extends Building {
            public float timer = 0f;

            @Override
            public boolean acceptItem(Building source, Item item){
                return item == useItem && items.get(item) < itemCapacity;
            }

            @Override
            public void updateTile(){
                if(useItem == null || items.get(useItem) < itemsPerUse) return;

                timer += edelta();
                if(timer < useTime) return;
                timer = 0f;

                boolean did = false;
                for(int dx = -range; dx <= range; dx++){
                    for(int dy = -range; dy <= range; dy++){
                        Tile t = world.tile(tileX() + dx, tileY() + dy);
                        if(t == null || t.build != null) continue;

                        if(targetWalls.contains(t.block()) && t.block() != Blocks.air){
                            t.removeNet();
                            did = true;
                        }else if(targetFloors.contains(t.floor())){
                            t.setFloorNet(replaceFloor, Blocks.air);
                            did = true;
                        }
                    }
                }

                if(did){
                    items.remove(useItem, itemsPerUse);
                    MrFx.terraform.at(x, y);
                }
            }

            @Override
            public void drawSelect(){
                super.drawSelect();
                MrFx.hexRing(x, y, range * 8f, Color.valueOf("9fc4e8"));

                float p = useTime > 0f ? Mathf.clamp(timer / useTime) : 0f;
                MrFx.bar(x, y - block.size * 4f - 8f, block.size * 8f, p, Color.valueOf("9fc4e8"));
            }
        }
    }
}
