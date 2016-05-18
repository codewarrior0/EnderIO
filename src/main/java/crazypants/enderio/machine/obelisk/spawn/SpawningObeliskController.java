package crazypants.enderio.machine.obelisk.spawn;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import crazypants.enderio.config.Config;

public class SpawningObeliskController {

  public static SpawningObeliskController instance = new SpawningObeliskController();
  
  static {
    MinecraftForge.EVENT_BUS.register(SpawningObeliskController.instance);
  }

  private Map<Integer, Map<BlockPos, ISpawnCallback>> perWorldGuards = new HashMap<Integer, Map<BlockPos, ISpawnCallback>>();

  private SpawningObeliskController() {
  }

  public <T extends TileEntity & ISpawnCallback> void registerGuard(T guard) {
    if(guard == null) {
      return;
    }    
    Map<BlockPos, ISpawnCallback> chargers = getGuardsForWorld(guard.getWorld());
    chargers.put(guard.getPos(), guard);
  }

  public <T extends TileEntity & ISpawnCallback> void deregisterGuard(T guard) {
    if(guard == null) {
      return;
    }
    Map<BlockPos, ISpawnCallback> chargers = getGuardsForWorld(guard.getWorld());
    chargers.remove(guard.getPos());
  }
  
  @SubscribeEvent
  public void onEntitySpawn(LivingSpawnEvent.CheckSpawn evt) {
    if (evt.getResult() == Result.DENY) {
      return;
    }
    
    if(Config.spawnGuardStopAllSlimesDebug && evt.entity instanceof EntitySlime) {
      evt.setResult(Result.DENY);
      return;
    }
    if(Config.spawnGuardStopAllSquidSpawning && evt.entity.getClass() == EntitySquid.class) {
      evt.setResult(Result.DENY);
      return;
    }
    
    
    Map<BlockPos, ISpawnCallback> guards = getGuardsForWorld(evt.world);
    for (ISpawnCallback guard : guards.values()) {
      ISpawnCallback.Result result = guard.isSpawnPrevented(evt.entityLiving);
      if (result == ISpawnCallback.Result.DENY) {
        evt.setResult(Result.DENY);
        return;
      } else if (result == ISpawnCallback.Result.DONE) {
        return;
      }
    }    
  }
  
  private Map<BlockPos, ISpawnCallback> getGuardsForWorld(World world) {
    Map<BlockPos, ISpawnCallback> res = perWorldGuards.get(world.provider.getDimensionId());
    if(res == null) {
      res = new HashMap<BlockPos, ISpawnCallback>();
      perWorldGuards.put(world.provider.getDimensionId(), res);
    }
    return res;
  }
  
}
