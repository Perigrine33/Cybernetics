package com.perigrine3.createcybernetics.client.skin;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.cybereyes.CybereyeVisualConfig;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.compat.curios.CuriosCompat;
import com.perigrine3.createcybernetics.event.custom.FullBorgHandler;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.cyberware.lungs.VampyresItem;
import com.perigrine3.createcybernetics.tattoo.TattooLayer;
import com.perigrine3.createcybernetics.tattoo.client.ClientTattooModifierCollector;
import com.perigrine3.createcybernetics.util.ModTags;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Central manager for all skin modifications in the game.
 * This class determines which skin modifiers should be active for each player.
 *
 * How to Add New Skin Modifications:
 * 1. Define your texture constant:
 *    private static final ResourceLocation YOUR_SKIN_TEXTURE = 
 *        new ResourceLocation(CreateCybernetics.MODID, "textures/entity/your_texture.png");
 *
 * 2. Create a SkinModifier instance:
 *    private static final SkinModifier YOUR_MODIFIER = new SkinModifier(
 *        YOUR_SKIN_TEXTURE,
 *        FastColor.ARGB32.color(255, r, g, b), // Color tint (optional)
 *        hideVanillaLayers // true/false
 *    );
 *
 * 3. Add your condition in getPlayerSkinState:
 *    // For cyberware-based modifications:
 *    if (data.hasAnyTagged(ModTags.Items.YOUR_ITEMS, CyberwareSlot.YOUR_SLOT)) {
 *        state.addModifier(YOUR_MODIFIER);
 *    }
 *
 *    // For effect-based modifications:
 *    if (player.hasEffect(ModEffects.YOUR_EFFECT)) {
 *        state.addModifier(YOUR_MODIFIER);
 *    }
 *
 *    // For conditional modifications:
 *    if (shouldApplyCondition(player)) {
 *        state.addModifier(YOUR_MODIFIER);
 *    }
 *
 * Example Implementations:
 * 1. Metallic Skin Overlay:
 *    private static final ResourceLocation METALLIC_SKIN = 
 *        new ResourceLocation(CreateCybernetics.MODID, "textures/entity/metallic_skin.png");
 *    private static final SkinModifier METALLIC_MODIFIER = new SkinModifier(
 *        METALLIC_SKIN,
 *        FastColor.ARGB32.color(255, 192, 192, 192),
 *        false
 *    );
 *
 * 2. Glowing Circuits:
 *    private static final ResourceLocation CIRCUIT_PATTERN = 
 *        new ResourceLocation(CreateCybernetics.MODID, "textures/entity/circuit_pattern.png");
 *    private static final SkinModifier CIRCUIT_MODIFIER = new SkinModifier(
 *        CIRCUIT_PATTERN,
 *        FastColor.ARGB32.color(255, 0, 255, 255),
 *        false
 *    );
 *
 * Important Notes:
 * - Modifiers are rendered in the order they're added to the state
 * - Later modifiers will render on top of earlier ones
 * - Use alpha channels in textures for partial transparency
 * - Color tints can be used to create variations of the same texture
 * - Consider performance when adding multiple modifiers
 */
public class SkinModifierManager {
    private static final Map<UUID, SkinModifierState> PLAYER_STATES = new HashMap<>();

    private static final String ENTITY_HOLO_SNAPSHOT_KEY = "cc_holo_snapshot";

    private static boolean SNAP_REFLECT_READY = false;
    private static boolean SNAP_REFLECT_FAILED = false;
    private static java.lang.reflect.Constructor<?> SNAP_CTOR;
    private static java.lang.reflect.Method SNAP_DESERIALIZE_1;
    private static java.lang.reflect.Method SNAP_DESERIALIZE_2;



    // ---- MERMOD COMPONENT-BASED MODIFIER DETECTION (soft compat) ----
    private static final ResourceLocation MERMOD_SEA_NECKLACE_ID =
            ResourceLocation.fromNamespaceAndPath("mermod", "sea_necklace");

    private static DataComponentType<?> MERMOD_NECKLACE_MODIFIERS_COMPONENT;
    private static boolean MERMOD_REFLECTION_READY = false;
    private static boolean MERMOD_REFLECTION_FAILED = false;
    private static Method MERMOD_COMPONENT_MODIFIERS_METHOD;
    private static Method MERMOD_MODIFIER_ID_METHOD;




    //INTERCHANGEABLES
    private static final ResourceLocation MISSING_SKIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/playermuscles_wide.png");
    private static final ResourceLocation CYBEREYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/cybereyes_dyed.png");
    private static final ResourceLocation CYBEREYES_PRIMARY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/cybereyes_dye_primary.png");
    private static final ResourceLocation CYBEREYES_SECONDARY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/cybereyes_dye_secondary.png");
    private static final ResourceLocation RIGHT_CYBERLEG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberleg.png");
    private static final ResourceLocation COPPER_PLATED_RIGHT_CYBERLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copper_rightleg.png");
    private static final ResourceLocation IRON_PLATED_RIGHT_CYBERLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/iron_rightleg.png");
    private static final ResourceLocation GOLD_PLATED_RIGHT_CYBERLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gold_rightleg.png");
    private static final ResourceLocation RIGHT_CYBERLEG_PRIMARY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberleg_dye_primary.png");
    private static final ResourceLocation RIGHT_CYBERLEG_SECONDARY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberleg_dye_secondary.png");
    private static final ResourceLocation LEFT_CYBERLEG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberleg.png");
    private static final ResourceLocation COPPER_PLATED_LEFT_CYBERLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copper_leftleg.png");
    private static final ResourceLocation IRON_PLATED_LEFT_CYBERLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/iron_leftleg.png");
    private static final ResourceLocation GOLD_PLATED_LEFT_CYBERLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gold_leftleg.png");
    private static final ResourceLocation LEFT_CYBERLEG_PRIMARY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberleg_dye_primary.png");
    private static final ResourceLocation LEFT_CYBERLEG_SECONDARY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberleg_dye_secondary.png");
    private static final ResourceLocation POLAR_BEAR_FUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/polar_bear_fur.png");
    private static final ResourceLocation SPINAL_INJECTOR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spinal_injector.png");
    private static final ResourceLocation SPINAL_INJECTOR_HIGHLIGHT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spinal_injector_highlight.png");
    private static final ResourceLocation SANDEVISTAN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sandevistan.png");
    private static final ResourceLocation SANDEVISTAN_HIGHLIGHT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sandevistan_highlight.png");
    private static final ResourceLocation SANDEVISTAN_DYE1_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sandevistan_dye1.png");
    private static final ResourceLocation SANDEVISTAN_DYE2_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sandevistan_dye2.png");
    private static final ResourceLocation DEPLOYABLE_ELYTRA_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/deployable_elytra.png");
    private static final ResourceLocation DEPLOYABLE_ELYTRA_HIGHLIGHT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/deployable_elytra_highlight.png");
    private static final ResourceLocation CYBERDECK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/cyberdeck.png");
    private static final ResourceLocation CYBERDECK_DYE1 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/cyberdeck_dye1.png");
    private static final ResourceLocation CYBERDECK_DYE2 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/cyberdeck_dye2.png");
    private static final ResourceLocation GILLS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gills.png");
    private static final ResourceLocation CHIPWARE_INACTIVE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/chipware_inactive.png");
    private static final ResourceLocation CHIPWARE_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/chipware_active.png");
    private static final ResourceLocation FURNACE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/furnace.png");
    private static final ResourceLocation FURNACE_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/furnace_lit.png");
    private static final ResourceLocation FURNACE_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/furnace_lit_highlight.png");
    private static final ResourceLocation SPIDER_EYES =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spider_eyes.png");
    private static final ResourceLocation MAGIC_CATALYST =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/magic_catalyst.png");
    private static final ResourceLocation SCULKED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculked.png");
    private static final ResourceLocation SCULK_SKIN =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_skin.png");
    private static final ResourceLocation SCULK_RIGHTARM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_rightarm.png");
    private static final ResourceLocation SCULK_LEFTARM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_leftarm.png");
    private static final ResourceLocation SCULK_RIGHTLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_rightleg.png");
    private static final ResourceLocation SCULK_LEFTLEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_leftleg.png");
    private static final ResourceLocation SCULK_HEAD =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_head.png");
    private static final ResourceLocation SCULK_TORSO =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/sculk_torso.png");
    private static final ResourceLocation NEURAL_PROCESSOR =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/neural_processor.png");
    private static final ResourceLocation VAMPYRES_CLOSED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/vampyres_closed.png");
    private static final ResourceLocation VAMPYRES_OPEN =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/vampyres_open.png");
    private static final ResourceLocation MONOVISION =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/monovision.png");
    private static final ResourceLocation MONOVISION_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/monovision_highlight.png");
    private static final ResourceLocation MONOVISION_DYE1 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/monovision_dye1.png");
    private static final ResourceLocation MONOVISION_DYE2 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/monovision_dye2.png");
    private static final ResourceLocation MULTIOPTICS1 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics1.png");
    private static final ResourceLocation MULTIOPTICS1_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics1_dyed.png");
    private static final ResourceLocation MULTIOPTICS1_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics1_highlight.png");
    private static final ResourceLocation MULTIOPTICS2 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics2.png");
    private static final ResourceLocation MULTIOPTICS2_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics2_dyed.png");
    private static final ResourceLocation MULTIOPTICS2_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics2_highlight.png");
    private static final ResourceLocation MULTIOPTICS3 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics3.png");
    private static final ResourceLocation MULTIOPTICS3_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics3_dyed.png");
    private static final ResourceLocation MULTIOPTICS3_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics3_highlight.png");
    private static final ResourceLocation MULTIOPTICS4 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics4.png");
    private static final ResourceLocation MULTIOPTICS4_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics4_dyed.png");
    private static final ResourceLocation MULTIOPTICS4_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/multioptics4_highlight.png");
    private static final ResourceLocation COAXIAL_PORT_HEAD_OFF =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/coaxial_link_port_head_off.png");
    private static final ResourceLocation COAXIAL_PORT_HEAD_ON =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/coaxial_link_port_head_on.png");
    private static final ResourceLocation COAXIAL_PORT_HEAD_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/coaxial_link_port_head_on_highlight.png");
    private static final ResourceLocation COAXIAL_PORT_HEAD_DYE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/coaxial_link_port_head_dye.png");

    private static final ResourceLocation SAMSON_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/samson_eyes_dyed.png");
    private static final ResourceLocation ECLIPSE_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/eclipse_eyes_dyed.png");
    private static final ResourceLocation SPYDER_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spyder_eyes_dyed.png");
    private static final ResourceLocation AQUARIUS_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/aquarius_eyes_dyed.png");
    private static final ResourceLocation DYMOND_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dymond_eyes_dyed.png");
    private static final ResourceLocation DRAGOON_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_eyes_dyed.png");
    private static final ResourceLocation DRAGOON_EYES_DYED_FACEPLATE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_eyes_dyed_faceplate.png");
    private static final ResourceLocation COPERNICUS_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copernicus_eyes_dyed.png");
    private static final ResourceLocation GENOS_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/genos_eyes_dyed.png");
    private static final ResourceLocation GENOS_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/genos_highlight.png");
    private static final ResourceLocation KILDARE_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/kildare_eyes_dyed.png");
    private static final ResourceLocation HEXBORG_EYES_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/hexborg_eyes_dyed.png");

    private static final ResourceLocation ECLIPSE_VISOR_TRIMMED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/eclipse_visor_trimmed.png");
    private static final ResourceLocation SPYDER_VISOR_TRIMMED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spyder_visor_trimmed.png");

    //WIDE VARIANTS
    private static final ResourceLocation LEFT_CYBERARM_TEXTURE_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberarm_wide.png");
    private static final ResourceLocation COPPER_PLATED_LEFT_CYBERARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copper_leftarm_wide.png");
    private static final ResourceLocation IRON_PLATED_LEFT_CYBERARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/iron_leftarm_wide.png");
    private static final ResourceLocation GOLD_PLATED_LEFT_CYBERARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gold_leftarm_wide.png");
    private static final ResourceLocation LEFT_CYBERARM_PRIMARY_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberarm_dye_primary_wide.png");
    private static final ResourceLocation LEFT_CYBERARM_SECONDARY_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberarm_dye_secondary_wide.png");
    private static final ResourceLocation RIGHT_CYBERARM_TEXTURE_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberarm_wide.png");
    private static final ResourceLocation COPPER_PLATED_RIGHT_CYBERARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copper_rightarm_wide.png");
    private static final ResourceLocation IRON_PLATED_RIGHT_CYBERARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/iron_rightarm_wide.png");
    private static final ResourceLocation GOLD_PLATED_RIGHT_CYBERARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gold_rightarm_wide.png");
    private static final ResourceLocation RIGHT_CYBERARM_PRIMARY_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberarm_dye_primary_wide.png");
    private static final ResourceLocation RIGHT_CYBERARM_SECONDARY_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberarm_dye_secondary_wide.png");
    private static final ResourceLocation KNUCKLES_LARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/knuckles_larm_wide.png");
    private static final ResourceLocation KNUCKLES_RARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/knuckles_rarm_wide.png");
    private static final ResourceLocation NETHERPLATED_SKIN_TEXTURE_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/isothermal_skin_wide.png");
    private static final ResourceLocation FIRESTARTER_LARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/firestarter_larm_wide.png");
    private static final ResourceLocation FIRESTARTER_RARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/firestarter_rarm_wide.png");
    private static final ResourceLocation FLYWHEEL_LARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/flywheel_larm_wide.png");
    private static final ResourceLocation FLYWHEEL_RARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/flywheel_rarm_wide.png");
    private static final ResourceLocation HEAVY_KNUCKLES_LARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/heavyknuckles_left_wide.png");
    private static final ResourceLocation HEAVY_KNUCKLES_RARM_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/heavyknuckles_right_wide.png");
    private static final ResourceLocation MANASKIN_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/manaskin.png");
    private static final ResourceLocation DRAGONSKIN_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragonskin_wide.png");
    private static final ResourceLocation ARC_CANNON_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_right.png");
    private static final ResourceLocation ARC_CANNON_RIGHT_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_right_dyed.png");
    private static final ResourceLocation ARC_CANNON_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_left.png");
    private static final ResourceLocation ARC_CANNON_LEFT_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_left_dyed.png");
    private static final ResourceLocation METAL_PLATING_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/metal_plating_wide.png");
    private static final ResourceLocation METAL_PLATING_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/metal_plating_wide_dyed.png");
    private static final ResourceLocation ULTRAVIOLENT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/ultraviolent_wide.png");

