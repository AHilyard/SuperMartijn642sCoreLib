package com.supermartijn642.core.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public interface EntityHoldingBlock extends ITileEntityProvider {

    TileEntity createNewBlockEntity();

    @Nullable
    @Override
    default TileEntity newBlockEntity(IBlockReader level){
        return this.createNewBlockEntity();
    }
}
