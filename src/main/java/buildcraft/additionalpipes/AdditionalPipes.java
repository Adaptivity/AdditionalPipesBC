package buildcraft.additionalpipes;

import java.io.File;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.additionalpipes.chunkloader.BlockChunkLoader;
import buildcraft.additionalpipes.chunkloader.ChunkLoadingHandler;
import buildcraft.additionalpipes.chunkloader.TileChunkLoader;
import buildcraft.additionalpipes.gates.GateProvider;
import buildcraft.additionalpipes.gates.TriggerPipeClosed;
import buildcraft.additionalpipes.gui.GuiHandler;
import buildcraft.additionalpipes.network.PacketHandler;
import buildcraft.additionalpipes.pipes.APPipe;
import buildcraft.additionalpipes.pipes.PipeItemsAddition;
import buildcraft.additionalpipes.pipes.PipeItemsAdvancedInsertion;
import buildcraft.additionalpipes.pipes.PipeItemsAdvancedWood;
import buildcraft.additionalpipes.pipes.PipeItemsClosed;
import buildcraft.additionalpipes.pipes.PipeItemsDistributor;
import buildcraft.additionalpipes.pipes.PipeItemsPriorityInsertion;
import buildcraft.additionalpipes.pipes.PipeItemsTeleport;
import buildcraft.additionalpipes.pipes.PipeLiquidsTeleport;
import buildcraft.additionalpipes.pipes.PipeLiquidsWaterPump;
import buildcraft.additionalpipes.pipes.PipePowerTeleport;
import buildcraft.additionalpipes.pipes.PipeSwitchFluids;
import buildcraft.additionalpipes.pipes.PipeSwitchItems;
import buildcraft.additionalpipes.pipes.PipeSwitchPower;
import buildcraft.additionalpipes.pipes.TeleportManager;
import buildcraft.additionalpipes.textures.Textures;
import buildcraft.additionalpipes.utils.Log;
import buildcraft.additionalpipes.utils.PipeCreator;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.transport.BlockGenericPipe;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = AdditionalPipes.MODID, name = AdditionalPipes.NAME, dependencies = "after:BuildCraft|Transport;after:BuildCraft|Silicon;after:BuildCraft|Transport;after:BuildCraft|Factory", version = AdditionalPipes.VERSION)
public class AdditionalPipes {
	public static final String MODID = "additionalpipes";
	public static final String NAME = "Additional Pipes";
	public static final String VERSION = "4.3.0";

	@Instance(MODID)
	public static AdditionalPipes instance;

	@SidedProxy(clientSide = "buildcraft.additionalpipes.MutiPlayerProxyClient", serverSide = "buildcraft.additionalpipes.MultiPlayerProxy")
	public static MultiPlayerProxy proxy;

	public File configFile;

	// chunk load boundaries
	public ChunkLoadViewDataProxy chunkLoadViewer;


	// teleport scanner TODO
	// public Item teleportScanner;

	// Redstone Liquid
	public Item pipeLiquidsRedstone;
	// Redstone
	public Item pipeItemsRedStone;
	// Advanced Insertion
	public Item pipeItemsAdvancedInsertion;
	// Advanced Insertion
	public Item pipeItemsAddition;
	// Advanced Wood
	public Item pipeItemsAdvancedWood;
	// Distributor
	public Item pipeItemsDistributor;
	// Jeweled
	public Item pipeItemsJeweled;

	
	//priority insertion pipe
	public Item pipeItemsPriority;
	// Item Teleport
	public Item pipeItemsTeleport;
	// Liquid Teleport
	public Item pipeLiquidsTeleport;
	// Power Teleport
	public Item pipePowerTeleport;
	// Items Closed
	public Item pipeItemsClosed;
	// Switch pipes
	public Item pipePowerSwitch;
	public Item pipeItemsSwitch;
	public Item pipeLiquidsSwitch;
	// water pump pipe
	public Item pipeLiquidsWaterPump;
	// chunk loader
	public Block blockChunkLoader;
	
	public ITriggerInternal triggerPipeClosed;

	public ITriggerInternal triggerPhasedSignalRed;
	public ITriggerInternal triggerPhasedSignalBlue;
	public ITriggerInternal triggerPhasedSignalGreen;
	public ITriggerInternal triggerPhasedSignalYellow;
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		
		PacketHandler.init();

