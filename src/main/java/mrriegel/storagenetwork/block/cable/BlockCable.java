package mrriegel.storagenetwork.block.cable;

import java.util.List;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.block.AbstractBlockConnectable;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCable extends AbstractBlockConnectable {

  public static final UnlistedPropertyBlockNeighbors BLOCK_NEIGHBORS = new UnlistedPropertyBlockNeighbors();

  public BlockCable(String registryName) {
    super(Material.ROCK, registryName);
    this.setHardness(1.4F);
    this.setCreativeTab(CreativeTab.tab);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected BlockStateContainer createBlockState() {
    IProperty[] listedProperties = new IProperty[0];
    IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] { BLOCK_NEIGHBORS };
    return new ExtendedBlockState(this, listedProperties, unlistedProperties);
  }

  protected UnlistedPropertyBlockNeighbors.BlockNeighbors getBlockNeighbors(IBlockAccess world, BlockPos pos) {
    UnlistedPropertyBlockNeighbors.BlockNeighbors blockNeighbors = new UnlistedPropertyBlockNeighbors.BlockNeighbors();
    for (EnumFacing facing : EnumFacing.values()) {
      TileMaster tileMaster = getClientSideTileEntity(world, pos.offset(facing), TileMaster.class);
      IConnectable connectable = getClientSideCapability(world, pos.offset(facing), StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
      if (connectable == null && tileMaster == null) {
        continue;
      }
      blockNeighbors.setNeighborType(facing, UnlistedPropertyBlockNeighbors.EnumNeighborType.CABLE);
    }
    return blockNeighbors;
  }

  @Override
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
    return extendedBlockState.withProperty(BLOCK_NEIGHBORS, getBlockNeighbors(world, pos));
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <V> V getClientSideTileEntity(IBlockAccess world, BlockPos pos, Class<V> tileEntityClassOrInterface) {
    TileEntity tileEntity = world.getTileEntity(pos);
    if (tileEntity == null) {
      return null;
    }
    if (!tileEntityClassOrInterface.isAssignableFrom(tileEntity.getClass())) {
      return null;
    }
    return (V) tileEntity;
  }

  @Nullable
  private <V> V getClientSideCapability(IBlockAccess world, BlockPos pos, Capability<V> capability, EnumFacing side) {
    TileEntity tileEntity = world.getTileEntity(pos);
    if (tileEntity == null) {
      return null;
    }
    if (!tileEntity.hasCapability(capability, side)) {
      return null;
    }
    return tileEntity.getCapability(capability, side);
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileCable();
  }

  @Override
  public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    return false;
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_) {
    return BlockFaceShape.MIDDLE_POLE_THIN;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isTranslucent(IBlockState state) {
    return true;
  }

  @Override
  public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return 0;
  }

  @Override
  public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
    return layer == BlockRenderLayer.SOLID;
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.INVISIBLE;
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntity tile = worldIn.getTileEntity(pos);
    if (!(tile instanceof TileCable)) {
      return false;
    }
    if (worldIn.isRemote) {
      return true;
    }
    // TODO: Move gui open actions to the block classes
    if (tile.getBlockType() == ModBlocks.exKabel) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.GuiIDs.EXPORT.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    if (tile.getBlockType() == ModBlocks.imKabel) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.GuiIDs.IMPORT.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    if (tile.getBlockType() == ModBlocks.storageKabel) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.GuiIDs.LINK.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    if (tile.getBlockType() == ModBlocks.processKabel) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.GuiIDs.PROCESSING.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    return false;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
    TileEntity tileHere = worldIn.getTileEntity(pos);
    if (!(tileHere instanceof TileCable)) {
      return;
    }
    float f = 0.3125F;
    float f1 = 0.6875F;
    float f2 = 0.3125F;
    float f3 = 0.6875F;
    float f4 = 0.3125F;
    float f5 = 0.6875F;
    addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    UnlistedPropertyBlockNeighbors.BlockNeighbors neighbors = getBlockNeighbors(worldIn, pos);
    if (neighbors.north() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      f2 = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (neighbors.south() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      f3 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (neighbors.west() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      f = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (neighbors.east() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      f1 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (neighbors.down() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      f4 = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (neighbors.up() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      f5 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
    UnlistedPropertyBlockNeighbors.BlockNeighbors neighbors = getBlockNeighbors(world, pos);
    float x1 = 0.37F;
    float x2 = 0.63F;
    float y1 = 0.37F;
    float y2 = 0.63F;
    float z1 = 0.37F;
    float z2 = 0.63F;
    if (neighbors.north() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      y1 = 0f;
    }
    if (neighbors.south() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      y2 = 1f;
    }
    if (neighbors.west() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      x1 = 0f;
    }
    if (neighbors.east() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      x2 = 1f;
    }
    if (neighbors.down() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      z1 = 0f;
    }
    if (neighbors.up() != UnlistedPropertyBlockNeighbors.EnumNeighborType.NONE) {
      z2 = 1f;
    }
    return new AxisAlignedBB(x1, z1, y1, x2, z2, y2);
  }

  @Override
  public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, playerIn, tooltip, advanced);
    if (stack.getItem() == Item.getItemFromBlock(ModBlocks.exKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_E"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.imKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_I"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.storageKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_S"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.kabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_L"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.processKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_P"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.simple_kabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.simple_kabel"));
  }
}
