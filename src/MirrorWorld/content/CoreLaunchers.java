package MirrorWorld.content;

import arc.Core;
import arc.Events;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.type.Sector;
import mindustry.ui.dialogs.LaunchLoadoutDialog;
import mindustry.ui.dialogs.PlanetDialog;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import java.lang.reflect.Field;

import static mindustry.Vars.*;

/**
 * 核心发射器的运行期判定：
 *   - 某个区块当前拥有的发射器档位（建造时实时扫描，离开区块后写入设置持久化）；
 *   - 目标区块是否在射程内；
 *   - 发射的核心尺寸是否被该档位允许；
 *   - 拦截原版发射流程（没有合格发射器就不允许发射）。
 */
public final class CoreLaunchers {

    private static final String PREFIX = "mirrorworld-launcher-";

    private static boolean inited;
    private static int cachedCount = -1;
    private static int cachedLevel;
    private static int lastCount = -1;

    private CoreLaunchers(){
    }

    static void init(){
        if(inited) return;
        inited = true;

        Events.run(Trigger.update, () -> {
            if(headless || state == null || state.rules == null || state.rules.sector == null) return;
            int count = Groups.build.size();
            if(count != lastCount){
                lastCount = count;
                refresh();
            }
        });
    }

    /** 把当前区块的发射器档位写入设置，供离开区块后（星图界面）判定使用。 */
    public static void refresh(){
        if(headless || state == null || state.rules == null || state.rules.sector == null) return;
        lastCount = Groups.build.size();
        Core.settings.put(key(state.rules.sector), worldLevel());
    }

    static String key(Sector sector){
        return PREFIX + sector.planet.name + "-" + sector.id;
    }

    /** 某区块可用的最高发射器档位；0 表示没有发射器。 */
    public static int level(Sector sector){
        if(sector == null) return 0;
        if(sector.isBeingPlayed()) return worldLevel();
        return Core.settings.getInt(key(sector), 0);
    }

    private static int worldLevel(){
        int count = Groups.build.size();
        if(count != cachedCount){
            int max = 0;
            for(Building b : Groups.build){
                if(b instanceof CoreLauncher.CoreLauncherBuild){
                    max = Math.max(max, ((CoreLauncher.CoreLauncherBuild)b).launcherLevel());
                }
            }
            cachedLevel = max;
            cachedCount = count;
        }
        return cachedLevel;
    }

    public static CoreLauncher byLevel(int level){
        switch(level){
            case 1: return CoreLauncher.small;
            case 2: return CoreLauncher.medium;
            case 3: return CoreLauncher.large;
            case 4: return CoreLauncher.finalLauncher;
            default: return null;
        }
    }

    /** 区块里能代表“会被发射出去”的核心（取最大核心）。 */
    static Block bestCore(Sector sector){
        if(sector == null) return null;
        if(sector.isBeingPlayed()){
            CoreBlock.CoreBuild core = state.rules.defaultTeam.cores().max(b -> b.block.size);
            if(core != null) return core.block;
        }
        return sector.info != null && sector.info.bestCoreType instanceof CoreBlock ? sector.info.bestCoreType : null;
    }

    /**
     * from 区块能否把核心发射到 to 区块：
     * 必须有发射器、目标在射程内、待发射核心不超过该发射器的尺寸上限。
     */
    public static boolean canLaunch(Sector from, Sector to){
        if(from == null || to == null || from.planet != to.planet) return false;

        CoreLauncher launcher = byLevel(level(from));
        if(launcher == null) return false;

        Block core = bestCore(from);
        if(core != null && core.size > launcher.maxCoreSize) return false;

        return withinRange(from, to, launcher.sectorRange);
    }

    private static boolean withinRange(Sector from, Sector to, int range){
        if(range <= 0) return true;
        return hops(from, to, range) <= range;
    }

    /** 区块六边形网格上的最短跳数，最多搜索 max 层。 */
    static int hops(Sector from, Sector to, int max){
        if(from == to) return 0;

        ObjectSet<Sector> seen = new ObjectSet<>();
        Seq<Sector> frontier = new Seq<>();
        seen.add(from);
        frontier.add(from);

        for(int depth = 1; depth <= max; depth++){
            Seq<Sector> next = new Seq<>();
            for(Sector s : frontier){
                for(Sector n : s.near()){
                    if(n == null || seen.contains(n)) continue;
                    if(n == to) return depth;
                    seen.add(n);
                    next.add(n);
                }
            }
            if(next.isEmpty()) break;
            frontier = next;
        }
        return Integer.MAX_VALUE;
    }

    /** 客户端安装发射拦截：没有合格发射器时不允许打开发射装载界面。 */
    public static void install(){
        if(headless) return;
        try{
            Field field = PlanetDialog.class.getDeclaredField("loadouts");
            field.setAccessible(true);
            field.set(ui.planet, new GuardedLoadout());
            Log.info("[MirrorWorld] core launch guard installed.");
        }catch(Throwable t){
            Log.err("[MirrorWorld] failed to install core launch guard", t);
        }
    }

    static class GuardedLoadout extends LaunchLoadoutDialog {
        @Override
        public void show(CoreBlock core, Sector sector, Sector destination, Runnable confirm){
            if(sector != null && destination != null && sector.planet == destination.planet && !canLaunch(sector, destination)){
                ui.showInfoFade(Core.bundle.get("mirrorworld.launcher.blocked"));
                return;
            }
            super.show(core, sector, destination, confirm);
        }
    }
}