    private static final ResourceLocation SAMSON_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/samson_wide.png");
    private static final ResourceLocation SAMSON_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/samson_wide_dyed.png");
    private static final ResourceLocation ECLIPSE_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/eclipse_wide.png");
    private static final ResourceLocation ECLIPSE_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/eclipse_wide_dyed.png");
    private static final ResourceLocation SPYDER_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spyder_wide.png");
    private static final ResourceLocation SPYDER_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spyder_wide_dyed.png");
    private static final ResourceLocation WINGMAN_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/wingman_wide.png");
    private static final ResourceLocation WINGMAN_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/wingman_wide_dyed.png");
    private static final ResourceLocation AQUARIUS_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/aquarius_wide.png");
    private static final ResourceLocation AQUARIUS_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/aquarius_wide_dyed.png");
    private static final ResourceLocation DYMOND_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dymond_wide.png");
    private static final ResourceLocation DYMOND_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dymond_wide_dyed.png");
    private static final ResourceLocation DRAGOON_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_wide.png");
    private static final ResourceLocation DRAGOON_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_wide_dyed.png");
    private static final ResourceLocation DRAGOON_WIDE_FACEPLATE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_wide_faceplate.png");
    private static final ResourceLocation DRAGOON_WIDE_DYED_FACEPLATE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_wide_dyed_faceplate.png");
    private static final ResourceLocation COPERNICUS_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copernicus_wide.png");
    private static final ResourceLocation COPERNICUS_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copernicus_wide_dyed.png");
    private static final ResourceLocation GENOS_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/genos_wide.png");
    private static final ResourceLocation GENOS_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/genos_wide_dyed.png");
    private static final ResourceLocation KILDARE_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/kildare_wide.png");
    private static final ResourceLocation KILDARE_WIDE_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/kildare_wide_dyed.png");
    private static final ResourceLocation HEXBORG_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/hexborg_wide.png");

    private static final ResourceLocation EXOSUIT1_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/exosuit1.png");

    //SLIM VARIANTS
    private static final ResourceLocation LEFT_CYBERARM_TEXTURE_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberarm_slim.png");
    private static final ResourceLocation COPPER_PLATED_LEFT_CYBERARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copper_leftarm_slim.png");
    private static final ResourceLocation IRON_PLATED_LEFT_CYBERARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/iron_leftarm_slim.png");
    private static final ResourceLocation GOLD_PLATED_LEFT_CYBERARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gold_leftarm_slim.png");
    private static final ResourceLocation LEFT_CYBERARM_PRIMARY_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberarm_dye_primary_slim.png");
    private static final ResourceLocation LEFT_CYBERARM_SECONDARY_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/left_cyberarm_dye_secondary_slim.png");
    private static final ResourceLocation RIGHT_CYBERARM_TEXTURE_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberarm_slim.png");
    private static final ResourceLocation COPPER_PLATED_RIGHT_CYBERARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copper_rightarm_slim.png");
    private static final ResourceLocation IRON_PLATED_RIGHT_CYBERARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/iron_rightarm_slim.png");
    private static final ResourceLocation GOLD_PLATED_RIGHT_CYBERARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/gold_rightarm_slim.png");
    private static final ResourceLocation RIGHT_CYBERARM_PRIMARY_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberarm_dye_primary_slim.png");
    private static final ResourceLocation RIGHT_CYBERARM_SECONDARY_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/right_cyberarm_dye_secondary_slim.png");
    private static final ResourceLocation KNUCKLES_LARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/knuckles_larm_slim.png");
    private static final ResourceLocation KNUCKLES_RARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/knuckles_rarm_slim.png");
    private static final ResourceLocation NETHERPLATED_SKIN_TEXTURE_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/isothermal_skin_slim.png");
    private static final ResourceLocation FIRESTARTER_LARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/firestarter_larm_slim.png");
    private static final ResourceLocation FIRESTARTER_RARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/firestarter_rarm_slim.png");
    private static final ResourceLocation FLYWHEEL_LARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/flywheel_larm_slim.png");
    private static final ResourceLocation FLYWHEEL_RARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/flywheel_rarm_slim.png");
    private static final ResourceLocation HEAVY_KNUCKLES_LARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/heavyknuckles_left_slim.png");
    private static final ResourceLocation HEAVY_KNUCKLES_RARM_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/heavyknuckles_right_slim.png");
    private static final ResourceLocation MANASKIN_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/manaskin_slim.png");
    private static final ResourceLocation DRAGONSKIN_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragonskin_slim.png");
    private static final ResourceLocation ARC_CANNON_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_right_slim.png");
    private static final ResourceLocation ARC_CANNON_RIGHT_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_right_slim_dyed.png");
    private static final ResourceLocation ARC_CANNON_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_left_slim.png");
    private static final ResourceLocation ARC_CANNON_LEFT_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/arc_cannon_left_slim_dyed.png");
    private static final ResourceLocation METAL_PLATING_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/metal_plating_slim.png");
    private static final ResourceLocation METAL_PLATING_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/metal_plating_slim_dyed.png");
    private static final ResourceLocation ULTRAVIOLENT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/ultraviolent_slim.png");

    private static final ResourceLocation SAMSON_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/samson_slim.png");
    private static final ResourceLocation SAMSON_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/samson_slim_dyed.png");
    private static final ResourceLocation ECLIPSE_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/eclipse_slim.png");
    private static final ResourceLocation ECLIPSE_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/eclipse_slim_dyed.png");
    private static final ResourceLocation SPYDER_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spyder_slim.png");
    private static final ResourceLocation SPYDER_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/spyder_slim_dyed.png");
    private static final ResourceLocation WINGMAN_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/wingman_slim.png");
    private static final ResourceLocation WINGMAN_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/wingman_slim_dyed.png");
    private static final ResourceLocation AQUARIUS_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/aquarius_slim.png");
    private static final ResourceLocation AQUARIUS_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/aquarius_slim_dyed.png");
    private static final ResourceLocation DYMOND_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dymond_slim.png");
    private static final ResourceLocation DYMOND_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dymond_slim_dyed.png");
    private static final ResourceLocation DRAGOON_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_slim.png");
    private static final ResourceLocation DRAGOON_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_slim_dyed.png");
    private static final ResourceLocation DRAGOON_SLIM_FACEPLATE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_slim_faceplate.png");
    private static final ResourceLocation DRAGOON_SLIM_DYED_FACEPLATE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/dragoon_slim_dyed_faceplate.png");
    private static final ResourceLocation COPERNICUS_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copernicus_slim.png");
    private static final ResourceLocation COPERNICUS_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/copernicus_slim_dyed.png");
    private static final ResourceLocation GENOS_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/genos_slim.png");
    private static final ResourceLocation GENOS_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/genos_slim_dyed.png");
    private static final ResourceLocation KILDARE_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/kildare_slim.png");
    private static final ResourceLocation KILDARE_SLIM_DYED =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/kildare_slim_dyed.png");
    private static final ResourceLocation HEXBORG_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/hexborg_slim.png");

    private static final ResourceLocation EXOSUIT1_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/exosuit1_slim.png");


// DURABILITY VISUAL DAMAGE
    private static final ResourceLocation BRAIN_DAMAGE_MEAT =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_brain_meat.png");
    private static final ResourceLocation BRAIN_DAMAGE_METAL =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_brain_metal.png");
    private static final ResourceLocation LARM_DAMAGE_MEAT_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_leftarm_25.png");
    private static final ResourceLocation LARM_DAMAGE_MEAT_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_leftarm_50.png");
    private static final ResourceLocation LARM_DAMAGE_MEAT_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_leftarm_75.png");
    private static final ResourceLocation LLEG_DAMAGE_MEAT_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_leftleg_25.png");
    private static final ResourceLocation LLEG_DAMAGE_MEAT_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_leftleg_50.png");
    private static final ResourceLocation LLEG_DAMAGE_MEAT_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_leftleg_75.png");
    private static final ResourceLocation RARM_DAMAGE_MEAT_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_rightarm_25.png");
    private static final ResourceLocation RARM_DAMAGE_MEAT_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_rightarm_50.png");
    private static final ResourceLocation RARM_DAMAGE_MEAT_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_rightarm_75.png");
    private static final ResourceLocation RLEG_DAMAGE_MEAT_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_rightleg_25.png");
    private static final ResourceLocation RLEG_DAMAGE_MEAT_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_rightleg_50.png");
    private static final ResourceLocation RLEG_DAMAGE_MEAT_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_rightleg_75.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_MEAT_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_25.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_MEAT_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_50.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_MEAT_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_75.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_CFRAME_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_50_capacitorframe.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_CFRAME_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_75_capacitorframe.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_LFRAME_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_50_linearframe.png");
    private static final ResourceLocation LUNGS_HEART_DAMAGE_LFRAME_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_lungsandheart_75_linearframe.png");
    private static final ResourceLocation ORGANS_DAMAGE_MEAT_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_organs_25.png");
    private static final ResourceLocation ORGANS_DAMAGE_MEAT_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_organs_50.png");
    private static final ResourceLocation ORGANS_DAMAGE_MEAT_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/meatdamage_organs_75.png");
    private static final ResourceLocation LARM_DAMAGE_METAL_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_leftarm_25.png");
    private static final ResourceLocation LARM_DAMAGE_METAL_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_leftarm_50.png");
    private static final ResourceLocation LARM_DAMAGE_METAL_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_leftarm_75.png");
    private static final ResourceLocation LLEG_DAMAGE_METAL_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_leftleg_25.png");
    private static final ResourceLocation LLEG_DAMAGE_METAL_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_leftleg_50.png");
    private static final ResourceLocation LLEG_DAMAGE_METAL_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_leftleg_75.png");
    private static final ResourceLocation RARM_DAMAGE_METAL_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_rightarm_25.png");
    private static final ResourceLocation RARM_DAMAGE_METAL_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_rightarm_50.png");
    private static final ResourceLocation RARM_DAMAGE_METAL_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_rightarm_75.png");
    private static final ResourceLocation RLEG_DAMAGE_METAL_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_rightleg_25.png");
    private static final ResourceLocation RLEG_DAMAGE_METAL_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_rightleg_50.png");
    private static final ResourceLocation RLEG_DAMAGE_METAL_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metaldamage_rightleg_75.png");
    private static final ResourceLocation SKIN_DAMAGE_TORSO_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/skindamage_torso_25.png");
    private static final ResourceLocation SKIN_DAMAGE_TORSO_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/skindamage_torso_50.png");
    private static final ResourceLocation SKIN_DAMAGE_TORSO_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/skindamage_torso_75.png");
    private static final ResourceLocation METALPLATING_DAMAGE_TORSO_25 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metalplating_torso_25.png");
    private static final ResourceLocation METALPLATING_DAMAGE_TORSO_50 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metalplating_torso_50.png");
    private static final ResourceLocation METALPLATING_DAMAGE_TORSO_75 =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/durability/metalplating_torso_75.png");


// CYBERWARE TRIMS
    private static final ResourceLocation BOLT_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_left_leg.png");
    private static final ResourceLocation BOLT_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_left_slim.png");
    private static final ResourceLocation BOLT_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_left_wide.png");
    private static final ResourceLocation BOLT_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_right_leg.png");
    private static final ResourceLocation BOLT_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_right_slim.png");
    private static final ResourceLocation BOLT_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_right_wide.png");
    private static final ResourceLocation BOLT_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_full_body.png");
    private static final ResourceLocation BOLT_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/bolt_slim_full_body.png");

    private static final ResourceLocation COAST_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_left_leg.png");
    private static final ResourceLocation COAST_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_left_slim.png");
    private static final ResourceLocation COAST_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_left_wide.png");
    private static final ResourceLocation COAST_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_right_leg.png");
    private static final ResourceLocation COAST_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_right_slim.png");
    private static final ResourceLocation COAST_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_right_wide.png");
    private static final ResourceLocation COAST_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_full_body.png");
    private static final ResourceLocation COAST_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/coast_slim_full_body.png");

    private static final ResourceLocation DUNE_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_left_leg.png");
    private static final ResourceLocation DUNE_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_left_slim.png");
    private static final ResourceLocation DUNE_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_left_wide.png");
    private static final ResourceLocation DUNE_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_right_leg.png");
    private static final ResourceLocation DUNE_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_right_slim.png");
    private static final ResourceLocation DUNE_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_right_wide.png");
    private static final ResourceLocation DUNE_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_full_body.png");
    private static final ResourceLocation DUNE_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/dune_slim_full_body.png");

    private static final ResourceLocation EYE_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_left_leg.png");
    private static final ResourceLocation EYE_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_left_slim.png");
    private static final ResourceLocation EYE_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_left_wide.png");
    private static final ResourceLocation EYE_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_right_leg.png");
    private static final ResourceLocation EYE_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_right_slim.png");
    private static final ResourceLocation EYE_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_right_wide.png");
    private static final ResourceLocation EYE_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_full_body.png");
    private static final ResourceLocation EYE_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/eye_slim_full_body.png");

    private static final ResourceLocation FLOW_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_left_leg.png");
    private static final ResourceLocation FLOW_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_left_slim.png");
    private static final ResourceLocation FLOW_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_left_wide.png");
    private static final ResourceLocation FLOW_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_right_leg.png");
    private static final ResourceLocation FLOW_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_right_slim.png");
    private static final ResourceLocation FLOW_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_right_wide.png");
    private static final ResourceLocation FLOW_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_full_body.png");
    private static final ResourceLocation FLOW_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/flow_slim_full_body.png");

