package mrriegel.storagenetwork.block.cable;

import mrriegel.storagenetwork.block.TileConnectable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Base class for TileCable
 *
 */
public class TileCable extends TileConnectable {

  public IBlockState facadeState = null;

  public TileCable() {
    super();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    if (compound.hasKey("facade")) {
      NBTTagCompound facadeTag = compound.getCompoundTag("facade");
      Block facade = Block.getBlockFromName(facadeTag.getString("block"));
      facadeState = facade.getStateFromMeta(facadeTag.getInteger("meta"));
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    if (facadeState != null) {
      NBTTagCompound facadeTag = new NBTTagCompound();
      facadeTag.setString("block", ForgeRegistries.BLOCKS.getKey(facadeState.getBlock()).toString());
      facadeTag.setInteger("meta", facadeState.getBlock().getMetaFromState(facadeState));
      compound.setTag("facade", facadeTag);
    }
    return compound;
  }

  @Override
  public AxisAlignedBB getRenderBoundingBox() {
    // if (facadeState != null) {
    //   return facadeState.getActualState(world, pos).getBoundingBox(world, pos);
    // }
    double renderExtention = 1.0d;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - renderExtention, pos.getY() - renderExtention, pos.getZ() - renderExtention, pos.getX() + 1 + renderExtention, pos.getY() + 1 + renderExtention, pos.getZ() + 1 + renderExtention);
    return bb;
  }
}
