package com.perigrine3.createcybernetics.common.scheduling;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TickableCyberwareClassifier {

    public record ClassTraits(boolean tickable, boolean energyNode) {}

    private static final String DEFAULT_ORGANS_PACKAGE = "com.perigrine3.createcybernetics.item.organs.";

    private static final Set<String> NON_TICKING_CLASS_NAMES = Set.of(
            "com.perigrine3.createcybernetics.item.cyberware.brain.CorticalStackItem",
            "com.perigrine3.createcybernetics.item.cyberware.brain.NeedlecasterItem",
            "com.perigrine3.createcybernetics.item.cyberware.eyes.NavigationChipItem",
            "com.perigrine3.createcybernetics.item.cyberware.brain.ChipwareSlotsItem",
            "com.perigrine3.createcybernetics.item.cyberware.brain.RamdeckItem",
            "com.perigrine3.createcybernetics.item.cyberware.muscle.WiredReflexesItem",
            "com.perigrine3.createcybernetics.item.cyberware.wetware.BlubberItem"
    );

    private static final ConcurrentMap<Class<?>, ClassTraits> TRAIT_CACHE = new ConcurrentHashMap<>();

    private TickableCyberwareClassifier() {}

    public static boolean isTickable(ICyberwareItem item) {
        if (item == null) {
            return false;
        }
        return getTraits(item.getClass()).tickable();
    }

    public static boolean isEnergyNode(ICyberwareItem item) {
        if (item == null) {
            return false;
        }
        return getTraits(item.getClass()).energyNode();
    }

    public static ClassTraits getTraits(Class<?> clazz) {
        return TRAIT_CACHE.computeIfAbsent(clazz, TickableCyberwareClassifier::computeTraits);
    }

    public static CompletableFuture<Void> prewarmAsync(Iterable<Class<?>> classes) {
        return CompletableFuture.runAsync(() -> {
            for (Class<?> clazz : classes) {
                if (clazz != null && ICyberwareItem.class.isAssignableFrom(clazz)) {
                    getTraits(clazz);
                }
            }
        });
    }

    private static ClassTraits computeTraits(Class<?> clazz) {
        String className = clazz.getName();
        boolean tickable = !className.startsWith(DEFAULT_ORGANS_PACKAGE)
                && !NON_TICKING_CLASS_NAMES.contains(className)
                && overridesAnyTickMethod(clazz);
        boolean energyNode = overridesAnyEnergyMethod(clazz);
        return new ClassTraits(tickable, energyNode);
    }

    private static boolean overridesAnyTickMethod(Class<?> clazz) {
        return isEffectiveTickOverride(clazz, "onTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class, int.class)
                || isEffectiveTickOverride(clazz, "onTick", LivingEntity.class)
                || isEffectiveTickOverride(clazz, "onTick", Player.class, ItemStack.class, CyberwareSlot.class, int.class)
                || isEffectiveTickOverride(clazz, "onTick", Player.class);
    }

    private static boolean overridesAnyEnergyMethod(Class<?> clazz) {
        return isOverridden(clazz, "getEnergyGeneratedPerTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyGeneratedPerTick", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "shouldGenerateEnergyThisTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "shouldGenerateEnergyThisTick", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyGeneratedThisTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyGeneratedThisTick", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyUsedPerTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyUsedPerTick", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "shouldUseEnergyThisTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "shouldUseEnergyThisTick", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyUsedThisTick", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyUsedThisTick", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyCapacity", LivingEntity.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyCapacity", Player.class, ItemStack.class, CyberwareSlot.class)
                || isOverridden(clazz, "getEnergyActivationCost", LivingEntity.class, ItemStack.class, CyberwareSlot.class);
    }

    private static boolean isOverridden(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            return method.getDeclaringClass() != ICyberwareItem.class;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static boolean isEffectiveTickOverride(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            Class<?> declaring = method.getDeclaringClass();
            if (declaring == ICyberwareItem.class) {
                return false;
            }
            byte[] code = extractMethodBytecode(declaring, methodName, buildMethodDescriptor(parameterTypes));
            if (code == null) {
                return true;
            }
            if (!isNoOpTickBytecode(code)) {
                return true;
            }
            return isSuperDelegation(code) && delegatesToTickingSuperclass(declaring, methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static boolean isNoOpTickBytecode(byte[] code) {
        int len = code.length;
        if (len == 0) {
            return true;
        }
        if (len == 1 && (code[0] & 0xFF) == 0xB1) {
            return true;
        }
        return isSuperDelegation(code) || isClientGuardOnly(code);
    }

    private static boolean isSuperDelegation(byte[] code) {
        int len = code.length;
        if (len == 6) {
            return (code[0] & 0xFF) == 0x2A
                    && (code[1] & 0xFF) == 0x2B
                    && (code[2] & 0xFF) == 0xB7
                    && (code[5] & 0xFF) == 0xB1;
        }
        return len == 10
                && (code[0] & 0xFF) == 0x2A
                && (code[6] & 0xFF) == 0xB7
                && (code[9] & 0xFF) == 0xB1;
    }

    private static boolean delegatesToTickingSuperclass(Class<?> declaring, String methodName, Class<?>[] parameterTypes) {
        Class<?> parent = declaring.getSuperclass();
        return parent != null
                && ICyberwareItem.class.isAssignableFrom(parent)
                && isEffectiveTickOverride(parent, methodName, parameterTypes);
    }

    private static boolean isClientGuardOnly(byte[] code) {
        if (code.length != 12) {
            return false;
        }
        int sideAccess = code[4] & 0xFF;
        int branch = code[7] & 0xFF;
        return (code[0] & 0xFF) == 0x2B
                && (code[1] & 0xFF) == 0xB6
                && (sideAccess == 0xB4 || sideAccess == 0xB6)
                && (branch == 0x99 || branch == 0x9A)
                && (code[10] & 0xFF) == 0xB1
                && (code[11] & 0xFF) == 0xB1;
    }

    private static String buildMethodDescriptor(Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> param : parameterTypes) {
            if (param == int.class) {
                sb.append('I');
            } else {
                sb.append('L').append(param.getName().replace('.', '/')).append(';');
            }
        }
        sb.append(")V");
        return sb.toString();
    }

    private static byte[] extractMethodBytecode(Class<?> declaringClass, String targetName, String targetDesc) {
        String resourcePath = declaringClass.getName().replace('.', '/') + ".class";
        ClassLoader loader = declaringClass.getClassLoader();
        try (InputStream raw = loader != null
                ? loader.getResourceAsStream(resourcePath)
                : ClassLoader.getSystemResourceAsStream(resourcePath)) {
            if (raw == null) {
                return null;
            }
            DataInputStream in = new DataInputStream(raw);
            if (in.readInt() != 0xCAFEBABE) {
                return null;
            }
            in.readUnsignedShort();
            in.readUnsignedShort();

            int cpCount = in.readUnsignedShort();
            String[] utf8 = new String[cpCount];
            for (int i = 1; i < cpCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1 -> utf8[i] = in.readUTF();
                    case 3, 4, 9, 10, 11, 12, 17, 18 -> in.skipBytes(4);
                    case 5, 6 -> {
                        in.skipBytes(8);
                        i++;
                    }
                    case 7, 8, 16, 19, 20 -> in.skipBytes(2);
                    case 15 -> in.skipBytes(3);
                    default -> {
                        return null;
                    }
                }
            }

            in.skipBytes(6);
            int interfacesCount = in.readUnsignedShort();
            in.skipBytes(interfacesCount * 2);

            int fieldsCount = in.readUnsignedShort();
            for (int i = 0; i < fieldsCount; i++) {
                in.skipBytes(6);
                int attrCount = in.readUnsignedShort();
                for (int a = 0; a < attrCount; a++) {
                    in.skipBytes(2);
                    int attrLen = in.readInt();
                    in.skipBytes(attrLen);
                }
            }

            int methodsCount = in.readUnsignedShort();
            for (int i = 0; i < methodsCount; i++) {
                in.skipBytes(2);
                int nameIdx = in.readUnsignedShort();
                int descIdx = in.readUnsignedShort();
                int attrCount = in.readUnsignedShort();
                boolean matches = targetName.equals(utf8[nameIdx]) && targetDesc.equals(utf8[descIdx]);
                for (int a = 0; a < attrCount; a++) {
                    int attrNameIdx = in.readUnsignedShort();
                    int attrLen = in.readInt();
                    if (matches && "Code".equals(utf8[attrNameIdx])) {
                        in.skipBytes(4);
                        int codeLen = in.readInt();
                        byte[] code = new byte[codeLen];
                        in.readFully(code);
                        return code;
                    } else {
                        in.skipBytes(attrLen);
                    }
                }
            }
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
