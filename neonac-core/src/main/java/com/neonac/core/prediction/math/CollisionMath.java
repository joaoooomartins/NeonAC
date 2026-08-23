package com.neonac.core.prediction.math;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class CollisionMath {

    private static final Set<Material> NON_SOLID = EnumSet.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.WATER, Material.LAVA,
            Material.TALL_GRASS,
            Material.FIRE, Material.SOUL_FIRE,
            Material.REDSTONE_WIRE, Material.TRIPWIRE,
            Material.TRIPWIRE_HOOK, Material.STRING,
            Material.TORCH, Material.REDSTONE_TORCH,
            Material.WALL_TORCH, Material.REDSTONE_WALL_TORCH,
            Material.FLOWER_POT, Material.POPPY, Material.DANDELION,
            Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET,
            Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.OXEYE_DAISY,
            Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH,
            Material.PEONY, Material.OAK_SAPLING, Material.SPRUCE_SAPLING,
            Material.BIRCH_SAPLING, Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING,
            Material.DEAD_BUSH, Material.DANDELION, Material.POPPY,
            Material.SUGAR_CANE, Material.NETHER_WART,
            Material.MELON_STEM, Material.ATTACHED_MELON_STEM,
            Material.PUMPKIN_STEM, Material.ATTACHED_PUMPKIN_STEM,
            Material.BEETROOTS, Material.WHEAT, Material.CARROTS,
            Material.POTATOES, Material.NETHER_WART,
            Material.COCOA, Material.BAMBOO_SAPLING
    );

    private static final Set<Material> FULL_BLOCKS = EnumSet.of(
            Material.STONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE,
            Material.DIRT, Material.COARSE_DIRT, Material.PODZOL,
            Material.COBBLESTONE, Material.OAK_PLANKS, Material.SPRUCE_PLANKS,
            Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS,
            Material.DARK_OAK_PLANKS, Material.BEDROCK,
            Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE,
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.GLASS, Material.LAPIS_BLOCK, Material.SANDSTONE,
            Material.GOLD_BLOCK, Material.IRON_BLOCK,
            Material.BRICK, Material.TNT, Material.BOOKSHELF,
            Material.OBSIDIAN, Material.DIAMOND_BLOCK, Material.CRAFTING_TABLE,
            Material.FURNACE, Material.CHEST, Material.JUKEBOX,
            Material.BREWING_STAND, Material.NETHERRACK, Material.SOUL_SAND,
            Material.GLOWSTONE, Material.JACK_O_LANTERN,
            Material.STONE_BRICKS, Material.MELON, Material.NETHER_BRICK,
            Material.END_STONE, Material.EMERALD_BLOCK,
            Material.QUARTZ_BLOCK, Material.NETHER_QUARTZ_ORE,
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
            Material.COAL_BLOCK, Material.PACKED_ICE,
            Material.PRISMARINE, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE,
            Material.HAY_BLOCK, Material.TERRACOTTA,
            Material.COAL_BLOCK, Material.PACKED_ICE,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG
    );

    private CollisionMath() {}

    public static boolean isSolid(Material mat) {
        if (mat == null || mat == Material.AIR) return false;
        if (NON_SOLID.contains(mat)) return false;
        if (mat.isBlock() && mat.isSolid()) return true;
        return FULL_BLOCKS.contains(mat);
    }

    public static boolean isFullBlock(Material mat) {
        return FULL_BLOCKS.contains(mat) || (mat.isBlock() && mat.isSolid());
    }

    public static double getBlockFriction(Material mat) {
        if (mat == null) return 0.6;
        switch (mat) {
            case ICE: case PACKED_ICE: return 0.98;
            case BLUE_ICE: return 0.989;
            case SLIME_BLOCK: return 0.8;
            case HONEY_BLOCK: return 0.4;
            case SOUL_SAND: case SOUL_SOIL: return 0.4;
            case COBWEB: return 0.1;
            default: return mat.isBlock() && mat.isSolid() ? 0.6 : 0.91;
        }
    }

    public static double getBlockBounceRestitution(Material mat) {
        if (mat == null) return 0;
        switch (mat) {
            case SLIME_BLOCK: return 0.8;
            case HONEY_BLOCK: return 0.5;
            default: return 0;
        }
    }

    public static List<CollisionBox> getCollisionBoxes(World world, CollisionBox area) {
        List<CollisionBox> boxes = new ArrayList<>();
        int minX = (int) Math.floor(area.minX) - 1;
        int maxX = (int) Math.floor(area.maxX) + 1;
        int minY = (int) Math.floor(area.minY) - 1;
        int maxY = (int) Math.floor(area.maxY) + 1;
        int minZ = (int) Math.floor(area.minZ) - 1;
        int maxZ = (int) Math.floor(area.maxZ) + 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material mat = block.getType();
                    if (isFullBlock(mat)) {
                        boxes.add(new CollisionBox(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return boxes;
    }

    public static Vector3d collide(CollisionBox playerBox, double dx, double dy, double dz,
                                    List<CollisionBox> blockBoxes) {
        if (dx == 0 && dy == 0 && dz == 0) return new Vector3d();

        double x = dx, y = dy, z = dz;
        CollisionBox moved = playerBox.copy();

        for (CollisionBox bb : blockBoxes) {
            y = bb.collideY(moved, y);
        }
        moved.offset(0, y, 0);

        for (CollisionBox bb : blockBoxes) {
            x = bb.collideX(moved, x);
        }
        moved.offset(x, 0, 0);

        for (CollisionBox bb : blockBoxes) {
            z = bb.collideZ(moved, z);
        }

        return new Vector3d(x, y, z);
    }
}