    private static final ResourceLocation HOST_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_left_leg.png");
    private static final ResourceLocation HOST_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_left_slim.png");
    private static final ResourceLocation HOST_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_left_wide.png");
    private static final ResourceLocation HOST_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_right_leg.png");
    private static final ResourceLocation HOST_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_right_slim.png");
    private static final ResourceLocation HOST_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_right_wide.png");
    private static final ResourceLocation HOST_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_full_body.png");
    private static final ResourceLocation HOST_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/host_slim_full_body.png");

    private static final ResourceLocation RAISER_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_left_leg.png");
    private static final ResourceLocation RAISER_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_left_slim.png");
    private static final ResourceLocation RAISER_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_left_wide.png");
    private static final ResourceLocation RAISER_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_right_leg.png");
    private static final ResourceLocation RAISER_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_right_slim.png");
    private static final ResourceLocation RAISER_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_right_wide.png");
    private static final ResourceLocation RAISER_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_full_body.png");
    private static final ResourceLocation RAISER_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/raiser_slim_full_body.png");

    private static final ResourceLocation RIB_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_left_leg.png");
    private static final ResourceLocation RIB_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_left_slim.png");
    private static final ResourceLocation RIB_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_left_wide.png");
    private static final ResourceLocation RIB_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_right_leg.png");
    private static final ResourceLocation RIB_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_right_slim.png");
    private static final ResourceLocation RIB_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_right_wide.png");
    private static final ResourceLocation RIB_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_full_body.png");
    private static final ResourceLocation RIB_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/rib_slim_full_body.png");

    private static final ResourceLocation SENTRY_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_left_leg.png");
    private static final ResourceLocation SENTRY_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_left_slim.png");
    private static final ResourceLocation SENTRY_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_left_wide.png");
    private static final ResourceLocation SENTRY_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_right_leg.png");
    private static final ResourceLocation SENTRY_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_right_slim.png");
    private static final ResourceLocation SENTRY_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_right_wide.png");
    private static final ResourceLocation SENTRY_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_full_body.png");
    private static final ResourceLocation SENTRY_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/sentry_slim_full_body.png");

    private static final ResourceLocation SHAPER_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_left_leg.png");
    private static final ResourceLocation SHAPER_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_left_slim.png");
    private static final ResourceLocation SHAPER_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_left_wide.png");
    private static final ResourceLocation SHAPER_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_right_leg.png");
    private static final ResourceLocation SHAPER_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_right_slim.png");
    private static final ResourceLocation SHAPER_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_right_wide.png");
    private static final ResourceLocation SHAPER_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_full_body.png");
    private static final ResourceLocation SHAPER_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/shaper_slim_full_body.png");

    private static final ResourceLocation SILENCE_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_left_leg.png");
    private static final ResourceLocation SILENCE_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_left_slim.png");
    private static final ResourceLocation SILENCE_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_left_wide.png");
    private static final ResourceLocation SILENCE_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_right_leg.png");
    private static final ResourceLocation SILENCE_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_right_slim.png");
    private static final ResourceLocation SILENCE_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_right_wide.png");
    private static final ResourceLocation SILENCE_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_full_body.png");
    private static final ResourceLocation SILENCE_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/silence_slim_full_body.png");

    private static final ResourceLocation SNOUT_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_left_leg.png");
    private static final ResourceLocation SNOUT_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_left_slim.png");
    private static final ResourceLocation SNOUT_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_left_wide.png");
    private static final ResourceLocation SNOUT_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_right_leg.png");
    private static final ResourceLocation SNOUT_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_right_slim.png");
    private static final ResourceLocation SNOUT_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_right_wide.png");
    private static final ResourceLocation SNOUT_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_full_body.png");
    private static final ResourceLocation SNOUT_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/snout_slim_full_body.png");

    private static final ResourceLocation SPIRE_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_left_leg.png");
    private static final ResourceLocation SPIRE_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_left_slim.png");
    private static final ResourceLocation SPIRE_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_left_wide.png");
    private static final ResourceLocation SPIRE_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_right_leg.png");
    private static final ResourceLocation SPIRE_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_right_slim.png");
    private static final ResourceLocation SPIRE_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_right_wide.png");
    private static final ResourceLocation SPIRE_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_full_body.png");
    private static final ResourceLocation SPIRE_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/spire_slim_full_body.png");

    private static final ResourceLocation TIDE_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_left_leg.png");
    private static final ResourceLocation TIDE_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_left_slim.png");
    private static final ResourceLocation TIDE_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_left_wide.png");
    private static final ResourceLocation TIDE_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_right_leg.png");
    private static final ResourceLocation TIDE_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_right_slim.png");
    private static final ResourceLocation TIDE_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_right_wide.png");
    private static final ResourceLocation TIDE_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_full_body.png");
    private static final ResourceLocation TIDE_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/tide_slim_full_body.png");

    private static final ResourceLocation VEX_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_left_leg.png");
    private static final ResourceLocation VEX_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_left_slim.png");
    private static final ResourceLocation VEX_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_left_wide.png");
    private static final ResourceLocation VEX_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_right_leg.png");
    private static final ResourceLocation VEX_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_right_slim.png");
    private static final ResourceLocation VEX_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_right_wide.png");
    private static final ResourceLocation VEX_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_full_body.png");
    private static final ResourceLocation VEX_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/vex_slim_full_body.png");

    private static final ResourceLocation WARD_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_left_leg.png");
    private static final ResourceLocation WARD_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_left_slim.png");
    private static final ResourceLocation WARD_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_left_wide.png");
    private static final ResourceLocation WARD_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_right_leg.png");
    private static final ResourceLocation WARD_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_right_slim.png");
    private static final ResourceLocation WARD_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_right_wide.png");
    private static final ResourceLocation WARD_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_full_body.png");
    private static final ResourceLocation WARD_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/ward_slim_full_body.png");

    private static final ResourceLocation WAYFINDER_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_left_leg.png");
    private static final ResourceLocation WAYFINDER_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_left_slim.png");
    private static final ResourceLocation WAYFINDER_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_left_wide.png");
    private static final ResourceLocation WAYFINDER_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_right_leg.png");
    private static final ResourceLocation WAYFINDER_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_right_slim.png");
    private static final ResourceLocation WAYFINDER_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_right_wide.png");
    private static final ResourceLocation WAYFINDER_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_full_body.png");
    private static final ResourceLocation WAYFINDER_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wayfinder_slim_full_body.png");

    private static final ResourceLocation WILD_LEFT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_left_leg.png");
    private static final ResourceLocation WILD_LEFT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_left_slim.png");
    private static final ResourceLocation WILD_LEFT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_left_wide.png");
    private static final ResourceLocation WILD_RIGHT_LEG =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_right_leg.png");
    private static final ResourceLocation WILD_RIGHT_SLIM =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_right_slim.png");
    private static final ResourceLocation WILD_RIGHT_WIDE =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_right_wide.png");
    private static final ResourceLocation WILD_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_full_body.png");
    private static final ResourceLocation WILD_SLIM_FULL_BODY =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "textures/entity/trims/wild_slim_full_body.png");




    public static final String JACKED_IN_STANDIN_FLAG = "CreateCyberneticsJackedInStandIn";

    public static SkinModifierState getPlayerSkinState(AbstractClientPlayer player) {
        PlayerCyberwareData data = PlayerCyberwareData.getForVisual(player, player.registryAccess());
        if (data == null) {
            data = tryBuildCyberwareDataFromSnapshot(player);
        }
        if (data == null) return null;

        UUID playerId = player.getUUID();
        SkinModifierState state = PLAYER_STATES.computeIfAbsent(playerId, k -> new SkinModifierState());
        state.clearModifiers();
        addTattooModifierIfLayer(state, player, TattooLayer.UNDER_CYBERWARE);

        EnumSet<SkinModifier.HideVanilla> FULL_OUTER_HIDE = EnumSet.allOf(SkinModifier.HideVanilla.class);
        EnumSet<SkinModifier.HideVanilla> GENOS_OUTER_HIDE = EnumSet.of(
                SkinModifier.HideVanilla.JACKET,
                SkinModifier.HideVanilla.LEFT_SLEEVE,
                SkinModifier.HideVanilla.RIGHT_SLEEVE,
                SkinModifier.HideVanilla.LEFT_PANTS,
                SkinModifier.HideVanilla.RIGHT_PANTS
        );


// --- FACEPLATE BASE-SKIN OVERRIDE (alias -> Mojang skin) ---
        Component custom = player.getCustomName();
        if (custom != null) {
            String alias = custom.getString();
            FaceplateSkinOverrideClient.ResolvedSkin resolved = FaceplateSkinOverrideClient.getOrRequest(alias);
            if (resolved != null) {
                state.addModifier(new SkinModifier(resolved.texture(), resolved.texture(),
                        0xFFFFFFFF, false, FULL_OUTER_HIDE));
            }
        }


// MERMOD TAIL
        boolean mermodTailActive = false;
        if (ModCompats.isInstalled("mermod")) {

            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack necklaceCurio = CuriosCompat.findFirstCurio(player, "necklace", st -> {
                        ResourceLocation k = BuiltInRegistries.ITEM.getKey(st.getItem());return MERMOD_SEA_NECKLACE_ID.equals(k);
                    })
                    .orElse(ItemStack.EMPTY);

            boolean hasNecklaceEquipped = false;
            ItemStack necklaceStack = ItemStack.EMPTY;

            if (!chest.isEmpty()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(chest.getItem());
                if (MERMOD_SEA_NECKLACE_ID.equals(key)) {
                    hasNecklaceEquipped = true;
                    necklaceStack = chest;
                }
            }

            if (!hasNecklaceEquipped && !necklaceCurio.isEmpty()) {
                hasNecklaceEquipped = true;
                necklaceStack = necklaceCurio;
            }

            if (hasNecklaceEquipped) {
                boolean inWater = player.isInWaterOrBubble();

                var moisturizerTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("mermod", "tail_moisturizer_modifier"));

                boolean hasMoisturizerByItemTag = necklaceStack.getTags().anyMatch(t -> t.equals(moisturizerTag));
                boolean hasMoisturizerByComponent = hasMermodMoisturizerComponent(necklaceStack);

                mermodTailActive = inWater || hasMoisturizerByItemTag || hasMoisturizerByComponent;
            }
        }





// CYBEREYES
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
            boolean hasPrimaryDye = data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);
            boolean hasSecondaryDye = data.isSecondaryDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

            int primaryTint = hasPrimaryDye ? data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) : 0xFFFFFFFF;
            int secondaryTint = hasSecondaryDye ? data.secondaryDyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) : 0xFFFFFFFF;

            if (hasPrimaryDye && hasSecondaryDye) {
                ResourceLocation primaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildPrimaryHighlight(player);
                ResourceLocation secondaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildSecondaryOverlay(player);

                if (primaryOverlay != null) {
                    state.addModifier(new SkinModifier(primaryOverlay, primaryOverlay, primaryTint, false));
                    state.addHighlight(new SkinHighlight(primaryOverlay, primaryOverlay, primaryTint, true, true));
                }

                if (secondaryOverlay != null) {
                    state.addModifier(new SkinModifier(secondaryOverlay, secondaryOverlay, secondaryTint, false));
                    state.addHighlight(new SkinHighlight(secondaryOverlay, secondaryOverlay, secondaryTint, true, true));
                }
            } else if (hasPrimaryDye) {
                ResourceLocation primaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildCombinedHighlight(player);

                if (primaryOverlay != null) {
                    state.addModifier(new SkinModifier(primaryOverlay, primaryOverlay, primaryTint, false));
                    state.addHighlight(new SkinHighlight(primaryOverlay, primaryOverlay, primaryTint, true, true));
                }
            } else if (hasSecondaryDye) {
                ResourceLocation secondaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildSecondaryOverlay(player);

                if (secondaryOverlay != null) {
                    state.addModifier(new SkinModifier(secondaryOverlay, secondaryOverlay, secondaryTint, false));
                    state.addHighlight(new SkinHighlight(secondaryOverlay, secondaryOverlay, secondaryTint, true, true));
                }
            }
        }

// MISSING SKIN
        if (!data.hasAnyTagged(ModTags.Items.SKIN_ITEMS, CyberwareSlot.SKIN)) {
            state.addModifier(new SkinModifier(MISSING_SKIN_TEXTURE, MISSING_SKIN_TEXTURE,
                    0xFFFFFFFF, true));
        }


// METAL PLATING
        boolean hasNaturalSkin = data.hasSpecificItem(ModItems.BODYPART_SKIN.get(), CyberwareSlot.SKIN);
        boolean hasSynthSkin = data.hasSpecificItem(ModItems.SKINUPGRADES_SYNTHSKIN.get(), CyberwareSlot.SKIN);
        boolean hasMetalPlating = data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);
        boolean isFullBorg = FullBorgHandler.isFullBorg(data);

        if (!isFullBorg && !(hasNaturalSkin || hasSynthSkin) && hasMetalPlating) {
            state.addModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM,
                    0xFFFFFFFF, true, GENOS_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(METAL_PLATING_DYED, METAL_PLATING_SLIM_DYED,
                        tint, true, GENOS_OUTER_HIDE));
            }
        }

