package com.ishland.raknetfabric.common.connection;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import java.util.HashSet;
import java.util.Set;

public class RaknetMultiChannel {

    private RaknetMultiChannel() {
    }

    private static Set<Class<?>> createClassSet(String[] classNames) {
        final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        final boolean isIntermediaryNamespace = mappingResolver.getCurrentRuntimeNamespace().equals("intermediary");
        final HashSet<Class<?>> classHashSet = new HashSet<>();
        for (String className : classNames) {
            final String intermediary = mappingResolver.mapClassName("intermediary", className.replace('/', '.'));
            if (!isIntermediaryNamespace && intermediary.equals(className))
                //noinspection RedundantStringFormatCall
                System.err.println("Warning: Failed to remap %s for raknet multi-channel".formatted(intermediary));
            try {
                classHashSet.add(Class.forName(intermediary));
            } catch (ClassNotFoundException e) {
                //noinspection RedundantStringFormatCall
                System.out.println("Warning: %s not found for raknet multi-channel".formatted(intermediary));
            }
        }
        return classHashSet; // no read-only wrapper for performance
    }

    // Primarily used for interactions
    private static final Set<Class<?>> channel1 = createClassSet(new String[]{
            "net/minecraft/class_2629", // BossBarS2C
            "net/minecraft/class_5888", // ClearTitleS2C
            "net/minecraft/class_5903", // SubtitleS2C
            "net/minecraft/class_5904", // TitleS2C
            "net/minecraft/class_5905", // TitleFadeS2C
            "net/minecraft/class_5892", // DeathMessageS2C
            "net/minecraft/class_2635", // GameMessageS2CPacket
            "net/minecraft/class_5894", // OverlayMessageS2C
            "net/minecraft/class_2748", // ExperienceBarUpdateS2C
            "net/minecraft/class_2749", // HealthUpdateS2C
            "net/minecraft/class_2772", // PlayerListHeaderS2C
            "net/minecraft/class_2703", // PlayerListS2C
            "net/minecraft/class_2736", // ScoreboardDisplayS2C
            "net/minecraft/class_2751", // ScoreboardDisplayObjectiveUpdateS2C
            "net/minecraft/class_2757", // ScoreboardPlayerUpdateS2C
            "net/minecraft/class_2641", // CommandTreeS2C
            "net/minecraft/class_2639", // CommandSuggestionsS2C

            "net/minecraft/class_2805", // RequestCommandCompletionsC2S
            "net/minecraft/class_2811", // ButtonClickC2S
            "net/minecraft/class_2797", // ChatMessageC2S
            "net/minecraft/class_2813", // ClickSlotC2S
            "net/minecraft/class_2848", // ClientCommandC2S
            "net/minecraft/class_2803", // ClientSettingsC2S
            "net/minecraft/class_2803", // ClientStatusC2S
            "net/minecraft/class_4210", // UpdateDifficultyC2S
            "net/minecraft/class_4211", // UpdateDifficultyLockC2S
            "net/minecraft/class_4463", // PlayerActionResponseS2C
            "net/minecraft/class_2708", // PlayerPositionLookS2C

            "net/minecraft/class_2846", // PlayerActionC2S
            "net/minecraft/class_2851", // PlayerInputC2S
            "net/minecraft/class_2885", // PlayerInteractBlockC2S
            "net/minecraft/class_2886", // PlayerInteractItemC2S
            "net/minecraft/class_2824", // PlayerInteractEntityC2S
            "net/minecraft/class_2828", // PlayerMoveC2S
            "net/minecraft/class_2795", // QueryBlockNbtC2S
            "net/minecraft/class_2822", // QueryEntityNbtC2S
            "net/minecraft/class_2884", // SpectatorTeleportC2S
            "net/minecraft/class_2793", // TeleportConfirmC2S

            "net/minecraft/class_2774", // NbtQueryResponseS2C

            "net/minecraft/class_2645", // CloseScreenS2C
            "net/minecraft/class_2648", // OpenHorseScreenS2C
            "net/minecraft/class_3944", // OpenScreenS2C
            "net/minecraft/class_2651", // ScreenHandlerPropertyUpdateS2C
            "net/minecraft/class_2653", // ScreenHandlerSlotUpdateS2C
            "net/minecraft/class_3895", // OpenWrittenBookS2C
            "net/minecraft/class_2649", // InventoryS2C
            "net/minecraft/class_2713", // UnlockRecipesS2C
            "net/minecraft/class_2735", // UpdateSelectedSlotS2C
            "net/minecraft/class_3943", // SetTradeOffersS2C

            "net/minecraft/class_2853", // RecipeBookDataC2S
            "net/minecraft/class_5427", // RecipeCategoryOptionsC2S
            "net/minecraft/class_2855", // RenameItemC2S
            "net/minecraft/class_2815", // CloseHandledScreenC2S
            "net/minecraft/class_2873", // CreativeInventoryActionC2S
            "net/minecraft/class_2840", // CraftRequestC2S
            "net/minecraft/class_2838", // PickFromInventoryC2S
            "net/minecraft/class_2863", // SelectMerchantTradeC2S
            "net/minecraft/class_2866", // UpdateBeaconC2S
            "net/minecraft/class_2870", // UpdateCommandBlockC2S
            "net/minecraft/class_2871", // UpdateCommandBlockMinecartC2S
            "net/minecraft/class_3753", // UpdateJigsawC2S
            "net/minecraft/class_2842", // UpdatePlayerAbilitiesC2S
            "net/minecraft/class_2868", // UpdateSelectedSlotC2S
//            "net/minecraft/class_2693", // SignEditorOpenS2C
//            "net/minecraft/class_2877", // UpdateSignC2S
            "net/minecraft/class_2875", // UpdateStructureBlockC2S

            "net/minecraft/class_2707", // LookAtS2C

            "net/minecraft/class_2761", // WorldTimeUpdateS2C
            "net/minecraft/class_2632", // DifficultyS2C
            "net/minecraft/class_5900", // TeamS2C

            "net/minecraft/class_2788", // SynchronizeRecipesS2C
            "net/minecraft/class_2790", // SynchronizeTagsS2C
            "net/minecraft/class_6682", // SimulationDistanceS2C

            "net/minecraft/class_5889", // WorldBorderInitializeS2C
            "net/minecraft/class_5895", // WorldBorderCenterChangedS2C
            "net/minecraft/class_5896", // WorldBorderInterpolateSizeS2C
            "net/minecraft/class_5897", // WorldBorderSizeChangedS2C
            "net/minecraft/class_5898", // WorldBorderWarningTimeChangedS2C
            "net/minecraft/class_5899", // WorldBorderWarningBlockChangedS2C
    });

