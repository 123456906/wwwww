package MirrorWorld.content;

import arc.graphics.Color;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.content.Weathers;
import mindustry.game.Team;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import mindustry.world.meta.Env;
import mindustry.type.Weather;

public class MirrorSystemPlanets {
    public static Planet antichthon;

    private static boolean loaded;

    public static void load() {
        if(loaded) return;
        loaded = true;

        // 1. 确保 AntichthonBlocks 和生成器的蓝图已加载
        AntichthonBlocks.load();
        MirrorWorld.content.AntichthonGenerator.loadBlueprints();

        // ========== 对地星 (Antichthon) ==========
        antichthon = new Planet("antichthon", Planets.sun, 1f, 2);
        antichthon.generator = new MirrorWorld.content.AntichthonGenerator(); // 当前包直接调用即可
        antichthon.localizedName = "Antichthon";
        antichthon.visible = true;
        antichthon.accessible = true;
        antichthon.alwaysUnlocked = true;
        antichthon.bloom = false;
        antichthon.defaultEnv = Env.terrestrial;

        // 大气与光照
        antichthon.atmosphereColor = Color.valueOf("d89070");
        antichthon.atmosphereRadIn = 0.02f;
        antichthon.atmosphereRadOut = 0.28f;
        antichthon.allowLaunchToNumbered = true;
        antichthon.startSector = 5;
        antichthon.defaultCore = Blocks.coreShard;

        // 行星本体 + 人造星环（赤道环带，MultiMesh 一起渲染）
        antichthon.meshLoader = () -> new MultiMesh(
                new HexMesh(antichthon, 6),
                new RingMesh(antichthon)
        );

        // 云层
        Color baseColor = Color.valueOf("d89070");
        Color c1 = baseColor.cpy().mul(0.9f);
        c1.a = 0.75f;
        Color c2 = Color.white.cpy().lerp(baseColor, 0.55f);
        c2.a = 0.75f;

        antichthon.cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(antichthon, 11, 0.15f, 0.13f, 6, c1, 2, 0.45f, 0.9f, 0.38f),
                new HexSkyMesh(antichthon, 1, 0.6f, 0.16f, 6, c2, 2, 0.45f, 1.0f, 0.41f)
        );

        // 规则
        antichthon.ruleSetter = r -> {
            r.waveTeam = Team.crux;
            r.waves = true;
            r.fire = false;
            r.ambientLight.set(Color.valueOf("FF856315"));
            r.weather.add(new Weather.WeatherEntry(Weathers.sandstorm));
            r.env = Env.terrestrial;
            r.placeRangeCheck = true;
        };
    }
}