		configFile = event.getSuggestedConfigurationFile();
		APConfiguration.loadConfigs(false, configFile);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
	
		Log.info("Registering chunk load handler");
		ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkLoadingHandler());
		chunkLoadViewer = new ChunkLoadViewDataProxy(APConfiguration.chunkSightRange);
		FMLCommonHandler.instance().bus().register(chunkLoadViewer);
		
		proxy.registerKeyHandler();
		
		proxy.registerRendering();

		APConfiguration.loadConfigs(true, configFile);
		
		Log.info("Registering pipes");
		loadPipes();

		triggerPipeClosed = new TriggerPipeClosed();
		StatementManager.registerTriggerProvider(new GateProvider());

		if(APConfiguration.allowWRRemove)
		{
			// Additional Pipes
			GameRegistry.addShapelessRecipe(new ItemStack(pipeItemsTeleport), new Object[] {pipePowerTeleport});
			GameRegistry.addShapelessRecipe(new ItemStack(pipeItemsTeleport), new Object[] {pipeLiquidsTeleport});
			
			GameRegistry.addShapelessRecipe(new ItemStack(pipeItemsSwitch), new Object[] {pipeLiquidsSwitch});
			GameRegistry.addShapelessRecipe(new ItemStack(pipeItemsSwitch), new Object[] {pipePowerSwitch});
			
			//it looks like Buildcraft might be adding these itself.  I need to look into removing this. -JS
			
			// BC Liquid
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsCobblestone), new Object[] { BuildCraftTransport.pipeFluidsCobblestone });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsGold), new Object[] { BuildCraftTransport.pipeFluidsGold });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsIron), new Object[] { BuildCraftTransport.pipeFluidsIron });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsStone), new Object[] { BuildCraftTransport.pipeFluidsStone });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsWood), new Object[] { BuildCraftTransport.pipeFluidsWood });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsDiamond), new Object[] { BuildCraftTransport.pipeFluidsDiamond });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsEmerald), new Object[] { BuildCraftTransport.pipeFluidsEmerald });
			
			// BC Power
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsGold), new Object[] { BuildCraftTransport.pipePowerGold });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsStone), new Object[] { BuildCraftTransport.pipePowerStone });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsWood), new Object[] { BuildCraftTransport.pipePowerWood });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsCobblestone), new Object[] { BuildCraftTransport.pipePowerCobblestone });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsEmerald), new Object[] { BuildCraftTransport.pipePowerEmerald });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsDiamond), new Object[] { BuildCraftTransport.pipePowerDiamond });
			GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeItemsQuartz), new Object[] { BuildCraftTransport.pipePowerQuartz });

		}

		// ChunkLoader
		blockChunkLoader = new BlockChunkLoader();
		blockChunkLoader.setBlockName("TeleportTether");
		GameRegistry.registerBlock(blockChunkLoader, ItemBlock.class, "chunkLoader");
		GameRegistry.registerTileEntity(TileChunkLoader.class, "TeleportTether");
		GameRegistry.addRecipe(new ItemStack(blockChunkLoader), new Object[] { "iii", "iLi", "iii", 'i', Items.iron_ingot, 'L', new ItemStack(Items.dye, 1, 4) });
	}

	@EventHandler
	public void onServerStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandAdditionalPipes());
		TeleportManager.instance.reset();
	}

	
	private void loadPipes() {
		// Item Teleport Pipe
		pipeItemsTeleport = PipeCreator.createPipeSpecial((Class<? extends APPipe<?>>) PipeItemsTeleport.class);
		
		GameRegistry.addRecipe(new ItemStack(pipeItemsTeleport, 4), new Object[] { "dgd", 'd', BuildCraftCore.diamondGearItem, 'g', Blocks.glass });
		AssemblyRecipeManager.INSTANCE.addRecipe("teleportPipe", 10000, new ItemStack(pipeItemsTeleport, 8), new Object[] { new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 4), new ItemStack(BuildCraftTransport.pipeItemsDiamond, 8),
				new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3) });


		// Liquid Teleport Pipe
		pipeLiquidsTeleport = PipeCreator.createPipeSpecial((Class<? extends APPipe<?>>) PipeLiquidsTeleport.class);
		if(pipeItemsTeleport != null) {
			GameRegistry.addShapelessRecipe(new ItemStack(pipeLiquidsTeleport), new Object[] {BuildCraftTransport.pipeWaterproof, pipeItemsTeleport});
		}

		// Power Teleport Pipe
		
		pipePowerTeleport = PipeCreator.createPipeSpecial((Class<? extends APPipe<?>>) PipePowerTeleport.class);
		if(pipeItemsTeleport != null) {
			GameRegistry.addShapelessRecipe(new ItemStack(pipePowerTeleport), new Object[] {Items.redstone, pipeItemsTeleport});
		}

		//Jeweled Pipe
		//disabled since I can't get the GUI to work
		//pipeItemsJeweled = doCreatePipeAndRecipe(PipeItemsJeweled.class, new Object[] { " D ", "DGD", " D ", 'D', BuildCraftTransport.pipeItemsDiamond, 'G', BuildCraftCore.goldGearItem});
		
		// Distributor Pipe
		pipeItemsDistributor = PipeCreator.createPipeAndRecipe(1, PipeItemsDistributor.class, new Object[] { " r ", "IgI", 'r', Items.redstone, 'I', Items.iron_ingot, 'g', Blocks.glass }, false);

		// Advanced Insertion Pipe
		pipeItemsAdvancedInsertion = PipeCreator.createPipeAndRecipe(8, PipeItemsAdvancedInsertion.class,
				new Object[] { "IgI", 'I', BuildCraftCore.ironGearItem, 'g', Blocks.glass }, false);
		
		// Advanced Insertion Pipe
		pipeItemsAddition = PipeCreator.createPipeAndRecipe(1, PipeItemsAddition.class,
				new Object[] { " R ", "RIR", " R ", 'I', pipeItemsAdvancedInsertion, 'R', Items.redstone}, false);
		
		pipeItemsPriority = PipeCreator.createPipeAndRecipe(2, PipeItemsPriorityInsertion.class, new Object[] {pipeItemsDistributor, pipeItemsAdvancedInsertion}, true);
		
		// Advanced Wooded Pipe
		pipeItemsAdvancedWood = PipeCreator.createPipeAndRecipe(8, PipeItemsAdvancedWood.class, new Object[] { "WgW", 'W', BuildCraftCore.woodenGearItem, 'g', Blocks.glass }, false);

		// Closed Items Pipe
		pipeItemsClosed = PipeCreator.createPipeAndRecipe(1, PipeItemsClosed.class, new Object[] {BuildCraftTransport.pipeItemsVoid, BuildCraftCore.ironGearItem}, true);
		// switch pipes
		pipeItemsSwitch = PipeCreator.createPipeAndRecipe(8, PipeSwitchItems.class, new Object[] { "GgI", 'g', Blocks.glass, 'G', BuildCraftCore.goldGearItem, 'I', BuildCraftCore.ironGearItem}, false);
		pipePowerSwitch = PipeCreator.createPipeAndRecipe(1, PipeSwitchPower.class, new Object[] {pipeItemsSwitch, Items.redstone }, true);
		pipeLiquidsSwitch = PipeCreator.createPipeAndRecipe(1, PipeSwitchFluids.class, new Object[] {pipeItemsSwitch, BuildCraftTransport.pipeWaterproof }, true);

		// water pump pipe
		pipeLiquidsWaterPump = PipeCreator.createPipeAndRecipe(1, PipeLiquidsWaterPump.class, new Object[] { " L ", "rPr", " W ", 'r', Items.redstone, 'P', BuildCraftCore.ironGearItem, 'L',
				BuildCraftTransport.pipeFluidsGold, 'w', BuildCraftTransport.pipeWaterproof, 'W', BuildCraftTransport.pipeFluidsWood }, false);
	}

	// legacy method
	public static boolean isPipe(Item item)
	{
		if(item != null && BlockGenericPipe.pipes.containsKey(item))
		{
			return true;
		}
		return false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) throws IOException 
	{
		Textures.registerIcons(event.map, event.map.getTextureType());
	}
}