// LEFT CYBERLEG
        if (!mermodTailActive) {
            if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)) {
                state.addModifier(SkinModifier.leftLeg(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE, 0xFFFFFFFF));

                if (data.isDyed(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)) {
                    int tint = data.dyeColor(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG);
                    state.addModifier(SkinModifier.leftLeg(LEFT_CYBERLEG_PRIMARY, LEFT_CYBERLEG_PRIMARY, tint));
                }

                if (data.isSecondaryDyed(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)) {
                    int tint = data.secondaryDyeColor(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG);
                    state.addModifier(SkinModifier.leftLeg(LEFT_CYBERLEG_SECONDARY, LEFT_CYBERLEG_SECONDARY, tint));
                }

                if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.leftLeg(tex, tex, tint));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get(), CyberwareSlot.LLEG)) {
                state.addModifier(SkinModifier.leftLeg(COPPER_PLATED_LEFT_CYBERLEG, COPPER_PLATED_LEFT_CYBERLEG, 0xFFFFFFFF));

                if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get(), CyberwareSlot.LLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get(), CyberwareSlot.LLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get(), CyberwareSlot.LLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.leftLeg(tex, tex, tint));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get(), CyberwareSlot.LLEG)) {
                state.addModifier(SkinModifier.leftLeg(IRON_PLATED_LEFT_CYBERLEG, IRON_PLATED_LEFT_CYBERLEG, 0xFFFFFFFF));

                if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get(), CyberwareSlot.LLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get(), CyberwareSlot.LLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get(), CyberwareSlot.LLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.leftLeg(tex, tex, tint));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get(), CyberwareSlot.LLEG)) {
                state.addModifier(SkinModifier.leftLeg(GOLD_PLATED_LEFT_CYBERLEG, GOLD_PLATED_LEFT_CYBERLEG, 0xFFFFFFFF));

                if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get(), CyberwareSlot.LLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get(), CyberwareSlot.LLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get(), CyberwareSlot.LLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.leftLeg(tex, tex, tint));
                    }
                }
            }
        }

// RIGHT CYBERLEG
        if (!mermodTailActive) {
            if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)) {
                state.addModifier(SkinModifier.rightLeg(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE, 0xFFFFFFFF));

                if (data.isDyed(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)) {
                    int tint = data.dyeColor(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG);
                    state.addModifier(SkinModifier.rightLeg(RIGHT_CYBERLEG_PRIMARY, RIGHT_CYBERLEG_PRIMARY, tint));
                }

                if (data.isSecondaryDyed(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)) {
                    int tint = data.secondaryDyeColor(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG);
                    state.addModifier(SkinModifier.rightLeg(RIGHT_CYBERLEG_SECONDARY, RIGHT_CYBERLEG_SECONDARY, tint));
                }

                if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.rightLeg(tex, tex, tint));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get(), CyberwareSlot.RLEG)) {
                state.addModifier(SkinModifier.rightLeg(COPPER_PLATED_RIGHT_CYBERLEG, COPPER_PLATED_RIGHT_CYBERLEG, 0xFFFFFFFF));

                if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get(), CyberwareSlot.RLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get(), CyberwareSlot.RLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get(), CyberwareSlot.RLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.rightLeg(tex, tex, tint));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get(), CyberwareSlot.RLEG)) {
                state.addModifier(SkinModifier.rightLeg(IRON_PLATED_RIGHT_CYBERLEG, IRON_PLATED_RIGHT_CYBERLEG, 0xFFFFFFFF));

                if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get(), CyberwareSlot.RLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get(), CyberwareSlot.RLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get(), CyberwareSlot.RLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.rightLeg(tex, tex, tint));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get(), CyberwareSlot.RLEG)) {
                state.addModifier(SkinModifier.rightLeg(GOLD_PLATED_RIGHT_CYBERLEG, GOLD_PLATED_RIGHT_CYBERLEG, 0xFFFFFFFF));

                if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get(), CyberwareSlot.RLEG)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get(), CyberwareSlot.RLEG);
                    int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get(), CyberwareSlot.RLEG);

                    ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.LEG, false);
                    if (tex != null) {
                        state.addModifier(SkinModifier.rightLeg(tex, tex, tint));
                    }
                }
            }
        }

// LEFT CYBERARM
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)) {
            state.addModifier(SkinModifier.leftArm(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM, 0xFFFFFFFF));

            if (data.isDyed(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM);
                state.addModifier(SkinModifier.leftArm(LEFT_CYBERARM_PRIMARY_WIDE, LEFT_CYBERARM_PRIMARY_SLIM, tint));
            }

            if (data.isSecondaryDyed(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)) {
                int tint = data.secondaryDyeColor(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM);
                state.addModifier(SkinModifier.leftArm(LEFT_CYBERARM_SECONDARY_WIDE, LEFT_CYBERARM_SECONDARY_SLIM, tint));
            }

            boolean slim = isSlimArms(player);

            if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM);

                ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.leftArm(tex, tex, tint));
                }
            }
        }

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get(), CyberwareSlot.LARM)) {
            state.addModifier(SkinModifier.leftArm(COPPER_PLATED_LEFT_CYBERARM_WIDE, COPPER_PLATED_LEFT_CYBERARM_SLIM, 0xFFFFFFFF));

            if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get(), CyberwareSlot.LARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get(), CyberwareSlot.LARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get(), CyberwareSlot.LARM);

                boolean slim = isSlimArms(player);

                ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.leftArm(tex, tex, tint));
                }
            }
        }

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get(), CyberwareSlot.LARM)) {
            state.addModifier(SkinModifier.leftArm(IRON_PLATED_LEFT_CYBERARM_WIDE, IRON_PLATED_LEFT_CYBERARM_SLIM, 0xFFFFFFFF));

            if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get(), CyberwareSlot.LARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get(), CyberwareSlot.LARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get(), CyberwareSlot.LARM);

                boolean slim = isSlimArms(player);

                ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.leftArm(tex, tex, tint));
                }
            }
        }

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get(), CyberwareSlot.LARM)) {
            state.addModifier(SkinModifier.leftArm(GOLD_PLATED_LEFT_CYBERARM_WIDE, GOLD_PLATED_LEFT_CYBERARM_SLIM, 0xFFFFFFFF));

            if (data.isTrimmed(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get(), CyberwareSlot.LARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get(), CyberwareSlot.LARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get(), CyberwareSlot.LARM);

                boolean slim = isSlimArms(player);

                ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.leftArm(tex, tex, tint));
                }
            }
        }

// RIGHT CYBERARM
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)) {
            state.addModifier(SkinModifier.rightArm(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM, 0xFFFFFFFF));

            if (data.isDyed(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM);
                state.addModifier(SkinModifier.rightArm(RIGHT_CYBERARM_PRIMARY_WIDE, RIGHT_CYBERARM_PRIMARY_SLIM, tint));
            }

            if (data.isSecondaryDyed(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)) {
                int tint = data.secondaryDyeColor(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM);
                state.addModifier(SkinModifier.rightArm(RIGHT_CYBERARM_SECONDARY_WIDE, RIGHT_CYBERARM_SECONDARY_SLIM, tint));
            }

            boolean slim = isSlimArms(player);

            if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM);

                ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.rightArm(tex, tex, tint));
                }
            }
        }

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get(), CyberwareSlot.RARM)) {
            state.addModifier(SkinModifier.rightArm(COPPER_PLATED_RIGHT_CYBERARM_WIDE, COPPER_PLATED_RIGHT_CYBERARM_SLIM, 0xFFFFFFFF));

            if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get(), CyberwareSlot.RARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get(), CyberwareSlot.RARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get(), CyberwareSlot.RARM);

                boolean slim = isSlimArms(player);

                ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.rightArm(tex, tex, tint));
                }
            }
        }

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get(), CyberwareSlot.RARM)) {
            state.addModifier(SkinModifier.rightArm(IRON_PLATED_RIGHT_CYBERARM_WIDE, IRON_PLATED_RIGHT_CYBERARM_SLIM, 0xFFFFFFFF));

            if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get(), CyberwareSlot.RARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get(), CyberwareSlot.RARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get(), CyberwareSlot.RARM);

                boolean slim = isSlimArms(player);

                ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.rightArm(tex, tex, tint));
                }
            }
        }

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get(), CyberwareSlot.RARM)) {
            state.addModifier(SkinModifier.rightArm(GOLD_PLATED_RIGHT_CYBERARM_WIDE, GOLD_PLATED_RIGHT_CYBERARM_SLIM, 0xFFFFFFFF));

            if (data.isTrimmed(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get(), CyberwareSlot.RARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get(), CyberwareSlot.RARM);
                int tint = data.trimColor(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get(), CyberwareSlot.RARM);

                boolean slim = isSlimArms(player);

                ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.rightArm(tex, tex, tint));
                }
            }
        }

// ARC CANNON RIGHT
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM)) {
            state.addModifier(SkinModifier.rightArm(ARC_CANNON_RIGHT_WIDE, ARC_CANNON_RIGHT_SLIM, 0xFFFFFFFF));

            if (data.isDyed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM)) {
                int tint = data.dyeColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM);
                state.addModifier(SkinModifier.rightArm(ARC_CANNON_RIGHT_WIDE_DYED, ARC_CANNON_RIGHT_SLIM_DYED, tint));
            }

            boolean slim = isSlimArms(player);

            if (data.isTrimmed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM);
                int tint = data.trimColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM);

                ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.rightArm(tex, tex, tint));
                }
            }
        }

// ARC CANNON LEFT
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM)) {
            state.addModifier(SkinModifier.leftArm(ARC_CANNON_LEFT_WIDE, ARC_CANNON_LEFT_SLIM, 0xFFFFFFFF));

            if (data.isDyed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM)) {
                int tint = data.dyeColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM);
                state.addModifier(SkinModifier.leftArm(ARC_CANNON_LEFT_WIDE_DYED, ARC_CANNON_LEFT_SLIM_DYED, tint));
            }

            boolean slim = isSlimArms(player);

            if (data.isTrimmed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM);
                int tint = data.trimColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM);

                ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.ARM, slim);
                if (tex != null) {
                    state.addModifier(SkinModifier.leftArm(tex, tex, tint));
                }
            }
        }


// ISOTHERMAL SKIN
        if (data.hasSpecificItem(ModItems.SKINUPGRADES_NETHERITEPLATING.get(), CyberwareSlot.SKIN)) {
            state.addModifier(new SkinModifier(NETHERPLATED_SKIN_TEXTURE_WIDE, NETHERPLATED_SKIN_TEXTURE_SLIM,
                    0xFFFFFFFF, true, FULL_OUTER_HIDE));
        }

