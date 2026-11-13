package MirrorWorld.content;

import arc.graphics.Color;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.content.Weathers;
import mindustry.game.Team;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;
import mindustry.world.meta.Env;
//import mindustry.game.Weathers;
import mindustry.type.Weather;

public class MirrorSystemPlanets {
    public static Planet antichthon; // 将 fusionPlanet 改名或替换成 antichthon
    public static Planet superPlanet;
    public static SectorPreset cliffboundReaches;
    public static SectorPreset undevelopedZone;
    public static SectorPreset abandonedFerry;
    public static SectorPreset myceliumBastion;


    public static void load() {
        // 确保 AntichthonBlocks 已加载
        if (FusionPlanet.content.AntichthonBlocks.liquidCO2 == null) {
            FusionPlanet.content.AntichthonBlocks.load();
        }
        FusionPlanet.content.AntichthonGenerator.loadBlueprints();

        // ========== 对地星 (Antichthon) ==========
        // 注意内部名改成了 "mw-antichthon"
        antichthon = new Planet("antichthon", Planets.sun, 1f, 2);
        antichthon.generator = new FusionPlanet.content.AntichthonGenerator();
        antichthon.localizedName = "Antichthon";
        antichthon.visible = true;
        antichthon.accessible = true;
        antichthon.alwaysUnlocked = false;
        antichthon.bloom = false;
        antichthon.defaultEnv = Env.terrestrial;
        
        // 大气与光照参考 JS
        antichthon.atmosphereColor = Color.valueOf("d89070");
        antichthon.atmosphereRadIn = 0.02f;
        antichthon.atmosphereRadOut = 0.28f;
        antichthon.allowLaunchToNumbered = true;
        antichthon.startSector = 5;
        antichthon.defaultCore = Blocks.coreShard;

        antichthon.meshLoader = () -> new HexMesh(antichthon, 6);

        // 云层参考 JS
        Color baseColor = Color.valueOf("d89070"); 
        Color c1 = baseColor.cpy().mul(0.9f); 
        c1.a = 0.75f;
        Color c2 = Color.white.cpy().lerp(baseColor, 0.55f); 
        c2.a = 0.75f;

        antichthon.cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(antichthon, 11, 0.15f, 0.13f, 6, c1, 2, 0.45f, 0.9f, 0.38f),
                new HexSkyMesh(antichthon, 1, 0.6f, 0.16f, 6, c2, 2, 0.45f, 1.0f, 0.41f)
        );

        // 规则参考 JS
        antichthon.ruleSetter = r -> {
            r.waveTeam = Team.crux;
            r.waves = true;
            r.fire = false;
            r.ambientLight.set(Color.valueOf("FF856315"));
            r.weather.add(new Weather.WeatherEntry(Weathers.sandstorm));
            r.env = Env.terrestrial;
            r.placeRangeCheck = true;
        };

        // ========== 区块预设 ==========
        undevelopedZone = new SectorPreset("undeveloped-zone", antichthon, 5);
        undevelopedZone.localizedName = "未开发区";
        undevelopedZone.description = "一片尚未被开发的区域";
        undevelopedZone.difficulty = 1;
        undevelopedZone.captureWave = 20;
        undevelopedZone.alwaysUnlocked = true;

        abandonedFerry = new SectorPreset("abandoned-ferry", antichthon, 1);
        abandonedFerry.localizedName = "废弃渡口";
        abandonedFerry.description = "据说这里藏着先驱者曾经放在这里的武器（假的）";
        abandonedFerry.difficulty = 3;
        abandonedFerry.captureWave = 20;
        abandonedFerry.alwaysUnlocked = false;

        myceliumBastion = new SectorPreset("mycelium-bastion", antichthon, 1);
        myceliumBastion.localizedName = "菌丝壁垒";
        myceliumBastion.description = "据说这里藏着先驱者曾经放在这里的武器";
        myceliumBastion.difficulty = 6;
        myceliumBastion.captureWave = 30;
        myceliumBastion.alwaysUnlocked = false;

        // ========== 超级星 (保持不变) ==========
        superPlanet = new Planet("super-planet", Planets.sun, 1.2f, 2) {{
            generator = new SuperPlanetGenerator();
            meshLoader = () -> new HexMesh(this, 6);
            localizedName = "宇宙无敌超级大霹力星";
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            bloom = false;
            defaultEnv = Env.terrestrial;
            atmosphereColor = Color.valueOf("6688cc");
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
            allowLaunchToNumbered = true;
            startSector = 0;
            defaultCore = Blocks.coreShard;
            ruleSetter = r -> {
                r.waveTeam = Team.crux;
                r.waves = true;
                r.env = Env.terrestrial;
                r.placeRangeCheck = true;
            };
        }};

        cliffboundReaches = new SectorPreset("cliffbound-reaches", superPlanet, 73);
        cliffboundReaches.localizedName = "\uF657\uF657cliffboundReaches\uF7A9\uF657\uF657";
        cliffboundReaches.description = "检测到此地正源源不断发射未知电信号。";
        cliffboundReaches.difficulty = 3;
        cliffboundReaches.captureWave = 30;
        cliffboundReaches.alwaysUnlocked = true;
    }
}