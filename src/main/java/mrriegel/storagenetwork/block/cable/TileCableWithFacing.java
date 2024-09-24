package mrriegel.storagenetwork.block.cable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.block.master.TileMaster;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileCableWithFacing extends TileCable {

  @Nullable
  EnumFacing direction = null;

  public boolean hasDirection() {
    return direction != null;
  }

  public EnumFacing getDirection() {
    return direction;
  }

  public BlockPos getFacingPosition() {
    if (direction == null) {
      return null;
    }
    return this.getPos().offset(direction);
  }

  public void setDirection(@Nullable EnumFacing direction) {
    this.direction = direction;
  }

  protected boolean isValidLinkNeighbor(EnumFacing facing) {
    if (facing == null) {
      return false;
    }
    if (!TileMaster.isTargetAllowed(world.getBlockState(pos.offset(facing)))) {
      return false;
    }
    TileEntity neighbor = world.getTileEntity(pos.offset(facing));
    if (neighbor != null && neighbor.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
      return true;
    }
    return false;
  }

  public void findNewDirection() {
    if (isValidLinkNeighbor(direction)) {
      return;
    }
    for (EnumFacing facing : EnumFacing.values()) {
      if (isValidLinkNeighbor(facing)) {
        setDirection(facing);
        return;
      }
    }
    setDirection(null);
  }

  public void rotate() {
    EnumFacing previous = direction;
    List<EnumFacing> targetFaces = Arrays.asList(EnumFacing.values());
    Collections.shuffle(targetFaces);
    for (EnumFacing facing : EnumFacing.values()) {
      if (previous == facing) {
        continue;
      }
      if (isValidLinkNeighbor(facing)) {
        setDirection(facing);
        this.markDirty();
        if (previous != direction) {
          TileMaster master = getTileMaster();
          if (master != null) {
            master.refreshNetwork();
          }
        }
        return;
      }
    }
  }

  public TileMaster getTileMaster() {
    if (getMaster() == null) {
      return null;
    }
    return getMaster().getTileEntity(TileMaster.class);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    if (compound.hasKey("direction")) {
      this.direction = EnumFacing.byIndex(compound.getInteger("direction"));
    }
    else {
      this.direction = null;
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    if (direction != null) {
      compound.setInteger("direction", this.direction.ordinal());
    }
    return super.writeToNBT(compound);
  }
}
