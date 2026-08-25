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
    public static SectorPreset undevelopedZone;
    private static final float ringGlowOffset = 0.008f;

    public static void load() {
        fusionPlanet = new Planet("fusion-planet", Planets.sun, 1f, 2);
        fusionPlanet.generator = new FusionPlanetGenerator();
        fusionPlanet.localizedName = "Fusion World";
        fusionPlanet.visible = true;
        fusionPlanet.accessible = true;
        fusionPlanet.alwaysUnlocked = true;
        fusionPlanet.bloom = false;
        fusionPlanet.defaultEnv = Env.terrestrial;
        fusionPlanet.atmosphereColor = Color.valueOf("7a8cbf");
        fusionPlanet.atmosphereRadIn = 0.02f;
        fusionPlanet.atmosphereRadOut = 0.28f;
        fusionPlanet.allowLaunchToNumbered = true;
        fusionPlanet.startSector = 32;
        fusionPlanet.defaultCore = Blocks.coreShard;

        fusionPlanet.meshLoader = () -> new HexMesh(fusionPlanet, 5);

        Color ringColor1 = Color.valueOf("88aacc");
        Color ringColor2 = Color.valueOf("667799");
        Color glowColor = Color.valueOf("aaccee");

        fusionPlanet.cloudMeshLoader = () -> new MultiMesh(
                new DysonRingMesh(fusionPlanet, 1.45f, 0.12f, 512, ringColor1, ringColor2),
                new DysonRingMesh(fusionPlanet, 1.65f, 0.10f, 768, ringColor1, ringColor2),
                new DysonRingMesh(fusionPlanet, 1.85f, 0.08f, 1024, ringColor1, ringColor2),
                new DysonRingMesh(fusionPlanet, 1.45f + ringGlowOffset, 0.06f, 512, glowColor, glowColor, true),
                new DysonRingMesh(fusionPlanet, 1.65f + ringGlowOffset, 0.05f, 768, glowColor, glowColor, true),
                new DysonRingMesh(fusionPlanet, 1.85f + ringGlowOffset, 0.04f, 1024, glowColor, glowColor, true)
        );

        fusionPlanet.ruleSetter = r -> {
            r.waveTeam = Team.crux;
            r.waves = true;
            r.env = Env.terrestrial;
            r.winWave = 10;
            r.placeRangeCheck = true;
        };

        undevelopedZone = new SectorPreset("undeveloped-zone", fusionPlanet, 5);
        undevelopedZone.localizedName = "未开发区";
        undevelopedZone.description = "一片尚未被开发的区域";
        undevelopedZone.difficulty = 1;
        undevelopedZone.captureWave = 20;
        undevelopedZone.alwaysUnlocked = true;
    }
}