    // Primarily for packets not very critical to interactions
    private static final Set<Class<?>> channel2 = createClassSet(new String[]{
            "net/minecraft/class_2683", // MapUpdateS2C
            "net/minecraft/class_2675", // ParticleS2C
            "net/minecraft/class_2660", // PlaySoundIdS2C
            "net/minecraft/class_2765", // PlaySoundFromEntityS2C
            "net/minecraft/class_2767", // PlaySoundS2C
            "net/minecraft/class_2770", // StopSoundS2C
    });

    // Entities related stuff
    private static final Set<Class<?>> channel3 = createClassSet(new String[]{
            "net/minecraft/class_5890", // EndCombatS2C
            "net/minecraft/class_5891", // EnterCombatS2C
            "net/minecraft/class_2716", // EntitiesDestroyS2C
            "net/minecraft/class_2616", // EntityAnimationS2C
            "net/minecraft/class_2663", // EntityStatusS2C
            "net/minecraft/class_2684$class_2685", // EntityPacketS2C$MoveRelative
            "net/minecraft/class_2684$class_2687", // EntityPacketS2C$Rotate
            "net/minecraft/class_2684$class_2686", // EntityPacketS2C$RotateAndMoveRelative
            "net/minecraft/class_2726", // EntitySetHeadYawS2C
            "net/minecraft/class_2739", // EntityTrackerUpdateS2C
            "net/minecraft/class_2740", // EntityAttachS2C
            "net/minecraft/class_2743", // EntityVelocityUpdateS2C
            "net/minecraft/class_2744", // EntityEquipmentUpdateS2C
            "net/minecraft/class_2752", // EntityPassengerSetS2C
            "net/minecraft/class_2777", // EntityPositionS2C
            "net/minecraft/class_2781", // EntityAttributesS2C
            "net/minecraft/class_2783", // EntityStatusEffectS2C
            "net/minecraft/class_2718", // RemoveEntityStatusEffectS2C
            "net/minecraft/class_2610", // MobSpawnS2C
            "net/minecraft/class_2612", // PaintingSpawnS2C

            "net/minecraft/class_2664", // ExplosionS2C
            "net/minecraft/class_2678", // GameJoinS2C
            "net/minecraft/class_2668", // GameStateChangeS2C
            "net/minecraft/class_2775", // ItemPickupAnimationS2C
            "net/minecraft/class_2696", // PlayerAbilitiesS2C
            "net/minecraft/class_2734", // SetCameraEntityS2C
            "net/minecraft/class_2692", // VehicleMoveS2C

            "net/minecraft/class_2833", // VehicleMoveC2S
    });

    private static final Set<Class<?>> unordered = createClassSet(new String[]{
            "net/minecraft/class_2670", // KeepAliveS2C
            "net/minecraft/class_2827", // KeepAliveC2S
            "net/minecraft/class_2661", // DisconnectS2C
            "net/minecraft/class_6373", // PlayPingS2C
            "net/minecraft/class_6374", // PlayPongC2S

            "net/minecraft/class_2720", // ResourcePackSendS2C
            "net/minecraft/class_2856", // ResourcePackStatusC2S

            "net/minecraft/class_2617", // StatisticsS2C
            "net/minecraft/class_2859", // AdvancementTabC2S
            "net/minecraft/class_2729", // SelectAdvancementTabS2C
    });

    private static final Object2IntOpenHashMap<Class<?>> classToChannelIdOverride = new Object2IntOpenHashMap<>();

    static {
        channel1.forEach(clazz -> classToChannelIdOverride.put(clazz, 1));
        channel2.forEach(clazz -> classToChannelIdOverride.put(clazz, 2));
//        channel3.forEach(clazz -> classToChannelIdOverride.put(clazz, 3)); TODO entity sync
        unordered.forEach(clazz -> classToChannelIdOverride.put(clazz, -1));
    }



}
