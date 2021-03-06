package hardcorequesting;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.event.PlayerDeathEventListener;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.event.WorldEventListener;
import hardcorequesting.integration.IntegrationHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = HardcoreQuesting.ID, name = HardcoreQuesting.NAME, version = HardcoreQuesting.VERSION)
public class HardcoreQuesting {

    public static final String ID = "hardcorequesting";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "hcQuesting";
    public static final String CONFIG_LOC_NAME = "hqm";
    public static final String SOUNDLOC = "hardcorequesting";

    @Instance(ID)
    public static HardcoreQuesting instance;

    @SidedProxy(clientSide = "hardcorequesting.proxies.ClientProxy", serverSide = "hardcorequesting.proxies.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs HQMTab = new HQMTab();

    public static String path;

    public static File configDir;

    public static Side loadingSide;

    public static final String NAME = "Hardcore Questing Mode";
    public static final Logger LOG = LogManager.getFormatterLogger(NAME);

    private static EntityPlayer commandUser;

    public static EntityPlayer getPlayer() {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player) {
        commandUser = player;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        loadingSide = event.getSide();
        new EventTrigger();

        path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + CONFIG_LOC_NAME.toLowerCase() + File.separator;
        configDir = new File(path);
        QuestLine.init(path);

        HQMConfig.loadConfig();

        proxy.init();
        proxy.initSounds(path);

        ModBlocks.init();
        ModBlocks.registerTileEntities();

        ModItems.init();
        
        MinecraftForge.EVENT_BUS.register(instance);
    
        IntegrationHandler.preInit(event, this);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();

        NetworkManager.init();
        proxy.initRenderers();

        ModItems.registerRecipes();
        ModBlocks.registerRecipes();
    
        IntegrationHandler.init(event, this);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        IntegrationHandler.postInit(event, this);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(CommandHandler.instance);
    }
    
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegisterHelper.registerBlocks(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegisterHelper.registerItems(event);
    }
    
    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        Sounds.registerSounds(event);
    }
}
