package mrriegel.storagenetwork.block.cable;

import mrriegel.storagenetwork.block.TileConnectable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Base class for TileCable
 *
 */
public class TileCable extends TileConnectable {

  private static final String NBT_META = "meta";
  private static final String NBT_BLOCK = "block";
  private static final String NBT_FACADE = "facade";
  private IBlockState facadeState = null;

  public TileCable() {
    super();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    if (compound.hasKey(NBT_FACADE)) {
      setFacadeState(null); // null until we have it set
      NBTTagCompound facadeTag = compound.getCompoundTag(NBT_FACADE);
      if (facadeTag.hasKey(NBT_BLOCK)) {
        Block facade = Block.getBlockFromName(facadeTag.getString(NBT_BLOCK));
        setFacadeState(facade.getStateFromMeta(facadeTag.getInteger(NBT_META)));
      }
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    NBTTagCompound facadeTag = new NBTTagCompound();
    if (facadeState != null) {
      facadeTag.setString(NBT_BLOCK, ForgeRegistries.BLOCKS.getKey(getFacadeState().getBlock()).toString());
      facadeTag.setInteger(NBT_META, getFacadeState().getBlock().getMetaFromState(getFacadeState()));
    }
    compound.setTag(NBT_FACADE, facadeTag);
    return compound;
  }

  @Override
  public AxisAlignedBB getRenderBoundingBox() {
    double renderExtention = 1.0d;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - renderExtention, pos.getY() - renderExtention, pos.getZ() - renderExtention, pos.getX() + 1 + renderExtention, pos.getY() + 1 + renderExtention, pos.getZ() + 1 + renderExtention);
    return bb;
  }

  public IBlockState getFacadeState() {
    return facadeState;
  }

  public void setFacadeState(IBlockState facadeState) {
    this.facadeState = facadeState;
  }

  @SuppressWarnings("deprecation")
  public void setFacadeState(String block, int meta) {
    Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(block));
    this.facadeState = b.getStateFromMeta(meta);
  }
}
