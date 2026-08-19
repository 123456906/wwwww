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

            meshLoader = () -> {
                int segments = 24;
                float ringRadius = 0.9f;
                float ringWidth = 0.25f;
                float ringHeight = 0.04f;

                Color metalColor = Color.valueOf("555555");
                Color tintColor = Color.valueOf("333333");

                GenericMesh[] meshes = new GenericMesh[segments];

                for (int i = 0; i < segments; i++) {
                    float angle = (i / (float) segments) * 360f;

                    NoiseMesh segment = new NoiseMesh(
                            ringWorld, i + 100, 1,
                            ringWidth,
                            2, 0.5f, 0.4f, 12f,
                            metalColor, tintColor,
                            2, 0.5f, 0.4f, 0.3f
                    );
                    Mat3D mat = new Mat3D();
                    mat.setToTranslation(new Vec3(0, 0, ringRadius));
                    mat.rotate(0, 1, 0, angle);
                    Mat3D tilt = new Mat3D();
                    tilt.rotate(1, 0, 0, 30);
                    mat.mul(tilt);
                    Mat3D flatten = new Mat3D();
                    flatten.scale(1f, ringHeight / ringWidth, 1f);
                    mat.mul(flatten);

                    meshes[i] = new MatMesh(segment, mat);
                }

                return new MultiMesh(meshes);
            };

            localizedName = "Ring World";
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            bloom = false;
            defaultEnv = Env.terrestrial;
            allowLaunchToNumbered = true;
            startSector = 0;
            defaultCore = Blocks.coreShard;
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