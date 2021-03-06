package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.network.GeneralUsage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.util.HQMUtil;
import hardcorequesting.util.Translator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class ItemQuestBook extends Item {

    private static final String NBT_PLAYER = "UseAsPlayer";

    public ItemQuestBook() {
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setMaxStackSize(1);
        this.setRegistryName(ItemInfo.BOOK_UNLOCALIZED_NAME);
        this.setTranslationKey(ItemInfo.LOCALIZATION_START + ItemInfo.BOOK_UNLOCALIZED_NAME);
    }

    public static ItemStack getOPBook(EntityPlayer player) {
        ItemStack stack = new ItemStack(ModItems.book, 1, 1);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(NBT_PLAYER, player.getPersistentID().toString());
        stack.setTagCompound(nbt);
        return stack;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        if (world.isRemote && Quest.isEditing == true && !HQMUtil.isGameSingleplayer()) {
            Quest.setEditMode(false);
        }

        if (!world.isRemote && Quest.isEditing == true && HQMUtil.isGameSingleplayer() && QuestLine.doServerSync) {
            player.sendMessage(new TextComponentTranslation("hqm.command.editMode.disableSync").setStyle(new Style().setColor(TextFormatting.RED).setBold(true)));
            Quest.setEditMode(false);
        }

        if (!world.isRemote && player instanceof EntityPlayerMP) {
            ItemStack stack = player.getHeldItem(hand);
            if (!QuestingData.isQuestActive()) {
                player.sendMessage(Translator.translateToIChatComponent("hqm.message.noQuestYet"));
            } else {
                if (stack.getItemDamage() == 1) {
                    NBTTagCompound compound = stack.getTagCompound();
                    if (compound != null && compound.hasKey(NBT_PLAYER)) {
                        String uuidS = compound.getString(NBT_PLAYER);
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(uuidS);
                        } catch (IllegalArgumentException e) {
                            compound.removeTag(NBT_PLAYER);
                            return new ActionResult<>(EnumActionResult.FAIL, stack);
                        }
                        if (QuestingData.hasData(uuid)) {
                            if (CommandHandler.isOwnerOrOp(player)) {
                                EntityPlayer subject = QuestingData.getPlayer(uuid);
                                if (subject instanceof EntityPlayerMP) {
                                    EventTrigger.instance().onEvent(new EventTrigger.BookOpeningEvent(player.getName(), true, false));
                                    PlayerEntry entry = QuestingData.getQuestingData(subject).getTeam().getEntry(subject.getUniqueID());
                                    if (entry != null) {
                                        entry.setBookOpen(true);
                                        GeneralUsage.sendOpenBook(player, true);
                                    } else {
                                        player.sendMessage(new TextComponentTranslation("hqm.message.bookNoEntry"));
                                    }
                                }
                                //player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.alreadyEditing"));
                            } else {
                                player.sendMessage(Translator.translateToIChatComponent("hqm.message.bookNoPermission"));
                            }
                        } else {
                            player.sendMessage(new TextComponentTranslation("hqm.message.bookNoData"));
                        }
                    }
                } else {
                    EventTrigger.instance().onEvent(new EventTrigger.BookOpeningEvent(player.getName(), false, true));
                    PlayerEntry entry = QuestingData.getQuestingData(player).getTeam().getEntry(player.getUniqueID());
                    if (entry != null) {
                        entry.setBookOpen(true);
                        GeneralUsage.sendOpenBook(player, false);
                    } else {
                        player.sendMessage(new TextComponentTranslation("hqm.message.bookNoPlayer"));
                    }
                }
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "_" + stack.getItemDamage();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.getItemDamage() == 1) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound != null && compound.hasKey(NBT_PLAYER)) {
                EntityPlayer useAsPlayer = QuestingData.getPlayer(compound.getString(NBT_PLAYER));
                tooltip.add(Translator.translate("item.hqm:quest_book_1.useAs", useAsPlayer == null ? "INVALID" : useAsPlayer.getDisplayNameString()));
            } else
                tooltip.add(GuiColor.RED + Translator.translate("item.hqm:quest_book_1.invalid"));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.getItemDamage() == 1;
    }
}
