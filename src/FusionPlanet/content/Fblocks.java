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
            description = "把血量在0到1000内的敌方单位在10到20秒内转化为我方单位并减少1/3的血量";
            requirements(Category.effect, ItemStack.with(
                    Items.copper, 3000,
                    Items.lead, 2000,
                    Items.silicon, 2000
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
                Assimilator self = (Assimilator) block;

                // 搜索所有队伍的单位（null = 全部）
                Units.nearby(null, x, y, self.range, unit -> {
                    // 跳过无效或己方单位
                    if (unit == null || unit.dead() || unit.team() == team) return;

                    float health = unit.maxHealth();
                    float required = requiredTime(health);
                    float prog = progress.get(unit, 0f);
                    prog += Time.delta * self.assimilateSpeed;

                    if (prog >= required) {
                        unit.team(team);
                        unit.health(unit.health() * 0.7f);
                        progress.remove(unit);
                    } else {
                        progress.put(unit, prog);
                    }
                });

                // 清理离开范围或已死亡的单位进度
                for (Unit u : progress.keys().toSeq()) {
                    if (u == null || u.dead() || u.team() == team || u.dst(x, y) > self.range) {
                        progress.remove(u);
                    }
                }
            }

            private float requiredTime(float health) {
                if (health <= 1000f) {
                    return 600f + 0.08f * (health - 500f);
                } else if (health <= 5000f) {
                    return 1200f + 0.01f * (health - 1000f);
                } else {
                    return 9999999999f + 0.04f * (health - 5000f);
                }
            }

            @Override
            public void draw() {
                super.draw();
                Assimilator self = (Assimilator) block;

                Drawf.dashCircle(x, y, self.range, Color.white);
            }
        }
    }
}