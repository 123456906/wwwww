package FusionPlanet.content;

import arc.graphics.Color;
import mindustry.content.StatusEffects;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.ShallowLiquid;
import mindustry.world.blocks.environment.StaticWall;

public class AntichthonBlocks {
    public static Liquid liquidCO2;

    public static Floor co2Shallow, co2Deep;
    public static Floor redSand, darkRedSand, rustyGravel, redRock, hematiteRock;
    public static Floor dryIce, deepDryIce, paleMoss, deepPaleMoss, olivineSand, lichen;

    public static void load() {
        liquidCO2 = new Liquid("liquid-co2", Color.valueOf("6773D6")) {{
            heatCapacity = 0.6f;
            temperature = 0.1f;
            effect = StatusEffects.freezing;
            alwaysUnlocked = true;
            boilPoint = 0.85f;
        }};

        co2Shallow = new ShallowLiquid("co2-shallow") {{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            liquidDrop = liquidCO2;
            variants = 0;
            mapColor.set(Color.valueOf("6773D680"));
        }};

        co2Deep = new Floor("co2-deep") {{
            speedMultiplier = 0.18f;
            variants = 0;
            status = StatusEffects.freezing;
            statusDuration = 140f;
            drownTime = 200f;
            liquidDrop = liquidCO2;
            liquidMultiplier = 1.5f;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("4E569D80"));
        }};

        redSand = floor("red-sand", "b05a3a", "8a4530", 3);
        darkRedSand = floor("dark-red-sand", "8a3a25", "5a2518", 3);
        rustyGravel = floor("rusty-gravel", "9a5a3a", "6a3a25", 3);
        redRock = floor("red-rock", "6a3025", "4a2018", 2);
        hematiteRock = floor("hematite-rock", "4a1a15", "2a0e08", 2);
        dryIce = floor("dry-ice", "d8e4f0", "a8b8c8", 3);
        deepDryIce = floor("deep-dry-ice", "eef4fc", "c8d8e8", 2);
        paleMoss = floor("pale-moss", "7a8ac8", "5a6a98", 3);
        deepPaleMoss = floor("deep-pale-moss", "5a6a98", "3a4a78", 2);
        olivineSand = floor("olivine-sand", "5a6a4a", "3a4a2a", 2);

        lichen = new Floor("lichen") {{
            mapColor.set(Color.valueOf("a8b8b8"));
            variants = 2;
            albedo = 0.6f;
        }};
        StaticWall lichenWall = new StaticWall("lichen-wall") {{
            mapColor.set(Color.valueOf("889898"));
        }};
        lichen.wall = lichenWall;
    }

    private static Floor floor(String name, String color, String wallColor, int variants) {
        Floor floor = new Floor(name);
        floor.mapColor.set(Color.valueOf(color));
        floor.variants = variants;
        floor.supportsOverlay = true;

        StaticWall wall = new StaticWall(name + "-wall");
        wall.mapColor.set(Color.valueOf(wallColor));
        wall.variants = variants;
        floor.wall = wall;
        return floor;
    }
}