// SAMSON MODEL
        if (FullBorgHandler.isSamson(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(SAMSON_WIDE, SAMSON_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(SAMSON_WIDE_DYED, SAMSON_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(SAMSON_EYES_DYED, SAMSON_EYES_DYED,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(SAMSON_EYES_DYED, SAMSON_EYES_DYED,
                        tint, true, true));
            }
        }

// ECLIPSE MODEL
        if (FullBorgHandler.isEclipse(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(ECLIPSE_WIDE, ECLIPSE_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(ECLIPSE_WIDE_DYED, ECLIPSE_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(ECLIPSE_EYES_DYED, ECLIPSE_EYES_DYED,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(ECLIPSE_EYES_DYED, ECLIPSE_EYES_DYED,
                        tint, true, true));
            }

            if (data.isTrimmed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.trimColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);
                state.addModifier(new SkinModifier(ECLIPSE_VISOR_TRIMMED, ECLIPSE_VISOR_TRIMMED,
                        tint, false, FULL_OUTER_HIDE));
            }
        }

// SPYDER MODEL
        if (FullBorgHandler.isSpyder(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(SPYDER_WIDE, SPYDER_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(SPYDER_WIDE_DYED, SPYDER_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(SPYDER_EYES_DYED, SPYDER_EYES_DYED,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(SPYDER_EYES_DYED, SPYDER_EYES_DYED,
                        tint, true, true));
            }

            if (data.isTrimmed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.trimColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);
                state.addModifier(new SkinModifier(SPYDER_VISOR_TRIMMED, SPYDER_VISOR_TRIMMED,
                        tint, false, FULL_OUTER_HIDE));
            }
        }

// WINGMAN MODEL
        if (FullBorgHandler.isWingman(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(WINGMAN_WIDE, WINGMAN_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(WINGMAN_WIDE_DYED, WINGMAN_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }
        }

// AQUARIUS MODEL
        if (FullBorgHandler.isAquarius(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(AQUARIUS_WIDE, AQUARIUS_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(AQUARIUS_WIDE_DYED, AQUARIUS_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(AQUARIUS_EYES_DYED, AQUARIUS_EYES_DYED,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(AQUARIUS_EYES_DYED, AQUARIUS_EYES_DYED,
                        tint, true, true));
            }
        }

// DYMOND MODEL
        if (FullBorgHandler.isDymond(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(DYMOND_WIDE, DYMOND_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(DYMOND_WIDE_DYED, DYMOND_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(DYMOND_EYES_DYED, DYMOND_EYES_DYED,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(DYMOND_EYES_DYED, DYMOND_EYES_DYED,
                        tint, true, true));
            }
        }

// DRAGOON MODEL
        if (FullBorgHandler.isDragoon(data)) {

            state.clearModifiers();

            if (!data.hasSpecificItem(ModItems.SKINUPGRADES_FACEPLATE.get(), CyberwareSlot.SKIN)) {
                state.addModifier(new SkinModifier(DRAGOON_WIDE, DRAGOON_SLIM,
                        0xFFFFFFFF, false, FULL_OUTER_HIDE));

                if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                    int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                    state.addModifier(new SkinModifier(DRAGOON_WIDE_DYED, DRAGOON_SLIM_DYED,
                            tint, false, FULL_OUTER_HIDE));
                }

                if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                    int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                    state.addModifier(new SkinModifier(DRAGOON_EYES_DYED, DRAGOON_EYES_DYED,
                            tint, false, FULL_OUTER_HIDE));
                    state.addHighlight(new SkinHighlight(DRAGOON_EYES_DYED, DRAGOON_EYES_DYED,
                            tint, true, true));
                }
            } else {
                state.addModifier(new SkinModifier(DRAGOON_WIDE_FACEPLATE, DRAGOON_SLIM_FACEPLATE,
                        0xFFFFFFFF, false, FULL_OUTER_HIDE));

                if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                    int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                    state.addModifier(new SkinModifier(DRAGOON_WIDE_DYED_FACEPLATE, DRAGOON_SLIM_DYED_FACEPLATE,
                            tint, false, FULL_OUTER_HIDE));
                }

                if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                    int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                    state.addModifier(new SkinModifier(DRAGOON_EYES_DYED_FACEPLATE, DRAGOON_EYES_DYED_FACEPLATE,
                            tint, false, FULL_OUTER_HIDE));
                    state.addHighlight(new SkinHighlight(DRAGOON_EYES_DYED_FACEPLATE, DRAGOON_EYES_DYED_FACEPLATE,
                            tint, true, true));

                    ResourceLocation dyn = CybereyeOverlayHandler.getOrBuildOverlay(player);
                    if (dyn != null) {
                        state.addModifier(new SkinModifier(dyn, dyn,
                                tint, false));
                        state.addHighlight(new SkinHighlight(dyn, dyn,
                                tint, true, true));
                    }
                }
            }
        }

// COPERNICUS MODEL
        if (ModCompats.isInstalled("northstar") ||  ModCompats.isInstalled("creatingspace")) {
            if (FullBorgHandler.isCopernicus(data)) {

                state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
                state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
                state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
                state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
                state.removeModifier(new SkinModifier(NETHERPLATED_SKIN_TEXTURE_WIDE, NETHERPLATED_SKIN_TEXTURE_SLIM));
                state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
                state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
                state.clearHighlights();
                state.clearModifiers();

                state.addModifier(new SkinModifier(COPERNICUS_WIDE, COPERNICUS_SLIM,
                        0xFFFFFFFF, false, FULL_OUTER_HIDE));

                if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                    int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                    state.addModifier(new SkinModifier(COPERNICUS_WIDE_DYED, COPERNICUS_SLIM_DYED,
                            tint, false, FULL_OUTER_HIDE));
                }

                if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                    int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                    state.addModifier(new SkinModifier(COPERNICUS_EYES_DYED, COPERNICUS_EYES_DYED,
                            tint, false, FULL_OUTER_HIDE));
                }
            }
        }

// GENOS MODEL
        if (FullBorgHandler.isGenos(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.clearModifiers();

            state.addModifier(new SkinModifier(GENOS_WIDE, GENOS_SLIM,
                    0xFFFFFFFF, false, GENOS_OUTER_HIDE));
            state.addHighlight(new SkinHighlight(GENOS_HIGHLIGHT, GENOS_HIGHLIGHT,
                    0xFFFFFF, true, false));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(GENOS_WIDE_DYED, GENOS_SLIM_DYED,
                        tint, false, GENOS_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);
                state.clearHighlights();

                state.addModifier(new SkinModifier(GENOS_EYES_DYED, GENOS_EYES_DYED,
                        tint, false, GENOS_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(GENOS_EYES_DYED, GENOS_EYES_DYED,
                        tint, true, true));

                ResourceLocation dyn = CybereyeOverlayHandler.getOrBuildOverlay(player);
                if (dyn != null) {
                    state.addModifier(new SkinModifier(dyn, dyn, tint, false, GENOS_OUTER_HIDE));
                    state.addHighlight(new SkinHighlight(dyn, dyn, tint, true, true));
                }
            }
        }

// KILDARE MODEL
        if (FullBorgHandler.isKildare(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(KILDARE_WIDE, KILDARE_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                state.addModifier(new SkinModifier(KILDARE_WIDE_DYED, KILDARE_SLIM_DYED,
                        tint, false, FULL_OUTER_HIDE));
            }

            if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(KILDARE_HIGHLIGHT, KILDARE_HIGHLIGHT,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(KILDARE_HIGHLIGHT, KILDARE_HIGHLIGHT,
                        tint, true, true));
            }
        }

// HEXBORG MODEL
        if (FullBorgHandler.isHexborg(data)) {

            state.removeModifier(new SkinModifier(LEFT_CYBERLEG_TEXTURE, LEFT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(RIGHT_CYBERLEG_TEXTURE, RIGHT_CYBERLEG_TEXTURE));
            state.removeModifier(new SkinModifier(LEFT_CYBERARM_TEXTURE_WIDE, LEFT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(RIGHT_CYBERARM_TEXTURE_WIDE, RIGHT_CYBERARM_TEXTURE_SLIM));
            state.removeModifier(new SkinModifier(METAL_PLATING_WIDE, METAL_PLATING_SLIM));
            state.removeModifier(new SkinModifier(MANASKIN_WIDE, MANASKIN_SLIM));
            state.removeModifier(new SkinModifier(CYBEREYES_PRIMARY, CYBEREYES_PRIMARY));
            state.clearHighlights();
            state.clearModifiers();

            state.addModifier(new SkinModifier(HEXBORG_WIDE, HEXBORG_SLIM,
                    0xFFFFFFFF, false, FULL_OUTER_HIDE));

            if (data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(HEXBORG_EYES_DYED, HEXBORG_EYES_DYED,
                        tint, false, FULL_OUTER_HIDE));
                state.addHighlight(new SkinHighlight(HEXBORG_EYES_DYED, HEXBORG_EYES_DYED,
                        tint, true, true));
            }
        }

// SCULKED
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKRIGHTARM.get(), CyberwareSlot.RARM)) {
            state.addModifier(new SkinModifier(SCULK_RIGHTARM, SCULK_RIGHTARM,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.RIGHT_SLEEVE)));
        }
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKLEFTARM.get(), CyberwareSlot.LARM)) {
            state.addModifier(new SkinModifier(SCULK_LEFTARM, SCULK_LEFTARM,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.LEFT_SLEEVE)));
        }
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKRIGHTLEG.get(), CyberwareSlot.RLEG)) {
            state.addModifier(new SkinModifier(SCULK_RIGHTLEG, SCULK_RIGHTLEG,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.RIGHT_PANTS)));
        }
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKLEFTLEG.get(), CyberwareSlot.LLEG)) {
            state.addModifier(new SkinModifier(SCULK_LEFTLEG, SCULK_LEFTLEG,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.LEFT_PANTS)));
        }
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKBRAIN.get(), CyberwareSlot.BRAIN)) {
            state.addModifier(new SkinModifier(SCULK_HEAD, SCULK_HEAD,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.HAT)));
        }
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKLIVER.get(), CyberwareSlot.ORGANS) &&
                data.hasSpecificItem(ModItems.BODYPART_SCULKINTESTINES.get(), CyberwareSlot.ORGANS)) {
            state.addModifier(new SkinModifier(SCULK_TORSO, SCULK_TORSO,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.JACKET)));
        }
        if (data.hasSpecificItem(ModItems.BODYPART_SCULKSKIN.get(), CyberwareSlot.SKIN) &&
                data.hasSpecificItem(ModItems.BODYPART_SCULKMUSCLE.get(), CyberwareSlot.MUSCLE)) {
            state.addModifier(new SkinModifier(SCULK_SKIN, SCULK_SKIN,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.RIGHT_PANTS, SkinModifier.HideVanilla.LEFT_PANTS,
                    SkinModifier.HideVanilla.RIGHT_SLEEVE, SkinModifier.HideVanilla.LEFT_SLEEVE, SkinModifier.HideVanilla.JACKET)));
        }

        if (data.hasSpecificItem(ModItems.BODYPART_SCULKRIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BODYPART_SCULKLEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BODYPART_SCULKRIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BODYPART_SCULKLEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.BODYPART_SCULKBRAIN.get(), CyberwareSlot.BRAIN) && data.hasSpecificItem(ModItems.BODYPART_SCULKLIVER.get(), CyberwareSlot.ORGANS) &&
                data.hasSpecificItem(ModItems.BODYPART_SCULKINTESTINES.get(), CyberwareSlot.ORGANS) && data.hasSpecificItem(ModItems.BODYPART_SCULKMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.BODYPART_SCULKSKIN.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.WETWARE_SCULKLUNGS.get(), CyberwareSlot.LUNGS) &&
                data.hasSpecificItem(ModItems.WETWARE_SCULKHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.WETWARE_WARDENANTLERS.get(), CyberwareSlot.EYES)) {
            state.addModifier(new SkinModifier(SCULKED, SCULKED,
                    0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.RIGHT_PANTS, SkinModifier.HideVanilla.LEFT_PANTS,
                    SkinModifier.HideVanilla.RIGHT_SLEEVE, SkinModifier.HideVanilla.LEFT_SLEEVE, SkinModifier.HideVanilla.JACKET)));
        }


// --- SYNTHSKIN  ---
        if (hasSynthSkin) {
            state.clearHighlights();
            state.clearModifiers();
            addTattooModifierIfLayer(state, player, TattooLayer.UNDER_CYBERWARE);

            if (data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)) {
                boolean hasPrimaryDye = data.isDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);
                boolean hasSecondaryDye = data.isSecondaryDyed(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES);

                int primaryTint = hasPrimaryDye ? data.dyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) : 0xFFFFFFFF;
                int secondaryTint = hasSecondaryDye ? data.secondaryDyeColor(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) : 0xFFFFFFFF;

                if (hasPrimaryDye && hasSecondaryDye) {
                    ResourceLocation primaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildPrimaryHighlight(player);
                    ResourceLocation secondaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildSecondaryOverlay(player);

                    if (primaryOverlay != null) {
                        state.addModifier(new SkinModifier(primaryOverlay, primaryOverlay, primaryTint, false));
                        state.addHighlight(new SkinHighlight(primaryOverlay, primaryOverlay, primaryTint, true, true));
                    }

                    if (secondaryOverlay != null) {
                        state.addModifier(new SkinModifier(secondaryOverlay, secondaryOverlay, secondaryTint, false));
                        state.addHighlight(new SkinHighlight(secondaryOverlay, secondaryOverlay, secondaryTint, true, true));
                    }
                } else if (hasPrimaryDye) {
                    ResourceLocation dyn = CybereyeOverlayHandler.getOrBuildOverlay(player);

                    if (dyn != null) {
                        state.addModifier(new SkinModifier(dyn, dyn, primaryTint, false));
                        state.addHighlight(new SkinHighlight(dyn, dyn, primaryTint, true, true));
                    }
                } else if (hasSecondaryDye) {
                    ResourceLocation secondaryOverlay = CybereyeSecondaryOverlayHandler.getOrBuildSecondaryOverlay(player);

                    if (secondaryOverlay != null) {
                        state.addModifier(new SkinModifier(secondaryOverlay, secondaryOverlay, secondaryTint, false));
                        state.addHighlight(new SkinHighlight(secondaryOverlay, secondaryOverlay, secondaryTint, true, true));
                    }
                }
            }

            if (data.hasSpecificItem(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM)) {
                state.addModifier(SkinModifier.rightArm(ARC_CANNON_RIGHT_WIDE, ARC_CANNON_RIGHT_SLIM, 0xFFFFFFFF));

                if (data.isDyed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM)) {
                    int tint = data.dyeColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM);
                    state.addModifier(SkinModifier.rightArm(ARC_CANNON_RIGHT_WIDE_DYED, ARC_CANNON_RIGHT_SLIM_DYED, tint));
                }

                boolean slim = isSlimArms(player);

                if (data.isTrimmed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM);
                    int tint = data.trimColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.RARM);

                    ResourceLocation tex = resolveTrimOverlay(patternId, false, Limb.ARM, slim);
                    if (tex != null) {
                        state.addModifier(SkinModifier.rightArm(tex, tex, tint));
                    }
                }
            }
            if (data.hasSpecificItem(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM)) {
                state.addModifier(SkinModifier.leftArm(ARC_CANNON_LEFT_WIDE, ARC_CANNON_LEFT_SLIM, 0xFFFFFFFF));

                if (data.isDyed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM)) {
                    int tint = data.dyeColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM);
                    state.addModifier(SkinModifier.leftArm(ARC_CANNON_LEFT_WIDE_DYED, ARC_CANNON_LEFT_SLIM_DYED, tint));
                }

                boolean slim = isSlimArms(player);

                if (data.isTrimmed(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM)) {
                    ResourceLocation patternId = data.trimPatternId(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM);
                    int tint = data.trimColor(ModItems.ARMUPGRADES_ARCCANNON.get(), CyberwareSlot.LARM);

                    ResourceLocation tex = resolveTrimOverlay(patternId, true, Limb.ARM, slim);
                    if (tex != null) {
                        state.addModifier(SkinModifier.leftArm(tex, tex, tint));
                    }
                }
            }
            if (custom != null) {
                String alias = custom.getString();
                FaceplateSkinOverrideClient.ResolvedSkin resolved = FaceplateSkinOverrideClient.getOrRequest(alias);
                if (resolved != null) {
                    state.addModifier(new SkinModifier(resolved.texture(), resolved.texture(),
                            0xFFFFFFFF, false, FULL_OUTER_HIDE));
                }
            }
        }



