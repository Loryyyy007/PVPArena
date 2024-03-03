package me.loryyyy.pvparena.managers;

import lombok.Getter;
import lombok.Setter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.utils.Arena;
import me.loryyyy.pvparena.utils.ConstantPaths;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ArenaCheckTask {

    @Getter
    private final static ArenaCheckTask instance = new ArenaCheckTask();

    @Getter
    private final Map<Player, Arena> playersInArena = new HashMap<>();

    @Getter
    @Setter
    private boolean taskEnabled;
    private BukkitTask task = null;

    private ArenaCheckTask() {
    }

    public void start() {

        final int PERIOD = PVPArena.getInstance().getConfig().getInt(ConstantPaths.ARENA_TASK_PERIOD);
        final int DELAY = PVPArena.getInstance().getConfig().getInt(ConstantPaths.ARENA_TASK_DELAY);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    Location pLoc = p.getLocation();
                    if(!playersInArena.containsKey(p)){
                        for(Arena arena : Arena.getEnabledArenas().values()){
                            if(arena.contains(pLoc)){
                                arena.onJoin(p);
                                playersInArena.put(p, arena);
                                break;
                            }
                        }
                    }else{
                        Arena arena = playersInArena.get(p);
                        if(!arena.contains(pLoc)){
                            arena.onLeave(p);
                            playersInArena.remove(p);
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(PVPArena.getInstance(), DELAY, PERIOD);
    }

    public void cancel() {
        if (task == null) return;
        task.cancel();
        task = null;
    }

}
