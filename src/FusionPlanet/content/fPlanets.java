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
import FusionPlanet.content.FusionPlanetGenerator;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.world.TileGen;
import mindustry.world.meta.Env;

public class fPlanets {
    public static Planet fusionPlanet;
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

        blackHole = new Planet("black-hole", Planets.sun, 0.8f, 1) {{
            generator = new PlanetGenerator() {
                @Override
                public float getHeight(Vec3 pos) {
                    return 0f;
                }

                @Override
                public void getColor(Vec3 pos, Color out) {
                    float dist = pos.len();
                    float mix = Mathf.clamp((dist - 0.8f) / 0.2f);
                    out.set(Color.valueOf("000000")).lerp(Color.valueOf("440000"), mix);
                    out.a = 1f;
                }

                @Override
                public void genTile(Vec3 pos, TileGen tile) {
                    tile.floor = Blocks.air;
                    tile.block = Blocks.air;
                }

                @Override
                public float getSizeScl() {
                    return 1000f;
                }
            };

            meshLoader = () -> new HexMesh(this, 4);

            Color disk1 = Color.valueOf("ff6633");
            disk1.a = 0.6f;
            Color disk2 = Color.valueOf("ffaa44");
            disk2.a = 0.4f;
            cloudMeshLoader = () -> new MultiMesh(
                    new HexSkyMesh(this, 3, 0.2f, 0.25f, 6, disk1, 2, 0.3f, 0.9f, 0.6f),
                    new HexSkyMesh(this, 2, -0.1f, 0.30f, 6, disk2, 1, 0.2f, 1.1f, 0.7f)
            );

            atmosphereColor = Color.valueOf("ff8844");
            atmosphereRadIn = 0.01f;
            atmosphereRadOut = 0.4f;
            lightColor = Color.valueOf("ffaa55");

            accessible = false;
            visible = true;
            alwaysUnlocked = true;
            bloom = true;
            defaultEnv = Env.space;
            localizedName = "黑洞";
            description = "一个吞噬光线的天体，周围环绕着炽热的吸积盘。";
            startSector = -1;
            orbitRadius = 45f;
            orbitTime = 180 * 60;
            rotateTime = 30 * 60;
            iconColor = Color.valueOf("ff8844");
            updateLighting = false;
        }};
    }
}