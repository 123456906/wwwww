package FusionPlanet.content;

import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

public class Fblocks {
    public static Block coreEvoke;
    public static Block assimilator;

    public static void load() {
        coreEvoke = new CoreBlock("core-evoke") {{
            size = 4;
            squareSprite = false;
            health = 3600;
            itemCapacity = 4000;
            unitType = UnitTypes.evoke;
            requirements(Category.effect, ItemStack.with(
                    Items.copper, 5000,
                    Items.lead, 3000,
                    Items.titanium, 1500
            ));
            alwaysUnlocked = true;
            requirements = ItemStack.with(
                    Items.copper, 100,
                    Items.lead, 50,
                    Items.silicon, 25,
                    Items.graphite, 20
            );
        }};

        assimilator = new Assimilator("assimilator") {{
            size = 3;
            health = 400;
            range = 60f;
            requirements(Category.effect, ItemStack.with(
                    Items.copper, 200,
                    Items.lead, 150,
                    Items.silicon, 100
            ));
            alwaysUnlocked = true;
        }};
    }

    public static class Assimilator extends Block {
        public float range = 60f;
        public float assimilateSpeed = 1f;

        public Assimilator(String name) {
            super(name);
            update = true;
            solid = true;
            configurable = true;
            hasPower = true;
            consumePower(50f);
        }

        public class AssimilatorBuild extends Building {
            private final ObjectMap<Unit, Float> progress = new ObjectMap<>();

            @Override
            public void updateTile() {
                if (power == null || power.graph.getPowerBalance() < 0.1f) return;

                Units.nearby(x, y, range, unit -> {
                    if (unit == null || unit.dead() || unit.team() == team) return;

                    float health = unit.maxHealth();
                    float required = requiredTime(health);
                    float prog = progress.get(unit, 0f);
                    prog += Time.delta * assimilateSpeed;

                    if (prog >= required) {
                        unit.team(team);
                        unit.health(unit.health() * 0.7f);
                        progress.remove(unit);
                    } else {
                        progress.put(unit, prog);
                    }
                });

                // 清理离开范围的单位进度
                for (Unit u : progress.keys().toSeq()) {
                    if (u == null || u.dead() || u.team() == team || u.dst(x, y) > range) {
                        progress.remove(u);
                    }
                }
            }

            private float requiredTime(float health) {
                if (health <= 1000f) {
                    return 20f + 0.08f * (health - 500f);
                } else if (health <= 5000f) {
                    return 60f + 0.01f * (health - 1000f);
                } else {
                    return 100f + 0.04f * (health - 5000f);
                }
            }

            @Override
            public void draw() {
                super.draw();
                Drawf.dashCircle(x, y, range, Color.valueOf("88aaff"));
                Drawf.dashCircle(x, y, range, Color.valueOf("4488ff").a(0.25f));
            }
        }

        @Override
        public Building newBuilding() {
            return new AssimilatorBuild();
        }
    }
}