// DURABILITY VISUAL DAMAGE
        boolean fullBorgDurability = FullBorgHandler.isFullBorg(data);

        // MEAT LIMBS
        float rightArmDurability = data.getDurabilityPercent(ModItems.BODYPART_RIGHTARM.get(), CyberwareSlot.RARM);

        if (rightArmDurability >= 0.0F) {
            if (rightArmDurability <= 0.25F) {
                state.addModifier(SkinModifier.rightArm(RARM_DAMAGE_MEAT_75, RARM_DAMAGE_MEAT_75, 0xFFFFFFFF));
            } else if (rightArmDurability <= 0.50F) {
                state.addModifier(SkinModifier.rightArm(RARM_DAMAGE_MEAT_50, RARM_DAMAGE_MEAT_50, 0xFFFFFFFF));
            } else if (rightArmDurability <= 0.75F) {
                state.addModifier(SkinModifier.rightArm(RARM_DAMAGE_MEAT_25, RARM_DAMAGE_MEAT_25, 0xFFFFFFFF));
            }
        }

        float leftArmDurability = data.getDurabilityPercent(ModItems.BODYPART_LEFTARM.get(), CyberwareSlot.LARM);

        if (leftArmDurability >= 0.0F) {
            if (leftArmDurability <= 0.25F) {
                state.addModifier(SkinModifier.leftArm(LARM_DAMAGE_MEAT_75, LARM_DAMAGE_MEAT_75, 0xFFFFFFFF));
            } else if (leftArmDurability <= 0.50F) {
                state.addModifier(SkinModifier.leftArm(LARM_DAMAGE_MEAT_50, LARM_DAMAGE_MEAT_50, 0xFFFFFFFF));
            } else if (leftArmDurability <= 0.75F) {
                state.addModifier(SkinModifier.leftArm(LARM_DAMAGE_MEAT_25, LARM_DAMAGE_MEAT_25, 0xFFFFFFFF));
            }
        }

        float rightLegDurability = data.getDurabilityPercent(ModItems.BODYPART_RIGHTLEG.get(), CyberwareSlot.RLEG);

        if (rightLegDurability >= 0.0F) {
            if (rightLegDurability <= 0.25F) {
                state.addModifier(SkinModifier.rightLeg(RLEG_DAMAGE_MEAT_75, RLEG_DAMAGE_MEAT_75, 0xFFFFFFFF));
            } else if (rightLegDurability <= 0.50F) {
                state.addModifier(SkinModifier.rightLeg(RLEG_DAMAGE_MEAT_50, RLEG_DAMAGE_MEAT_50, 0xFFFFFFFF));
            } else if (rightLegDurability <= 0.75F) {
                state.addModifier(SkinModifier.rightLeg(RLEG_DAMAGE_MEAT_25, RLEG_DAMAGE_MEAT_25, 0xFFFFFFFF));
            }
        }

        float leftLegDurability = data.getDurabilityPercent(ModItems.BODYPART_LEFTLEG.get(), CyberwareSlot.LLEG);

        if (leftLegDurability >= 0.0F) {
            if (leftLegDurability <= 0.25F) {
                state.addModifier(SkinModifier.leftLeg(LLEG_DAMAGE_MEAT_75, LLEG_DAMAGE_MEAT_75, 0xFFFFFFFF));
            } else if (leftLegDurability <= 0.50F) {
                state.addModifier(SkinModifier.leftLeg(LLEG_DAMAGE_MEAT_50, LLEG_DAMAGE_MEAT_50, 0xFFFFFFFF));
            } else if (leftLegDurability <= 0.75F) {
                state.addModifier(SkinModifier.leftLeg(LLEG_DAMAGE_MEAT_25, LLEG_DAMAGE_MEAT_25, 0xFFFFFFFF));
            }
        }

        // NATURAL / SYNTH SKIN
        float skinDurability = data.getDurabilityPercent(ModItems.BODYPART_SKIN.get(), CyberwareSlot.SKIN);
        float synthSkinDurability = data.getDurabilityPercent(ModItems.SKINUPGRADES_SYNTHSKIN.get(), CyberwareSlot.SKIN);

        float bodySkinDurability = -1.0F;

        if (skinDurability >= 0.0F) {
            bodySkinDurability = skinDurability;
        } else if (synthSkinDurability >= 0.0F) {
            bodySkinDurability = synthSkinDurability;
        }

        if (bodySkinDurability >= 0.0F) {
            if (bodySkinDurability <= 0.25F) {
                state.addModifier(new SkinModifier(SKIN_DAMAGE_TORSO_75, SKIN_DAMAGE_TORSO_75, 0xFFFFFFFF, false, EnumSet.noneOf(SkinModifier.HideVanilla.class)));
            } else if (bodySkinDurability <= 0.50F) {
                state.addModifier(new SkinModifier(SKIN_DAMAGE_TORSO_50, SKIN_DAMAGE_TORSO_50, 0xFFFFFFFF, false, EnumSet.noneOf(SkinModifier.HideVanilla.class)));
            } else if (bodySkinDurability <= 0.75F) {
                state.addModifier(new SkinModifier(SKIN_DAMAGE_TORSO_25, SKIN_DAMAGE_TORSO_25, 0xFFFFFFFF, false, EnumSet.noneOf(SkinModifier.HideVanilla.class)));
            }
        }

        // BRAIN
        float brainDurability = data.getDurabilityPercent(ModItems.BODYPART_BRAIN.get(), CyberwareSlot.BRAIN);

        if (brainDurability >= 0.0F && brainDurability <= 0.90F) {
            if (fullBorgDurability || data.hasSpecificItem(ModItems.BONEUPGRADES_CYBERSKULL.get(), CyberwareSlot.BONE)) {
                state.addModifier(new SkinModifier(BRAIN_DAMAGE_METAL, BRAIN_DAMAGE_METAL, 0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.HAT)));
            } else {
                state.addModifier(new SkinModifier(BRAIN_DAMAGE_MEAT, BRAIN_DAMAGE_MEAT, 0xFFFFFFFF, false, EnumSet.of(SkinModifier.HideVanilla.HAT)));
            }
        }

        // LUNGS / HEART
        if (!fullBorgDurability) {
            float lungsDurability = data.getDurabilityPercent(ModItems.BODYPART_LUNGS.get(), CyberwareSlot.LUNGS);
            float heartDurability = data.getDurabilityPercent(ModItems.BODYPART_HEART.get(), CyberwareSlot.HEART);

            float lungsHeartDurability = 1.0F;
            boolean hasLungsHeartSource = false;

            if (lungsDurability >= 0.0F) {
                lungsHeartDurability = Math.min(lungsHeartDurability, lungsDurability);
                hasLungsHeartSource = true;
            }

            if (heartDurability >= 0.0F) {
                lungsHeartDurability = Math.min(lungsHeartDurability, heartDurability);
                hasLungsHeartSource = true;
            }

            boolean hasCapacitorFrame = data.hasSpecificItem(ModItems.BONEUPGRADES_CAPACITORFRAME.get(), CyberwareSlot.BONE);
            boolean hasLinearFrame = data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE);

            if (hasLungsHeartSource) {
                if (lungsHeartDurability <= 0.25F) {
                    if (hasCapacitorFrame) {
                        state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_CFRAME_75, LUNGS_HEART_DAMAGE_CFRAME_75, 0xFFFFFFFF, false));
                    } else if (hasLinearFrame) {
                        state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_LFRAME_75, LUNGS_HEART_DAMAGE_LFRAME_75, 0xFFFFFFFF, false));
                    } else {
                        state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_MEAT_75, LUNGS_HEART_DAMAGE_MEAT_75, 0xFFFFFFFF, false));
                    }
                } else if (lungsHeartDurability <= 0.50F) {
                    if (hasCapacitorFrame) {
                        state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_CFRAME_50, LUNGS_HEART_DAMAGE_CFRAME_50, 0xFFFFFFFF, false));
                    } else if (hasLinearFrame) {
                        state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_LFRAME_50, LUNGS_HEART_DAMAGE_LFRAME_50, 0xFFFFFFFF, false));
                    } else {
                        state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_MEAT_50, LUNGS_HEART_DAMAGE_MEAT_50, 0xFFFFFFFF, false));
                    }
                } else if (lungsHeartDurability <= 0.75F) {
                    state.addModifier(new SkinModifier(LUNGS_HEART_DAMAGE_MEAT_25, LUNGS_HEART_DAMAGE_MEAT_25, 0xFFFFFFFF, false));
                }
            }
        }

        // LIVER / INTESTINES
        if (!fullBorgDurability) {
            float liverDurability = data.getDurabilityPercent(ModItems.BODYPART_LIVER.get(), CyberwareSlot.ORGANS);
            float intestinesDurability = data.getDurabilityPercent(ModItems.BODYPART_INTESTINES.get(), CyberwareSlot.ORGANS);

            float organsDurability = 1.0F;
            boolean hasOrgansSource = false;

            if (liverDurability >= 0.0F) {
                organsDurability = Math.min(organsDurability, liverDurability);
                hasOrgansSource = true;
            }

            if (intestinesDurability >= 0.0F) {
                organsDurability = Math.min(organsDurability, intestinesDurability);
                hasOrgansSource = true;
            }

            if (hasOrgansSource) {
                if (organsDurability <= 0.25F) {
                    state.addModifier(new SkinModifier(ORGANS_DAMAGE_MEAT_75, ORGANS_DAMAGE_MEAT_75, 0xFFFFFFFF, false));
                } else if (organsDurability <= 0.50F) {
                    state.addModifier(new SkinModifier(ORGANS_DAMAGE_MEAT_50, ORGANS_DAMAGE_MEAT_50, 0xFFFFFFFF, false));
                } else if (organsDurability <= 0.75F) {
                    state.addModifier(new SkinModifier(ORGANS_DAMAGE_MEAT_25, ORGANS_DAMAGE_MEAT_25, 0xFFFFFFFF, false));
                }
            }
        }

        // CYBERLIMBS
        float rightArmMetalDurability = -1.0F;

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)) {
            rightArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get(), CyberwareSlot.RARM)) {
            rightArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get(), CyberwareSlot.RARM);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get(), CyberwareSlot.RARM)) {
            rightArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get(), CyberwareSlot.RARM);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get(), CyberwareSlot.RARM)) {
            rightArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get(), CyberwareSlot.RARM);
        }

        if (rightArmMetalDurability >= 0.0F) {
            if (rightArmMetalDurability <= 0.25F) {
                state.addModifier(SkinModifier.rightArm(RARM_DAMAGE_METAL_75, RARM_DAMAGE_METAL_75, 0xFFFFFFFF));
            } else if (rightArmMetalDurability <= 0.50F) {
                state.addModifier(SkinModifier.rightArm(RARM_DAMAGE_METAL_50, RARM_DAMAGE_METAL_50, 0xFFFFFFFF));
            } else if (rightArmMetalDurability <= 0.75F) {
                state.addModifier(SkinModifier.rightArm(RARM_DAMAGE_METAL_25, RARM_DAMAGE_METAL_25, 0xFFFFFFFF));
            }
        }

        float leftArmMetalDurability = -1.0F;

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)) {
            leftArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get(), CyberwareSlot.LARM)) {
            leftArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get(), CyberwareSlot.LARM);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get(), CyberwareSlot.LARM)) {
            leftArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get(), CyberwareSlot.LARM);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get(), CyberwareSlot.LARM)) {
            leftArmMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get(), CyberwareSlot.LARM);
        }

        if (leftArmMetalDurability >= 0.0F) {
            if (leftArmMetalDurability <= 0.25F) {
                state.addModifier(SkinModifier.leftArm(LARM_DAMAGE_METAL_75, LARM_DAMAGE_METAL_75, 0xFFFFFFFF));
            } else if (leftArmMetalDurability <= 0.50F) {
                state.addModifier(SkinModifier.leftArm(LARM_DAMAGE_METAL_50, LARM_DAMAGE_METAL_50, 0xFFFFFFFF));
            } else if (leftArmMetalDurability <= 0.75F) {
                state.addModifier(SkinModifier.leftArm(LARM_DAMAGE_METAL_25, LARM_DAMAGE_METAL_25, 0xFFFFFFFF));
            }
        }

        float rightLegMetalDurability = -1.0F;

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)) {
            rightLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get(), CyberwareSlot.RLEG)) {
            rightLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get(), CyberwareSlot.RLEG);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get(), CyberwareSlot.RLEG)) {
            rightLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get(), CyberwareSlot.RLEG);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get(), CyberwareSlot.RLEG)) {
            rightLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get(), CyberwareSlot.RLEG);
        }

        if (rightLegMetalDurability >= 0.0F) {
            if (rightLegMetalDurability <= 0.25F) {
                state.addModifier(SkinModifier.rightLeg(RLEG_DAMAGE_METAL_75, RLEG_DAMAGE_METAL_75, 0xFFFFFFFF));
            } else if (rightLegMetalDurability <= 0.50F) {
                state.addModifier(SkinModifier.rightLeg(RLEG_DAMAGE_METAL_50, RLEG_DAMAGE_METAL_50, 0xFFFFFFFF));
            } else if (rightLegMetalDurability <= 0.75F) {
                state.addModifier(SkinModifier.rightLeg(RLEG_DAMAGE_METAL_25, RLEG_DAMAGE_METAL_25, 0xFFFFFFFF));
            }
        }

        float leftLegMetalDurability = -1.0F;

        if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)) {
            leftLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get(), CyberwareSlot.LLEG)) {
            leftLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get(), CyberwareSlot.LLEG);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get(), CyberwareSlot.LLEG)) {
            leftLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get(), CyberwareSlot.LLEG);
        } else if (data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get(), CyberwareSlot.LLEG)) {
            leftLegMetalDurability = data.getDurabilityPercent(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get(), CyberwareSlot.LLEG);
        }

        if (leftLegMetalDurability >= 0.0F) {
            if (leftLegMetalDurability <= 0.25F) {
                state.addModifier(SkinModifier.leftLeg(LLEG_DAMAGE_METAL_75, LLEG_DAMAGE_METAL_75, 0xFFFFFFFF));
            } else if (leftLegMetalDurability <= 0.50F) {
                state.addModifier(SkinModifier.leftLeg(LLEG_DAMAGE_METAL_50, LLEG_DAMAGE_METAL_50, 0xFFFFFFFF));
            } else if (leftLegMetalDurability <= 0.75F) {
                state.addModifier(SkinModifier.leftLeg(LLEG_DAMAGE_METAL_25, LLEG_DAMAGE_METAL_25, 0xFFFFFFFF));
            }
        }

        // METAL PLATING
        float metalPlatingDurability = data.getDurabilityPercent(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

        if (metalPlatingDurability >= 0.0F) {
            if (fullBorgDurability) {
                if (metalPlatingDurability <= 0.25F) {
                    state.addModifier(new SkinModifier(METALPLATING_DAMAGE_TORSO_75, METALPLATING_DAMAGE_TORSO_75, 0xFFFFFFFF, false));
                } else if (metalPlatingDurability <= 0.50F) {
                    state.addModifier(new SkinModifier(METALPLATING_DAMAGE_TORSO_50, METALPLATING_DAMAGE_TORSO_50, 0xFFFFFFFF, false));
                } else if (metalPlatingDurability <= 0.75F) {
                    state.addModifier(new SkinModifier(METALPLATING_DAMAGE_TORSO_25, METALPLATING_DAMAGE_TORSO_25, 0xFFFFFFFF, false));
                }
            } else {
                if (metalPlatingDurability <= 0.25F) {
                    state.addModifier(new SkinModifier(SKIN_DAMAGE_TORSO_75, SKIN_DAMAGE_TORSO_75, 0xFFFFFFFF, false, EnumSet.noneOf(SkinModifier.HideVanilla.class)));
                } else if (metalPlatingDurability <= 0.50F) {
                    state.addModifier(new SkinModifier(SKIN_DAMAGE_TORSO_50, SKIN_DAMAGE_TORSO_50, 0xFFFFFFFF, false, EnumSet.noneOf(SkinModifier.HideVanilla.class)));
                } else if (metalPlatingDurability <= 0.75F) {
                    state.addModifier(new SkinModifier(SKIN_DAMAGE_TORSO_25, SKIN_DAMAGE_TORSO_25, 0xFFFFFFFF, false, EnumSet.noneOf(SkinModifier.HideVanilla.class)));
                }
            }
        }






// MONOVISION
        if (data.hasSpecificItem(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)) {
            ResourceLocation monovision = CybereyeOpticsOverlayHandler.getShifted(player, MONOVISION, CybereyeVisualConfig.OpticsType.MONOVISION);
            ResourceLocation monovisionDyed = CybereyeOpticsOverlayHandler.getShifted(player, MONOVISION_DYE1, CybereyeVisualConfig.OpticsType.MONOVISION);
            ResourceLocation monovisionSecondaryDyed = CybereyeOpticsOverlayHandler.getShifted(player, MONOVISION_DYE2, CybereyeVisualConfig.OpticsType.MONOVISION);
            ResourceLocation monovisionHighlight = CybereyeOpticsOverlayHandler.getShifted(player, MONOVISION_HIGHLIGHT, CybereyeVisualConfig.OpticsType.MONOVISION);

            state.addModifier(new SkinModifier(monovision, monovision,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(monovisionDyed, monovisionDyed,
                        tint, false));
                state.addHighlight(new SkinHighlight(monovisionHighlight, monovisionHighlight,
                        tint, true, true));
            } else {
                state.addHighlight(new SkinHighlight(monovisionHighlight, monovisionHighlight,
                        0xFFFFFFFF, true, true));
            }

            if (data.isSecondaryDyed(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)) {
                int tint = data.secondaryDyeColor(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(monovisionSecondaryDyed, monovisionSecondaryDyed,
                        tint, false));
            }
        }

// MULTIOPTICS
        if (data.hasSpecificItem(ModItems.EYEUPGRADES_MULTIOPTICS1.get(), CyberwareSlot.EYES)) {
            ResourceLocation base = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS1, CybereyeVisualConfig.OpticsType.MULTIOPTICS_1);
            ResourceLocation dyed = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS1_DYED, CybereyeVisualConfig.OpticsType.MULTIOPTICS_1);
            ResourceLocation highlight = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS1_HIGHLIGHT, CybereyeVisualConfig.OpticsType.MULTIOPTICS_1);

            state.addModifier(new SkinModifier(base, base,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.EYEUPGRADES_MULTIOPTICS1.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MULTIOPTICS1.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(dyed, dyed,
                        tint, false));
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        tint, true, true));
            } else {
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        0xFFB00000, true, true));
            }
        }

        if (data.hasSpecificItem(ModItems.EYEUPGRADES_MULTIOPTICS2.get(), CyberwareSlot.EYES)) {
            ResourceLocation base = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS2, CybereyeVisualConfig.OpticsType.MULTIOPTICS_2);
            ResourceLocation dyed = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS2_DYED, CybereyeVisualConfig.OpticsType.MULTIOPTICS_2);
            ResourceLocation highlight = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS2_HIGHLIGHT, CybereyeVisualConfig.OpticsType.MULTIOPTICS_2);

            state.addModifier(new SkinModifier(base, base,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.EYEUPGRADES_MULTIOPTICS2.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MULTIOPTICS2.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(dyed, dyed,
                        tint, false));
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        tint, true, true));
            } else {
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        0xFFB00000, true, true));
            }
        }

        if (data.hasSpecificItem(ModItems.EYEUPGRADES_MULTIOPTICS3.get(), CyberwareSlot.EYES)) {
            ResourceLocation base = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS3, CybereyeVisualConfig.OpticsType.MULTIOPTICS_3);
            ResourceLocation dyed = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS3_DYED, CybereyeVisualConfig.OpticsType.MULTIOPTICS_3);
            ResourceLocation highlight = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS3_HIGHLIGHT, CybereyeVisualConfig.OpticsType.MULTIOPTICS_3);

            state.addModifier(new SkinModifier(base, base,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.EYEUPGRADES_MULTIOPTICS3.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MULTIOPTICS3.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(dyed, dyed,
                        tint, false));
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        tint, true, true));
            } else {
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        0xFFB00000, true, true));
            }
        }

        if (data.hasSpecificItem(ModItems.EYEUPGRADES_MULTIOPTICS4.get(), CyberwareSlot.EYES)) {
            ResourceLocation base = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS4, CybereyeVisualConfig.OpticsType.MULTIOPTICS_4);
            ResourceLocation dyed = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS4_DYED, CybereyeVisualConfig.OpticsType.MULTIOPTICS_4);
            ResourceLocation highlight = CybereyeOpticsOverlayHandler.getShifted(player, MULTIOPTICS4_HIGHLIGHT, CybereyeVisualConfig.OpticsType.MULTIOPTICS_4);

            state.addModifier(new SkinModifier(base, base,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.EYEUPGRADES_MULTIOPTICS4.get(), CyberwareSlot.EYES)) {
                int tint = data.dyeColor(ModItems.EYEUPGRADES_MULTIOPTICS4.get(), CyberwareSlot.EYES);

                state.addModifier(new SkinModifier(dyed, dyed,
                        tint, false));
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        tint, true, true));
            } else {
                state.addHighlight(new SkinHighlight(highlight, highlight,
                        0xFFB00000, true, true));
            }
        }

