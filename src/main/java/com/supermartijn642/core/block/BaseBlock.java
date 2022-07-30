package com.supermartijn642.core.block;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class BaseBlock extends Block {

    private final boolean saveTileData;
    private final BlockProperties properties;

    private BaseBlock(boolean saveTileData, Properties properties, BlockProperties blockProperties){
        super(properties);
        this.saveTileData = saveTileData;
        this.properties = blockProperties;
    }

    public BaseBlock(boolean saveTileData, Properties properties){
        this(saveTileData, properties, null);
    }

    public BaseBlock(boolean saveTileData, BlockProperties properties){
        this(saveTileData, properties.toUnderlying(), properties);
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        CompoundNBT tag = stack.getTag();
        tag = tag == null ? null : tag.contains("tileData") ? tag.getCompound("tileData") : null;
        if(tag == null || tag.isEmpty())
            return;

        TileEntity entity = worldIn.getBlockEntity(pos);
        if(entity instanceof BaseBlockEntity)
            ((BaseBlockEntity)entity).readData(tag);
    }

    @Override
    public ResourceLocation getLootTable(){
        if(this.drops == null){
            if(this.properties.lootTableBlock == null)
                return super.getLootTable();

            Block block = this.properties.lootTableBlock.get();
            if(block == null){
                CoreLib.LOGGER.warn("Received null block from BlockProperties#lootTableFrom's supplier for block '" + Registries.BLOCKS.getIdentifier(this) + "'!");
                return super.getLootTable();
            }
            this.drops = block.getLootTable();
        }

        return this.drops;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder){
        List<ItemStack> items = super.getDrops(state, builder);

        if(!this.saveTileData)
            return items;

        TileEntity entity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if(!(entity instanceof BaseBlockEntity))
            return items;

        CompoundNBT entityTag = ((BaseBlockEntity)entity).writeItemStackData();
        if(entityTag == null || entityTag.isEmpty())
            return items;

        CompoundNBT tag = new CompoundNBT();
        tag.put("tileData", entityTag);

        for(ItemStack stack : items){
            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this){
                stack.setTag(tag);
            }
        }

        return items;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player){
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if(!this.saveTileData)
            return stack;

        TileEntity entity = world.getBlockEntity(pos);
        if(!(entity instanceof BaseBlockEntity))
            return stack;

        CompoundNBT entityTag = ((BaseBlockEntity)entity).writeItemStackData();
        if(entityTag == null || entityTag.isEmpty())
            return stack;

        CompoundNBT tag = new CompoundNBT();
        tag.put("tileData", entityTag);

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this)
            stack.setTag(tag);

        return stack;
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult){
        return this.interact(state, level, pos, player, hand, hitResult.getDirection(), hitResult.getLocation()).interactionResult;
    }

    /**
     * Called when a player interacts with this block.
     * @return whether the player's interaction should be consumed or passed on
     */
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        return InteractionFeedback.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader level, List<ITextComponent> information, ITooltipFlag flag){
        this.appendItemInformation(stack, level, information::add, flag.isAdvanced());
        super.appendHoverText(stack, level, information, flag);
    }

    /**
     * Adds information to be displayed when hovering over the item corresponding to this block in the inventory.
     * @param stack    the stack being hovered over
     * @param level    the world the player is in, may be {@code null}
     * @param info     consumes the information which should be added
     * @param advanced whether advanced tooltips is enabled
     */
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
    }

    @Override
    public boolean isAir(BlockState state, IBlockReader world, BlockPos pos){
        return (this.properties != null && this.properties.isAir) || super.isAir(state, world, pos);
    }

    @Override
    public boolean isAir(BlockState state){
        return this.properties != null && this.properties.isAir;
    }

    @Override
    public int getLightEmission(BlockState state){
        return this.properties != null ? this.properties.lightLevel.applyAsInt(state) : super.getLightEmission(state);
    }

    @Override
    public boolean isRedstoneConductor(BlockState state, IBlockReader level, BlockPos pos){
        return this.properties != null ? this.properties.isRedstoneConductor.test(state, level, pos) : super.isRedstoneConductor(state, level, pos);
    }

    @Override
    public boolean isSuffocating(BlockState state, IBlockReader level, BlockPos pos){
        return this.properties != null ? this.properties.isSuffocating.test(state, level, pos) : super.isSuffocating(state, level, pos);
    }

    public final boolean requiresCorrectToolForDrops(){
        return this.properties.requiresCorrectTool;
    }

    protected enum InteractionFeedback {
        PASS(ActionResultType.PASS), CONSUME(ActionResultType.CONSUME), SUCCESS(ActionResultType.SUCCESS);

        private final ActionResultType interactionResult;

        InteractionFeedback(ActionResultType interactionResult){
            this.interactionResult = interactionResult;
        }
    }
}
