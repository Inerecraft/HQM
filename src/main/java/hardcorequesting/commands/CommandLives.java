package hardcorequesting.commands;

import hardcorequesting.config.HQMConfig;
import hardcorequesting.quests.QuestingData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;

public class CommandLives extends CommandBase {

    private static String ADD = "add";
    private static String REMOVE = "remove";

    public CommandLives() {
        super("lives", ADD, REMOVE);
        permissionLevel = 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (isPlayerOp(sender))
            return super.addTabCompletionOptions(sender, args);
        else
            return null;
    }

    @Override
    public boolean isVisible(ICommandSender sender) {
        return true;
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender) {
        return isPlayerOp(sender) ? new int[]{0, 1, 2, 3} : super.getSyntaxOptions(sender);
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) throws CommandException {
        if (!QuestingData.isHardcoreActive()) {
            sendChat(sender, "hqm.message.noHardcoreYet");
            return;
        }
        int amount;

        if (arguments.length == 0 && sender instanceof EntityPlayer)
            currentLives((EntityPlayer) sender);

        if (!isPlayerOp(sender))
            throw new CommandException(CommandStrings.NO_PERMISSION);

        if (arguments.length == 1) {
            if (arguments[0].matches(REMOVE)) {
                if (sender instanceof EntityPlayer)
                    removeLives((EntityPlayer) sender, 1);
            } else if (arguments[0].matches(ADD)) {
                if (sender instanceof EntityPlayer)
                    addLives((EntityPlayer) sender, 1);
            } else {
                getPlayerLives(sender, arguments[0]);
            }
        } else if (arguments.length == 2) {
            //remove amount own.
            if (arguments[0].matches(REMOVE)) {
                try {
                    amount = Integer.parseInt(arguments[1]);
                    if (amount < 0)
                        throw new WrongUsageException("hqm.message.positiveNumbers");
                    if (sender instanceof EntityPlayer)
                        removeLives((EntityPlayer) sender, amount);
                } catch (Exception e) {
                    throw new WrongUsageException("hqm.message.posNumberAndPlayer");
                }
            }
            // remove playername
            else if (arguments[1].matches(REMOVE)) {
                try {
                    removeLivesFrom(sender, arguments[1], 1);
                } catch (Exception e) {
                    throw new WrongUsageException("hqm.message.posNumberAndPlayer");
                }
            }
            //add amount own
            else if (arguments[0].matches(ADD)) {
                try {
                    amount = Integer.parseInt(arguments[1]);
                    if (amount < 0)
                        throw new WrongUsageException("hqm.message.positiveNumbers");
                    if (sender instanceof EntityPlayer)
                        addLives((EntityPlayer) sender, amount);
                } catch (Exception e) {
                    throw new WrongUsageException("hqm.message.positiveNumbers");
                }
            }
            //add amount playername
            else if (arguments[0].matches(ADD)) {
                try {
                    addLivesTo(sender, arguments[1], 1);
                } catch (Exception e) {
                    throw new WrongUsageException("hqm.message.posNumberAndPlayer");
                }
            }
        } else if (arguments.length == 3) {
            // /hqm lives remove playername amount
            if (arguments[0].matches(REMOVE)) {
                try {
                    amount = Integer.parseInt(arguments[2]);
                    if (amount < 0)
                        throw new WrongUsageException("hqm.message.positiveNumbers");
                    removeLivesFrom(sender, arguments[1], amount);
                } catch (Exception e) {
                    throw new WrongUsageException("hqm.message.posNumberAndPlayer");
                }
            }
            // /hqm lives add playername amount
            else if (arguments[0].matches(ADD)) {
                try {
                    amount = Integer.parseInt(arguments[2]);
                    if (amount < 0)
                        throw new WrongUsageException("hqm.message.positiveNumbers");
                    addLivesTo(sender, arguments[1], amount);
                } catch (Exception e) {
                    throw new WrongUsageException("hqm.message.posNumberAndPlayer");
                }
            }
        }
    }

    private void removeLives(EntityPlayer player, int amount) {
        if (QuestingData.getQuestingData(player).getLives() - amount < QuestingData.getQuestingData(player).getLivesToStayAlive()) {
            int lives = QuestingData.getQuestingData(player).getLives();
            sendChat(player, lives != 1, "hqm.message.cantRemoveLives", lives);
        } else {
            QuestingData.getQuestingData(player).removeLives(player, amount);
            sendChat(player, amount != 1, "hqm.message.removeLives", amount);
            currentLives(player);
        }
    }

    private void removeLivesFrom(ICommandSender sender, String playerName, int amount) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);

        QuestingData.getQuestingData(player).removeLives((EntityPlayerMP) sender, amount);

        sendChat(sender, amount != 1, "hqm.message.removeLivesFrom", amount, playerName);
        sendChat(player, amount != 1, "hqm.message.removeLivesBy", amount, sender.getName());
        currentLives(player);
    }

    private void addLives(EntityPlayer player, int amount) {
        if (QuestingData.getQuestingData(player).getRawLives() + amount <= HQMConfig.Hardcore.MAX_LIVES) {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(player, amount != 1, "hqm.message.addLives", amount);
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(player, HQMConfig.Hardcore.MAX_LIVES != 1, "hqm.message.cantAddLives", HQMConfig.Hardcore.MAX_LIVES);
            currentLives(player);
        }
    }

    private void addLivesTo(ICommandSender sender, String playerName, int amount) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);

        if (QuestingData.getQuestingData(player).getRawLives() + amount <= HQMConfig.Hardcore.MAX_LIVES) {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(sender, amount != 1, "hqm.message.addLivesTo", amount, playerName);
            sendChat(player, amount != 1, "hqm.message.addLivesBy", amount, sender.getName());
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(sender, "hqm.message.cantGiveMoreLives", playerName, HQMConfig.Hardcore.MAX_LIVES);
            sendChat(sender, "hqm.massage.setLivesInstead", player, HQMConfig.Hardcore.MAX_LIVES);
            sendChat(player, "hqm.massage.setLivesBy", HQMConfig.Hardcore.MAX_LIVES, sender.getName());
            currentLives(player);
        }
    }

    private void getPlayerLives(ICommandSender sender, String playerName) throws CommandException {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
        if (player != null) {
            int lives = QuestingData.getQuestingData(player).getLives();
            sendChat(sender, lives != 1, "hqm.message.hasLivesRemaining", playerName, lives);
        } else {
            throw new CommandException("hqm.message.noPlayer");
        }
    }

}