// GILLS
        if (data.hasSpecificItem(ModItems.WETWARE_WATERBREATHINGLUNGS.get(), CyberwareSlot.LUNGS)) {
            state.addModifier(new SkinModifier(GILLS_TEXTURE, GILLS_TEXTURE,
                    0xFFFFFFFF, false));
        }


// METAL PLATING (TRIMMED)
        if (data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
            if (data.isTrimmed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                ResourceLocation patternId = data.trimPatternId(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);
                int tint = data.trimColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                ResourceLocation wide = resolveBodyTrimOverlay(patternId, false);
                ResourceLocation slim = resolveBodyTrimOverlay(patternId, true);

                if (wide != null && slim != null) {
                    tint = (tint & 0x00FFFFFF) | 0xFF000000;
                    state.addModifier(new SkinModifier(wide, slim,
                            tint, false, false, EnumSet.noneOf(SkinModifier.HideVanilla.class), EnumSet.noneOf(HumanoidArm.class), true));
                }
            }
        }

// POLAR BEAR FUR
        if (data.hasSpecificItem(ModItems.WETWARE_POLARBEARFUR.get(), CyberwareSlot.SKIN)) {
            state.addModifier(new SkinModifier(POLAR_BEAR_FUR_TEXTURE, POLAR_BEAR_FUR_TEXTURE,
                    0xFFFFFFFF, true));
        }
// DRAGON SKIN
        if (data.hasSpecificItem(ModItems.WETWARE_DRAGONSKIN.get(), CyberwareSlot.SKIN)) {
            state.addModifier(new SkinModifier(DRAGONSKIN_WIDE, DRAGONSKIN_SLIM,
                    0xFFFFFFFF, true));
        }
// MANA ASSIMILATOR
        if (ModItems.SKINUPGRADES_MANASKIN != null) {
            if (!FullBorgHandler.isHexborg(data)) {
                if (data.hasSpecificItem(ModItems.SKINUPGRADES_MANASKIN.get(), CyberwareSlot.SKIN)) {
                    state.addModifier(new SkinModifier(MANASKIN_WIDE, MANASKIN_SLIM,
                            0xFFFFFFFF, true, false, null, null, false));
                }
            }
        }
// SPINAL INJECTOR
        if (data.hasSpecificItem(ModItems.BONEUPGRADES_SPINALINJECTOR.get(), CyberwareSlot.BONE)) {
            state.addModifier(new SkinModifier(SPINAL_INJECTOR_TEXTURE, SPINAL_INJECTOR_TEXTURE,
                    0xFFFFFFFF, false));
            SkinHighlightRender.apply(state, true, SPINAL_INJECTOR_HIGHLIGHT_TEXTURE, SPINAL_INJECTOR_HIGHLIGHT_TEXTURE,
                    0xFFFFFFFF, true);
        }
// DEPLOYABLE ELYTRA
        if (ModItems.BONEUPGRADES_ELYTRA != null) {
            if (data.hasSpecificItem(ModItems.BONEUPGRADES_ELYTRA.get(), CyberwareSlot.BONE)) {
                state.addModifier(new SkinModifier(DEPLOYABLE_ELYTRA_TEXTURE, DEPLOYABLE_ELYTRA_TEXTURE,
                        0xFFFFFFFF, false));
                SkinHighlightRender.apply(state, true, DEPLOYABLE_ELYTRA_HIGHLIGHT_TEXTURE, DEPLOYABLE_ELYTRA_HIGHLIGHT_TEXTURE,
                        0xFFFFFFFF, true);
            }
        }
// SANDEVISTAN
        if (data.hasSpecificItem(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)) {
            state.addModifier(new SkinModifier(SANDEVISTAN_TEXTURE, SANDEVISTAN_TEXTURE,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)) {
                int tint = data.dyeColor(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE);

                state.addModifier(new SkinModifier(SANDEVISTAN_DYE1_TEXTURE, SANDEVISTAN_DYE1_TEXTURE,
                        tint, false));
                state.addHighlight(new SkinHighlight(SANDEVISTAN_DYE1_TEXTURE, SANDEVISTAN_DYE1_TEXTURE,
                        tint, true, true));
            } else {
                SkinHighlightRender.apply(state, true, SANDEVISTAN_HIGHLIGHT_TEXTURE, SANDEVISTAN_HIGHLIGHT_TEXTURE,
                        0xFFFFFFFF, true);
            }

            if (data.isSecondaryDyed(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)) {
                int tint = data.secondaryDyeColor(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE);

                state.addModifier(new SkinModifier(SANDEVISTAN_DYE2_TEXTURE, SANDEVISTAN_DYE2_TEXTURE,
                        tint, false));
            }
        }

// MAGIC CATALYST
        if (data.hasSpecificItem(ModItems.ORGANSUPGRADES_MAGICCATALYST.get(), CyberwareSlot.HEART)) {
            state.addModifier(new SkinModifier(MAGIC_CATALYST, MAGIC_CATALYST,
                    0xFFFFFFFF, false));
            SkinHighlightRender.apply(state, true, MAGIC_CATALYST, MAGIC_CATALYST,
                    0xFFFFFFFF, true);
        }
// CHIPWARE SLOTS
        if (data.hasSpecificItem(ModItems.BRAINUPGRADES_CHIPWARESLOTS.get(), CyberwareSlot.BRAIN)) {
            if (data.hasChipwareShard(ModTags.Items.DATA_SHARDS)) {
                state.addModifier(new SkinModifier(CHIPWARE_ACTIVE, CHIPWARE_ACTIVE,
                        0xFFFFFFFF, false));
                state.addHighlight(new SkinHighlight(CHIPWARE_ACTIVE, CHIPWARE_ACTIVE,
                        0xFFFFFFFF, true));
            } else {
                state.addModifier(new SkinModifier(CHIPWARE_INACTIVE, CHIPWARE_INACTIVE,
                        0xFFFFFFFF, false));
                state.addHighlight(new SkinHighlight(CHIPWARE_INACTIVE, CHIPWARE_INACTIVE,
                        0xFFFFFFFF, true));
            }
        }
// LEFT REINFORCED KNUCKLES
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get(), CyberwareSlot.LARM)) {
            state.addModifier(new SkinModifier(KNUCKLES_LARM_WIDE, KNUCKLES_LARM_SLIM,
                    0xFFFFFFFF, false));
        }
// RIGHT REINFORCED KNUCKLES
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get(), CyberwareSlot.RARM)) {
            state.addModifier(new SkinModifier(KNUCKLES_RARM_WIDE, KNUCKLES_RARM_SLIM,
                    0xFFFFFFFF, false));
        }
// LEFT FIRESTARTER
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_FIRESTARTER.get(), CyberwareSlot.LARM)) {
            state.addModifier(new SkinModifier(FIRESTARTER_LARM_WIDE, FIRESTARTER_LARM_SLIM,
                    0xFFFFFFFF, false));
        }
// RIGHT FIRESTARTER
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_FIRESTARTER.get(), CyberwareSlot.RARM)) {
            state.addModifier(new SkinModifier(FIRESTARTER_RARM_WIDE, FIRESTARTER_RARM_SLIM,
                    0xFFFFFFFF, false));
        }
// LEFT FLYWHEEL
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_FLYWHEEL.get(), CyberwareSlot.LARM)) {
            state.addModifier(new SkinModifier(FLYWHEEL_LARM_WIDE, FLYWHEEL_LARM_SLIM,
                    0xFFFFFFFF, false));
        }
// RIGHT FLYWHEEL
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_FLYWHEEL.get(), CyberwareSlot.RARM)) {
            state.addModifier(new SkinModifier(FLYWHEEL_RARM_WIDE, FLYWHEEL_RARM_SLIM,
                    0xFFFFFFFF, false));
        }

