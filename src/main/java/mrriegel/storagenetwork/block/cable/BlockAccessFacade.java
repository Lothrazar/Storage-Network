package mrriegel.storagenetwork.block.cable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAccessFacade implements IBlockAccess {
    
    private IBlockAccess world;

    public BlockAccessFacade(IBlockAccess world) {
        this.world = world;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return world.getTileEntity(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return world.getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCable) {
            TileCable cable = (TileCable) te;
            if (cable.facadeState != null)
                return cable.facadeState;
        }
        return world.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return world.isAirBlock(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Biome getBiome(BlockPos pos) {
        return world.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return world.getStrongPower(pos, direction);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public WorldType getWorldType() {
        return world.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return world.isSideSolid(pos, side, _default);
    }
}
