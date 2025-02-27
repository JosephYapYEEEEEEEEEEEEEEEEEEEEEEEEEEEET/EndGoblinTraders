package com.jab125.egt;

import com.jab125.egt.config.EndGoblinTradersConfig;
import com.jab125.egt.datagen.DataGeneration;
import com.jab125.egt.init.*;
import com.jab125.limeappleboat.gobt.api.GobTEvents;
import com.jab125.util.tradehelper.TradeManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.hat.gt.spawning.GoblinTraderSpawner;
import net.hat.gt.spawning.SpawnHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Objects;

public class EGobT implements ModInitializer {
	public static final String MODID = "endgoblintraders";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static EndGoblinTradersConfig config;
	private static final boolean doDataGen = false;

	@Override
	public void onInitialize() {
		TradeManager.instance().registerTrader(ModEntities.END_GOBLIN_TRADER);
		AutoConfig.register(EndGoblinTradersConfig.class, Toml4jConfigSerializer::new);
		config = AutoConfig.getConfigHolder(EndGoblinTradersConfig.class).getConfig();
		ModEntities.registerEntities();
		ModBlocks.registerBlocks();
		ModItems.registerItems();
		ModOres.registerOres();
		ModSounds.registerSounds();
		ModEnchantments.registerEnchantments();
		final LambdaFix<Boolean> loaded = new LambdaFix<>(false);
		ServerWorldEvents.LOAD.register((minecraftServer, world) -> {
			if (loaded.get()) return;
			SpawnHandler.addToSpawners(DimensionType.THE_NETHER_REGISTRY_KEY.getValue(), new GoblinTraderSpawner(minecraftServer, "EndGoblinTraderHell", ModEntities.END_GOBLIN_TRADER, Objects.requireNonNull(ModEntities.END_GOBLIN_TRADER.create(world))));
			SpawnHandler.addToSpawners(DimensionType.THE_END_REGISTRY_KEY.getValue(), new GoblinTraderSpawner(minecraftServer, "EndGoblinTraderSky", ModEntities.END_GOBLIN_TRADER, Objects.requireNonNull(ModEntities.END_GOBLIN_TRADER.create(world))));
			SpawnHandler.addToSpawners(DimensionType.OVERWORLD_REGISTRY_KEY.getValue(), new GoblinTraderSpawner(minecraftServer, "EndGoblinTraderEarth", ModEntities.END_GOBLIN_TRADER, Objects.requireNonNull(ModEntities.END_GOBLIN_TRADER.create(world))));
			SpawnHandler.getSpawners().forEach((identifier, goblinTraderSpawner) -> {
				System.out.println(identifier.toString() + ": " + goblinTraderSpawner);
			});
			loaded.set(true);
		});

		GobTEvents.ON_ATTEMPT_SPAWN.register(((entityType, serverWorld, blockPos) -> {
			if (entityType.equals(ModEntities.END_GOBLIN_TRADER) && serverWorld.getDimension().equals(DimensionType.OVERWORLD)) {
				/** End Goblin Traders can spawn up to y=255, this is just a test to make sure they don't spawn in broad daylight */
				return serverWorld.getLightLevel(blockPos) < 5 && !serverWorld.isSkyVisible(blockPos) ? ActionResult.PASS : ActionResult.FAIL;
			}
			/**
			 * Prevents End Goblin Traders from spawning the central end island
			 */
			if (entityType.equals(ModEntities.END_GOBLIN_TRADER) && serverWorld.getDimension().equals(DimensionType.THE_END)) {
				return blockPos.isWithinDistance(new BlockPos(0, 90, 0), 750) ? ActionResult.FAIL : ActionResult.PASS;
			}
			return ActionResult.PASS;
		}));
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		//LOGGER.info("Hello Fabric world!");

		if (EGobT.doDataGen) {
		    DataGenerator dataGenerator = new DataGenerator(new File("../src/main/generated/resources").toPath(), null);
		    DataGeneration.registerCommonProviders(dataGenerator);
		}

		//ResourceManagerHelper.registerBuiltinResourcePack(id("gobtvanillaish"), Objects.requireNonNull(FabricLoader.getInstance().getModContainer(MODID)).get(), ResourcePackActivationType.NORMAL);
	}

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static Identifier endGoblinTexture() {

		if (EGobT.config.END_GOBLIN_TRADER_CONFIG.END_GOBLIN_TRADER_ALT_TEXTURE) {
			return EGobT.id("textures/entity/endgoblintrader/ragna_goblin.png");
		} else {
			return EGobT.id("textures/entity/endgoblintrader/end_goblin_trader.png");
		}
	}
}
