package ikexing.atutils.core.events;

import ikexing.atutils.core.block.BlockEvilStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class EventHandler {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.getEntityWorld();

        if (entity instanceof EntityCreature || entity instanceof EntitySlime) {
            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            Iterable<BlockPos> allInBox = BlockPos.getAllInBox(pos.add(4, 5, 4), pos.add(-4, -5, -4));
            for (BlockPos blockPos : allInBox) {
                IBlockState blockState = world.getBlockState(blockPos);
                if (world.getBlockState(blockPos).getBlock() instanceof BlockEvilStone) {
                    Integer status = blockState.getValue(BlockEvilStone.STATUS);
                    if (blockState.getValue(BlockEvilStone.STATUS) < 5) {
                        if (!world.isRemote) {
                            world.setBlockState(blockPos, blockState.withProperty(BlockEvilStone.STATUS, status + 1));
                        }  // todo 特效

                        break;
                    }
                }
            }
        }
    }


}