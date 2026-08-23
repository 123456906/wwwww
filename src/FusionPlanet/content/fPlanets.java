package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Team;
import mindustry.graphics.g3d.*;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;
import mindustry.world.TileGen;
import mindustry.world.meta.Env;

public class fPlanets {
    public static Planet fusionPlanet;
    public static SectorPreset undevelopedZone;

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
        fusionPlanet.startSector = 1;
        fusionPlanet.defaultCore = Blocks.coreShard;

        Color cloud1 = Color.valueOf("aabbdd");
        cloud1.a = 0.4f;
        Color cloud2 = Color.valueOf("8899bb");
        cloud2.a = 0.3f;

        fusionPlanet.cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(fusionPlanet, 6, 0.15f, 0.12f, 5, cloud1, 2, 0.4f, 0.9f, 0.38f),
                new HexSkyMesh(fusionPlanet, 4, 0.3f, 0.10f, 5, cloud2, 1, 0.3f, 1.0f, 0.4f)
        );
        fusionPlanet.meshLoader = () -> new HexMesh(fusionPlanet, 5);
        fusionPlanet.ruleSetter = r -> {
            r.waveTeam = Team.crux;
            r.waves = true;
            r.env = Env.terrestrial;
            r.winWave = 10;
            r.placeRangeCheck = true;
        };

        undevelopedZone = new SectorPreset("undeveloped-zone", fusionPlanet, 1);
        undevelopedZone.localizedName = "未开发区";
        undevelopedZone.description = "一片尚未被开发的区域";
        undevelopedZone.difficulty = 1;
        undevelopedZone.captureWave = 10;
        undevelopedZone.alwaysUnlocked = true;

        undevelopedZone = new SectorPreset("abandoned-ferry", fusionPlanet, 1);
        undevelopedZone.localizedName = "废弃渡口";
        undevelopedZone.description = "先驱留下的遗迹";
        undevelopedZone.difficulty = 3;
        undevelopedZone.captureWave = 40;
        undevelopedZone.alwaysUnlocked = true;
    }
}