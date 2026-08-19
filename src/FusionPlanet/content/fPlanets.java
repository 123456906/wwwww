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


        ringWorld = new Planet("ring-world", fusionPlanet, 0.05f, 1) {{
            generator = new RingPlanetGenerator();
            meshLoader = () -> {
                // 环形噪声网格：直接生成一个扁平的环状网格
                // 参数：行星, 种子, 分段数, 半径, 八度, 持久性, 频率, 缩放
                // 使用极低的半径和扁平参数
                Color metal = Color.valueOf("555555");
                Color dark = Color.valueOf("333333");

                // 方法：创建一个扁平环，用两个叠加的网格
                // 主环体
                NoiseMesh ringMesh = new NoiseMesh(
                        ringWorld,           // 行星
                        42,                  // 种子
                        1,                   // 分段数（保持低值，让环看起来平滑）
                        0.85f,               // 半径（环的大小）
                        2,                   // 八度
                        0.5f,                // 持久性
                        0.4f,                // 频率
                        20f,                 // 缩放
                        metal,               // 主色
                        dark,                // 第二色
                        2, 0.5f, 0.3f, 0.3f
                );

                Mat3D mat = new Mat3D();
                mat.setToTranslation(new Vec3(0, 0, 0));
                Mat3D flatten = new Mat3D();
                flatten.scale(1f, 0.015f, 1f);
                mat.mul(flatten);
                Mat3D tilt = new Mat3D();
                tilt.rotate(1, 0, 0, 30);
                mat.mul(tilt);

                return new MatMesh(ringMesh, mat);
            };


            localizedName = "Ring World";
            visible = false;
            accessible = true;
            alwaysUnlocked = false;
            bloom = false;
            defaultEnv = Env.terrestrial;
            allowLaunchToNumbered = true;
            startSector = 0;
            defaultCore = Blocks.coreShard;

            orbitRadius = 0f;
            orbitTime = 60 * 60;
            rotateTime = 30 * 60;
            updateLighting = false;

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