// CYBERDECK
        if (data.hasSpecificItem(ModItems.BRAINUPGRADES_CYBERDECK.get(), CyberwareSlot.BRAIN)) {
            boolean primaryDyed = data.isDyed(ModItems.BRAINUPGRADES_CYBERDECK.get(), CyberwareSlot.BRAIN);
            boolean secondaryDyed = data.isSecondaryDyed(ModItems.BRAINUPGRADES_CYBERDECK.get(), CyberwareSlot.BRAIN);

            state.addModifier(new SkinModifier(CYBERDECK_TEXTURE, CYBERDECK_TEXTURE,
                    0xFFFFFFFF, false));

            if (!primaryDyed && !secondaryDyed) {
                state.addHighlight(new SkinHighlight(CYBERDECK_TEXTURE, CYBERDECK_TEXTURE,
                        0xFFFFFFFF, true));
            }

            if (primaryDyed) {
                int tint = data.dyeColor(ModItems.BRAINUPGRADES_CYBERDECK.get(), CyberwareSlot.BRAIN);

                state.addModifier(new SkinModifier(CYBERDECK_DYE1, CYBERDECK_DYE1,
                        tint, false));
            }

            if (secondaryDyed) {
                int tint = data.secondaryDyeColor(ModItems.BRAINUPGRADES_CYBERDECK.get(), CyberwareSlot.BRAIN);

                state.addModifier(new SkinModifier(CYBERDECK_DYE2, CYBERDECK_DYE2,
                        tint, false));
                state.addHighlight(new SkinHighlight(CYBERDECK_DYE2, CYBERDECK_DYE2,
                        tint, true, true));
            }
        }
// COAXIAL PORT
        if (data.hasSpecificItem(ModItems.BRAINUPGRADES_COAXIALPORT.get(), CyberwareSlot.BRAIN)) {
            boolean jackedIn = player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)
                    || player.getPersistentData().getBoolean(JACKED_IN_STANDIN_FLAG);

            ResourceLocation coaxialTexture = jackedIn ? COAXIAL_PORT_HEAD_ON : COAXIAL_PORT_HEAD_OFF;

            state.addModifier(new SkinModifier(coaxialTexture, coaxialTexture,
                    0xFFFFFFFF, false));

            if (data.isDyed(ModItems.BRAINUPGRADES_COAXIALPORT.get(), CyberwareSlot.BRAIN)) {
                int tint = data.dyeColor(ModItems.BRAINUPGRADES_COAXIALPORT.get(), CyberwareSlot.BRAIN);
                state.addModifier(new SkinModifier(COAXIAL_PORT_HEAD_DYE, COAXIAL_PORT_HEAD_DYE,
                        tint, false));
            }

            state.addHighlight(new SkinHighlight(COAXIAL_PORT_HEAD_HIGHLIGHT, COAXIAL_PORT_HEAD_HIGHLIGHT,
                    0xFFFFFFFF, true));
        }
// NEURAL PROCESSOR PORT
        if (data.hasSpecificItem(ModItems.BRAINUPGRADES_NEURALPROCESSOR.get(), CyberwareSlot.BRAIN)) {
            state.addModifier(new SkinModifier(NEURAL_PROCESSOR, NEURAL_PROCESSOR,
                    0xFFFFFFFF, false));
        }
// ULTRAVIOLENT
        if (ModCompats.isInstalled("vampirism")) {
            if (data.hasSpecificItem(ModItems.SKINUPGRADES_ULTRAVIOLENT.get(), CyberwareSlot.SKIN)) {
                state.addModifier(new SkinModifier(ULTRAVIOLENT_WIDE, ULTRAVIOLENT_SLIM,
                        0xFFFFFFFF, false));
            }
        }
// VAMPYRES
        int vampyresIndex = VampyresItem.findInstalledIndex(data);

        if (vampyresIndex >= 0) {
            boolean vampyresEnabled = data.isEnabled(CyberwareSlot.LUNGS, vampyresIndex);

            if (vampyresEnabled) {
                state.addModifier(new SkinModifier(VAMPYRES_OPEN, VAMPYRES_OPEN, 0xFFFFFFFF, false));
            } else {
                //state.addModifier(new SkinModifier(VAMPYRES_CLOSED, VAMPYRES_CLOSED, 0xFFFFFFFF, false));
            }
        }
// HEAT ENGINE
        if (data.hasSpecificItem(ModItems.ORGANSUPGRADES_HEATENGINE.get(), CyberwareSlot.ORGANS)) {
            state.addModifier(new SkinModifier(FURNACE, FURNACE,
                    0xFFFFFFFF, false));

            if (data.isHeatEngineActive()) {
                state.addModifier(new SkinModifier(FURNACE_ACTIVE, FURNACE_ACTIVE,
                        0xFFFFFFFF, false));
                state.addHighlight(new SkinHighlight(FURNACE_HIGHLIGHT, FURNACE_HIGHLIGHT,
                        0xFFFFFFFF, true));
            }
        }
// SPIDER EYES
        if (data.hasSpecificItem(ModItems.WETWARE_SPIDEREYES.get(), CyberwareSlot.EYES)) {
            state.addModifier(new SkinModifier(SPIDER_EYES, SPIDER_EYES,
                    0xFFFFFFFF, false));
            state.addHighlight(new SkinHighlight(SPIDER_EYES, SPIDER_EYES,
                    0xFFFFFFFF, true));
        }

// LEFT HEAVY KNUCKLES
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_HEAVYKNUCKLES.get(), CyberwareSlot.LARM)) {
            state.addModifier(new SkinModifier(HEAVY_KNUCKLES_LARM_WIDE, HEAVY_KNUCKLES_LARM_SLIM,
                    0xFFFFFFFF, false));
        }
// RIGHT HEAVY KNUCKLES
        if (data.hasSpecificItem(ModItems.ARMUPGRADES_HEAVYKNUCKLES.get(), CyberwareSlot.RARM)) {
            state.addModifier(new SkinModifier(HEAVY_KNUCKLES_RARM_WIDE, HEAVY_KNUCKLES_RARM_SLIM,
                    0xFFFFFFFF, false));
        }

// EXOSUIT
        ItemStack chestSlot = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestSlot.is(ModItems.EXOSUIT1)) {
            state.addModifier(new SkinModifier(EXOSUIT1_WIDE, EXOSUIT1_SLIM,
                    0xFFFFFFFF, false));
        }

        addTattooModifierIfLayer(state, player, TattooLayer.OVER_CYBERWARE);
        return state;
    }















    public static boolean isCyberspaceAvatar(AbstractClientPlayer player) {
        if (player == null) return false;
        if (player.getPersistentData().getBoolean(JACKED_IN_STANDIN_FLAG)) return false;

        return player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL);
    }

    private enum Limb {
        ARM("arm"),
        LEG("leg");

        final String id;
        Limb(String id) { this.id = id; }
    }

    private static ResourceLocation resolveTrimOverlay(ResourceLocation patternId, boolean left, Limb limb, boolean isSlimModelForArmsOrBody) {
        if (patternId == null) return null;

        String pattern = patternId.getPath(); // e.g. "flow"
        String file;

            String side = left ? "left" : "right";
            if (limb == Limb.LEG) {
                file = pattern + "_" + side + "_leg.png";
            } else {
                file = pattern + "_" + side + "_" + (isSlimModelForArmsOrBody ? "slim" : "wide") + ".png";
            }

        return ResourceLocation.fromNamespaceAndPath(
                CreateCybernetics.MODID,
                "textures/entity/trims/" + file
        );
    }

    private static boolean isSlimArms(AbstractClientPlayer player) {
        return player.getSkin().model() == PlayerSkin.Model.SLIM;
    }

    private record TrimInfo(ResourceLocation patternId, int tint) {}

    private static TrimInfo getLimbTrim(PlayerCyberwareData data, AbstractClientPlayer player,
                                        CyberwareSlot slot, net.minecraft.world.item.Item item) {
        if (!data.isTrimmed(item, slot)) return null;
        ResourceLocation patternId = data.trimPatternId(item, slot);
        int tint = data.trimColor(item, slot);
        if (patternId == null) return null;
        return new TrimInfo(patternId, tint);
    }

    private static TrimInfo getUnifiedLimbTrim(PlayerCyberwareData data, AbstractClientPlayer player) {
        TrimInfo lArm = getLimbTrim(data, player, CyberwareSlot.LARM, ModItems.BASECYBERWARE_LEFTARM.get());
        TrimInfo rArm = getLimbTrim(data, player, CyberwareSlot.RARM, ModItems.BASECYBERWARE_RIGHTARM.get());
        TrimInfo lLeg = getLimbTrim(data, player, CyberwareSlot.LLEG, ModItems.BASECYBERWARE_LEFTLEG.get());
        TrimInfo rLeg = getLimbTrim(data, player, CyberwareSlot.RLEG, ModItems.BASECYBERWARE_RIGHTLEG.get());

        if (lArm == null || rArm == null || lLeg == null || rLeg == null) return null;

        if (!lArm.patternId().equals(rArm.patternId())) return null;
        if (!lArm.patternId().equals(lLeg.patternId())) return null;
        if (!lArm.patternId().equals(rLeg.patternId())) return null;

        if (lArm.tint() != rArm.tint() || lArm.tint() != lLeg.tint() || lArm.tint() != rLeg.tint()) return null;

        return lArm;
    }

    private static ResourceLocation resolveBodyTrimOverlay(ResourceLocation patternId, boolean slim) {
        if (patternId == null) return null;

        String pattern = patternId.getPath();
        String file = slim
                ? pattern + "_slim_full_body.png"
                : pattern + "_full_body.png";

        return ResourceLocation.fromNamespaceAndPath(
                CreateCybernetics.MODID,
                "textures/entity/trims/" + file
        );
    }




    private static boolean hasMermodMoisturizerComponent(ItemStack necklace) {
        DataComponentType<?> type = getMermodNecklaceModifiersComponentType();
        if (type == null) return false;
        Object componentValue = necklace.get(type);
        if (componentValue == null) return false;
        return componentContainsMoisturizer(componentValue);
    }

    private static DataComponentType<?> getMermodNecklaceModifiersComponentType() {
        if (MERMOD_NECKLACE_MODIFIERS_COMPONENT != null) return MERMOD_NECKLACE_MODIFIERS_COMPONENT;
        DataComponentType<?> t;

        t = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("mermod", "necklace_modifiers"));
        if (t != null) return MERMOD_NECKLACE_MODIFIERS_COMPONENT = t;

        t = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("mermod", "necklace_modifiers_component_type"));
        if (t != null) return MERMOD_NECKLACE_MODIFIERS_COMPONENT = t;

        t = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("mermod", "necklace_modifiers_component"));
        if (t != null) return MERMOD_NECKLACE_MODIFIERS_COMPONENT = t;

        DataComponentType<?> best = null;
        int bestScore = -1;

        for (ResourceLocation id : BuiltInRegistries.DATA_COMPONENT_TYPE.keySet()) {
            if (!"mermod".equals(id.getNamespace())) continue;
            String p = id.getPath();

            int score = 0;
            if (p.contains("necklace")) score += 3;
            if (p.contains("modifier")) score += 3;
            if (p.contains("modifiers")) score += 2;
            if (p.contains("component")) score += 1;

            if (score > bestScore) {
                DataComponentType<?> candidate = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                if (candidate != null) {
                    best = candidate;
                    bestScore = score;
                }
            }
        }

        MERMOD_NECKLACE_MODIFIERS_COMPONENT = best;
        return best;
    }

    private static boolean componentContainsMoisturizer(Object necklaceModifiersComponent) {
        if (MERMOD_REFLECTION_FAILED) return false;
        if (!MERMOD_REFLECTION_READY) {
            try {
                MERMOD_COMPONENT_MODIFIERS_METHOD = necklaceModifiersComponent.getClass().getMethod("modifiers");

                Class<?> necklaceModifierClass = Class.forName("io.github.thatpreston.mermod.item.modifier.NecklaceModifier");
                MERMOD_MODIFIER_ID_METHOD = necklaceModifierClass.getMethod("id");
                MERMOD_REFLECTION_READY = true;
            } catch (Throwable t) {
                MERMOD_REFLECTION_FAILED = true;
                return false;
            }
        }

        try {
            Object mapObj = MERMOD_COMPONENT_MODIFIERS_METHOD.invoke(necklaceModifiersComponent);
            if (!(mapObj instanceof Map<?, ?> map)) return false;

            Collection<?> values = map.values();
            for (Object mod : values) {
                if (mod == null) continue;

                Object idObj = MERMOD_MODIFIER_ID_METHOD.invoke(mod);
                if (!(idObj instanceof String rawId)) continue;

                if (isMoisturizerId(rawId)) return true;
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static boolean isMoisturizerId(String rawId) {
        if (rawId == null || rawId.isBlank()) return false;
        String s = rawId;

        int colon = s.indexOf(':');
        if (colon >= 0 && colon + 1 < s.length()) s = s.substring(colon + 1);

        if (s.endsWith("_modifier")) s = s.substring(0, s.length() - "_modifier".length());

        return "tail_moisturizer".equals(s);
    }



    private static PlayerCyberwareData tryBuildCyberwareDataFromSnapshot(AbstractClientPlayer player) {
        if (player == null) {
            return null;
        }

        CompoundTag pd = player.getPersistentData();

        if (!pd.contains(PlayerCyberwareData.HOLO_SNAPSHOT_CYBERWARE, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag snap = pd.getCompound(PlayerCyberwareData.HOLO_SNAPSHOT_CYBERWARE);
        if (snap.isEmpty()) {
            return null;
        }

        return PlayerCyberwareData.fromSnapshotTag(snap, player.registryAccess());
    }

    private static void addTattooModifierIfLayer(SkinModifierState state, AbstractClientPlayer player, TattooLayer layer) {
        if (state == null || player == null || layer == null) {
            return;
        }

        ClientTattooModifierCollector.TattooRenderData tattoo =
                ClientTattooModifierCollector.getInstalledTattoo(player);

        if (tattoo == null || tattoo.layer() != layer) {
            return;
        }

        state.addModifier(new SkinModifier(
                tattoo.texture(),
                tattoo.texture(),
                0xFFFFFFFF,
                false
        ));
    }
}