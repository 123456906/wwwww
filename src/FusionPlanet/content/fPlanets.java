package FusionPlanet.content;

import FusionPlanet.ring.DysonRingMesh;
import arc.graphics.Color;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Team;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;
import mindustry.world.meta.Env;

import static FusionPlanet.ring.RingWorldPlanet.*;
import static FusionPlanet.ring.RingWorldMesh.*;
import static FusionPlanet.ring.RingWorldGrid.*;
import static FusionPlanet.ring.DysonRingMesh.*;

public class fPlanets {
    public static Planet fusionPlanet;
    public static Planet superPlanet;
    public static SectorPreset cliffboundReaches;
    public static SectorPreset undevelopedZone;
    public static SectorPreset abandonedFerry;
    public static SectorPreset myceliumBastion;
    private static final float ringGlowOffset = 0.008f;

    public static void load() {
        fusionPlanet = new Planet("fusion-planet", Planets.sun, 1f, 2);
        fusionPlanet.generator = new FusionPlanetGenerator();
        fusionPlanet.localizedName = "Fusion World";
        fusionPlanet.visible = true;
        fusionPlanet.accessible = true;
        fusionPlanet.alwaysUnlocked = false;
        fusionPlanet.bloom = false;
        fusionPlanet.defaultEnv = Env.terrestrial;
        fusionPlanet.atmosphereColor = Color.valueOf("7a8cbf");
        fusionPlanet.atmosphereRadIn = 0.02f;
        fusionPlanet.atmosphereRadOut = 0.28f;
        fusionPlanet.allowLaunchToNumbered = true;
        fusionPlanet.startSector = 5;
        fusionPlanet.defaultCore = Blocks.coreShard;

        fusionPlanet.meshLoader = () -> new HexMesh(fusionPlanet, 6   );

        Color ringColor1 = Color.valueOf("4C4C4C");
        Color ringColor2 = Color.valueOf("59B368");
        Color glowColor = Color.valueOf("FFFFFF");

        fusionPlanet.cloudMeshLoader = () -> new MultiMesh(
                new DysonRingMesh(fusionPlanet, 1.65f, 0.30f, 768, ringColor1, ringColor2),
                new DysonRingMesh(fusionPlanet, 1.65f + ringGlowOffset, 0.12f, 768, glowColor, glowColor, true)
        );

        fusionPlanet.ruleSetter = r -> {
            r.waveTeam = Team.crux;
            r.waves = true;
            r.env = Env.terrestrial;
            r.placeRangeCheck = true;
        };

        undevelopedZone = new SectorPreset("undeveloped-zone", fusionPlanet, 5);
        undevelopedZone.localizedName = "未开发区";
        undevelopedZone.description = "一片尚未被开发的区域";
        undevelopedZone.difficulty = 1;
        undevelopedZone.captureWave = 20;
        undevelopedZone.alwaysUnlocked = true;

        abandonedFerry = new SectorPreset("abandoned-ferry", fusionPlanet, 1);
        abandonedFerry.localizedName = "废弃渡口";
        abandonedFerry.description = "据说这里藏着先驱者曾经放在这里的武器（假的）";
        abandonedFerry.difficulty = 3;
        abandonedFerry.captureWave = 20;
        abandonedFerry.alwaysUnlocked = false;

        myceliumBastion = new SectorPreset("mycelium-bastion", fusionPlanet, 1);
        myceliumBastion.localizedName = "菌丝壁垒";
        myceliumBastion.description = "据说这里藏着先驱者曾经放在这里的武器";
        myceliumBastion.difficulty = 6;
        myceliumBastion.captureWave = 30;
        myceliumBastion.alwaysUnlocked = false;

        superPlanet= new Planet("super-planet", Planets.sun, 1.2f, 2) {{
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
                r.winWave = 10;
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