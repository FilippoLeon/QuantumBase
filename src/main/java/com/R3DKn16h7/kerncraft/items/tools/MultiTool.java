package com.R3DKn16h7.kerncraft.items.tools;

import com.R3DKn16h7.kerncraft.capabilities.itemhandler.ItemHandlerCapabilityProvider;
import com.R3DKn16h7.kerncraft.items.BasicItem;
import com.R3DKn16h7.kerncraft.utils.PlayerHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Created by filippo on 05-May-17.
 */
public class MultiTool extends BasicItem {
    public MultiTool(String name) {
        super(name);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {

        ItemStack stack = playerIn.getHeldItem(handIn);
        if (PlayerHelper.isCtrlKeyDown()) {
            nextItem(stack);
        } else if (PlayerHelper.isShiftDown()) {
            if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler cap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

                ItemStack other = playerIn.getHeldItem(PlayerHelper.otherHand(handIn));
                if (!other.isEmpty()) {
                    ItemStack split = other.splitStack(1);
                    ItemStack amount = cap.insertItem(getNumberOfItems(stack), split, false);
                    if (!amount.isEmpty()) {
                        other.splitStack(-1);
                    } else {
                        setNumberOfItems(stack, getNumberOfItems(stack) + 1);
                    }
                } else {
                    if (getNumberOfItems(stack) > 0) {
                        ItemStack extract = cap.extractItem(getNumberOfItems(stack) - 1, 1, false);
                        if (!extract.isEmpty()) {
                            playerIn.setHeldItem(PlayerHelper.otherHand(handIn), extract);
                            setNumberOfItems(stack, getNumberOfItems(stack) - 1);
                        }
                    }
                }
            }
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return this.getName() + currentHeldItem.getDisplayName();

//        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        if (!currentHeldItem.isEmpty()) {
            tooltip.add(String.format("Item: %s", stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                    .getStackInSlot(getCurrentItem(stack)).getDisplayName()));
            currentHeldItem.getItem().addInformation(currentHeldItem, playerIn, tooltip, advanced);
            tooltip.add("------------");
        }

        tooltip.add(String.format("Item %d/%d (max: 5)", getCurrentItem(stack), getNumberOfItems(stack)));

        super.addInformation(stack, playerIn, tooltip, advanced);
    }

    public ItemStack getCurrentHeldItem(ItemStack stack) {
        int currentItem = getCurrentItem(stack);
        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                null).getStackInSlot(currentItem);
    }

    private int getCurrentItem(ItemStack stack) {
        NBTTagCompound nbt;
        if (stack.hasTagCompound()) {
            nbt = stack.getTagCompound();
        } else {
            nbt = new NBTTagCompound();
        }

        if (nbt.hasKey("currentItem")) {
            return nbt.getInteger("currentItem");
        } else {
            return 0;
        }
    }

    private void nextItem(ItemStack stack) {
        NBTTagCompound nbt;
        if (stack.hasTagCompound()) {
            nbt = stack.getTagCompound();
        } else {
            nbt = new NBTTagCompound();
        }

        int currentItem;
        if (nbt.hasKey("currentItem")) {
            currentItem = (nbt.getInteger("currentItem") + 1) % getNumberOfItems(stack);
        } else {
            currentItem = 1 % getNumberOfItems(stack);
        }
        nbt.setInteger("currentItem", currentItem);
        stack.setTagCompound(nbt);

        ItemStack currentHeldItem = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(currentItem);

    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        int currentItem = getCurrentItem(stack);
        ItemStack currentHeldItem = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(currentItem);

        return currentHeldItem.canHarvestBlock(state);
    }


    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        int currentItem = getCurrentItem(stack);
        ItemStack currentHeldItem = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(currentItem);

        return getHarvestLevel(currentHeldItem, toolClass, player, blockState);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack currentHeldItem = getCurrentHeldItem(player.getHeldItem(hand));
        currentHeldItem.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        if (attacker instanceof EntityPlayer) {
            currentHeldItem.hitEntity(target, ((EntityPlayer) attacker));
        }
        return false;
    }

    @Override
    public float getStrVsBlock(ItemStack stack, IBlockState state) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getStrVsBlock(state);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.onItemUseFinish(worldIn, entityLiving);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        if (entityLiving instanceof EntityPlayer) {
            currentHeldItem.onBlockDestroyed(worldIn, state, pos, ((EntityPlayer) entityLiving));
        }

        return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        currentHeldItem.getItem().onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        super.onCreated(stack, worldIn, playerIn);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getItemUseAction();
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        currentHeldItem.onPlayerStoppedUsing(worldIn, entityLiving, timeLeft);

        super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {

        ItemStack currentHeldItem = getCurrentHeldItem(player.getHeldItem(hand));
        return currentHeldItem.onItemUseFirst(player, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        ItemStack currentHeldItem = getCurrentHeldItem(itemstack);
        return currentHeldItem.getItem().onBlockStartBreak(currentHeldItem, pos, player);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        currentHeldItem.getItem().onUsingTick(currentHeldItem, player, count);

        super.onUsingTick(stack, player, count);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getItem().onLeftClickEntity(currentHeldItem, player, entity);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getItem().onEntitySwing(entityLiving, currentHeldItem);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getItem().showDurabilityBar(currentHeldItem);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getItem().getDurabilityForDisplay(currentHeldItem);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        currentHeldItem.getItem().getRGBDurabilityForDisplay(currentHeldItem);

        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        return currentHeldItem.getMaxDamage();
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        currentHeldItem.setItemDamage(damage);

        super.setDamage(stack, damage);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {

        ItemStack currentHeldItem = getCurrentHeldItem(stack);
        currentHeldItem.getItem().getToolClasses(currentHeldItem);

        return super.getToolClasses(stack);
    }

    private int getNumberOfItems(ItemStack stack) {
        NBTTagCompound nbt;
        if (stack.hasTagCompound()) {
            nbt = stack.getTagCompound();
        } else {
            nbt = new NBTTagCompound();
        }

        if (nbt.hasKey("numberOfItems")) {
            return nbt.getInteger("numberOfItems");
        } else {
            return 0;
        }
    }

    private void setNumberOfItems(ItemStack stack, int val) {
        NBTTagCompound nbt;
        if (stack.hasTagCompound()) {
            nbt = stack.getTagCompound();
        } else {
            nbt = new NBTTagCompound();
        }

        nbt.setInteger("numberOfItems", val);
        stack.setTagCompound(nbt);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ItemHandlerCapabilityProvider(5);
    }
}
