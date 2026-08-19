package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Team;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.world.TileGen;
import mindustry.world.meta.Env;

public class fPlanets {
    public static Planet fusionPlanet;
    public static Planet ringWorld;
    public static Planet blackHole;

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

        ringWorld = new Planet("ring-world", fusionPlanet, 1.2f, 2) {{
            generator = new RingPlanetGenerator();
            meshLoader = () -> new HexMesh(this, 6);
            localizedName = "Ring World";
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            bloom = false;
            defaultEnv = Env.terrestrial;
            atmosphereColor = Color.valueOf("886644");
            atmosphereRadIn = 0.01f;
            atmosphereRadOut = 0.2f;
            allowLaunchToNumbered = true;
            startSector = 0;
            defaultCore = Blocks.coreShard;
            // 轨道：紧贴母星，视觉上呈现环状
            orbitRadius = 0.001f;
            orbitTime = 60 * 60;
            rotateTime = 30 * 60;
            ruleSetter = r -> {
                r.waveTeam = Team.crux;
                r.waves = true;
                r.env = Env.terrestrial;
                r.winWave = 10;
                r.placeRangeCheck = true;
            };
        }};
    }
}