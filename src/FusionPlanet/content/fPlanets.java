package FusionPlanet.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.struct.Seq;
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
        fusionPlanet.startSector = 32;
        fusionPlanet.defaultCore = Blocks.coreShard;

        fusionPlanet.meshLoader = () -> new HexMesh(fusionPlanet, 5);

        fusionPlanet.cloudMeshLoader = () -> {
            Seq<GenericMesh> meshes = new Seq<>();

            int segments = 36;
            float ringWidth = 0.3f;
            float ringHeight = 0.03f;
            float[] radii = {1.15f, 1.35f, 1.55f};
            Color[] colors = {
                    Color.valueOf("8899aa"),
                    Color.valueOf("99aabb"),
                    Color.valueOf("aabbcc")
            };
            Color tint = Color.valueOf("556677");

            for (int ringIdx = 0; ringIdx < radii.length; ringIdx++) {
                float radius = radii[ringIdx];
                Color c = colors[ringIdx];
                for (int i = 0; i < segments; i++) {
                    float angle = (i / (float)segments) * 360f;
                    NoiseMesh segment = new NoiseMesh(
                            fusionPlanet, i + ringIdx * 1000 + 200, 1,
                            ringWidth, 2, 0.5f, 0.4f, 12f,
                            c, tint, 2, 0.5f, 0.3f, 0.3f
                    );
                    Mat3D mat = new Mat3D();
                    mat.setToTranslation(new Vec3(0, 0, radius));
                    mat.rotate(0, 1, 0, angle);
                    Mat3D tilt = new Mat3D();
                    tilt.rotate(1, 0, 0, 25);
                    mat.mul(tilt);
                    Mat3D flatten = new Mat3D();
                    flatten.scale(1f, ringHeight / ringWidth, 1f);
                    mat.mul(flatten);
                    meshes.add(new MatMesh(segment, mat));
                }
            }

            return new MultiMesh(meshes.toArray(GenericMesh.class));
        };

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