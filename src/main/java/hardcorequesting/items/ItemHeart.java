package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.death.DeathType;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.Translator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class ItemHeart extends Item {
    private int value;
    
    public ItemHeart(int value) {
        super();
        this.value = value;
        
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(64);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setRegistryName(ItemInfo.HEART_ICONS[value]);
        this.setTranslationKey(ItemInfo.LOCALIZATION_START + ItemInfo.HEART_ICONS[value]);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if (value == 3) {
                if (!QuestingData.isHardcoreActive()) {
                    player.sendMessage(new TextComponentTranslation("hqm.message.noHardcoreYet"));
                } else if (QuestingData.getQuestingData(player).getRawLives() < HQMConfig.Hardcore.MAX_LIVES) {
                    QuestingData.getQuestingData(player).addLives(player, 1);
                    player.sendMessage(new TextComponentTranslation("hqm.message.addOne"));
                    int lives = QuestingData.getQuestingData(player).getLives();
                    player.sendMessage(new TextComponentTranslation("hqm.message.haveRemaining", lives));
                    SoundHandler.play(Sounds.LIFE, player);
                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);

                    }
                } else {
                    player.sendMessage(new TextComponentTranslation("hqm.message.haveMaxLives"));
                }
            }
            if (value == 4) {
                if (!QuestingData.isHardcoreActive()) {
                    player.sendMessage(new TextComponentTranslation("hqm.message.noHardcoreYet"));
                } else {
                    SoundHandler.play(Sounds.ROTTEN, player);
                    player.sendMessage(new TextComponentTranslation("hqm.message.eatRottenHearth"));
                    QuestingData.getQuestingData(player).removeLifeAndSendMessage(player);
                    DeathType.HQM.onDeath(player);

                    if (!player.capabilities.isCreativeMode)
                        stack.shrink(1);
                }

            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (value == 3 && HQMConfig.Hardcore.HEART_ROT_ENABLE) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    stack.setTagCompound(tagCompound);
                }
                if (!tagCompound.hasKey("RotTime")) {
                    int rot = (HQMConfig.Hardcore.HEART_ROT_TIME * 20);
                    tagCompound.setInteger("MaxRot", rot);
                    tagCompound.setInteger("RotTime", rot);
                } else {
                    int newRot = tagCompound.getInteger("RotTime");
                    if (newRot <= 0) {
                        stack = new ItemStack(ModItems.rottenheart);
                        entityPlayer.sendMessage(new TextComponentTranslation("hqm.message.hearthDecay"));
                    } else {
                        tagCompound.setInteger("RotTime", newRot - 1);
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (value == 3) {
            tooltip.add(Translator.translate("item.hqm:hearts_heart.tooltip"));
            if (HQMConfig.Hardcore.HEART_ROT_ENABLE) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    stack.setTagCompound(tagCompound);
                }
                if (tagCompound.hasKey("RotTime")) {
                    int rot = tagCompound.getInteger("RotTime");
                    int maxRot = tagCompound.getInteger("MaxRot");
                    float percentage = (float) ((rot * 100) / maxRot);
                    tooltip.add(Translator.translate("item.hqm:hearts_heart.freshness", percentage));
                }
            }
        }
        if (value == 4) {
            tooltip.add(Translator.translate("item.hqm:hearts_rottenheart.tooltip"));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return value == 3 || value == 4;
    }
}
