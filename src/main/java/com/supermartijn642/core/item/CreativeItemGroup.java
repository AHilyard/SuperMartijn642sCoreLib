package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class CreativeItemGroup extends CreativeModeTab {

    public static CreativeItemGroup create(String modid, String name, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Item group name '" + name + "' must only contain characters [a-z0-9_.-]!");

        String identifier = modid + "." + name;
        String translationKey = modid + ".item_group." + name;
        return new CreativeItemGroup(identifier, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, String name, ItemLike icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        String translationKey = modid + ".item_group";
        return new CreativeItemGroup(modid, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, ItemLike icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeModeTab getBuildingBlocks(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getDecoration(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getRedstone(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getTransportation(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getMisc(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getSearch(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getFood(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getTools(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getCombat(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getBrewing(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    private final String identifier;
    private final Component displayName;
    private final Supplier<ItemStack> icon;

    private CreativeItemGroup(String identifier, String translationKey, Supplier<ItemStack> icon){
        super(identifier);
        this.identifier = identifier;
        this.displayName = TextComponents.translation(translationKey).get();
        this.icon = icon;
    }

    @Override
    public ItemStack makeIcon(){
        ItemStack stack = this.icon.get();
        if(stack == null || stack.isEmpty())
            throw new RuntimeException("Item group '" + this.identifier + "'s icon stack must not be empty!");
        return stack;
    }

    @Override
    public Component getDisplayName(){
        return this.displayName;
    }
}
