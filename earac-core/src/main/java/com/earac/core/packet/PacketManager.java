package com.earac.core.packet;

import com.earac.core.check.CheckEngine;
import com.earac.core.player.PlayerData;
import com.earac.core.player.PlayerManager;
import com.earac.core.util.TimeUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * Bridges Bukkit events to the version-agnostic packet layer and forwards them to
 * the {@link CheckEngine}. This is the only place that touches raw Bukkit events;
 * checks never see them directly. A future ProtocolLib-backed adapter can replace
 * this class without any check changes.
 */
public final class PacketManager implements Listener {

    private final CheckEngine engine;
    private final PlayerManager playerManager;

    public PacketManager(CheckEngine engine, PlayerManager playerManager) {
        this.engine = engine;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        PlayerData pd = playerManager.get(e.getPlayer().getUniqueId());
        if (pd == null) return;
        org.bukkit.Location to = e.getTo();
        org.bukkit.Location from = e.getFrom();
        boolean hasPos = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        boolean hasRot = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        pd.update(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch(), e.getPlayer().isOnGround());
        engine.dispatchMove(pd, new BukkitMovePacket(to.getX(), to.getY(), to.getZ(),
                to.getYaw(), to.getPitch(), e.getPlayer().isOnGround(), hasPos, hasRot, e));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player attacker = (Player) e.getDamager();
        PlayerData pd = playerManager.get(attacker.getUniqueId());
        if (pd == null) return;
        Entity target = e.getEntity();
        engine.dispatchAttack(pd, new BukkitAttackPacket(target.getEntityId(), target, e));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        PlayerData pd = playerManager.get(e.getPlayer().getUniqueId());
        if (pd == null) return;
        Action a = e.getAction();
        Block block = e.getClickedBlock();
        if (block == null) return;
        BlockFace face = e.getBlockFace();
        int faceId = face.ordinal();
        if (a == Action.LEFT_CLICK_BLOCK) {
            engine.dispatchDig(pd, new BukkitDigPacket(0, block, faceId, e));
        } else if (a == Action.RIGHT_CLICK_BLOCK) {
            engine.dispatchPlace(pd, new BukkitPlacePacket(block, faceId, e));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        PlayerData pd = playerManager.get(e.getPlayer().getUniqueId());
        if (pd == null) return;
        engine.dispatchDig(pd, new BukkitDigPacket(2, e.getBlock(),
                e.getBlock().getFace(e.getPlayer().getLocation().getBlock()).ordinal(), e));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        PlayerData pd = playerManager.get(e.getPlayer().getUniqueId());
        if (pd == null) return;
        Block block = e.getBlockPlaced();
        engine.dispatchPlace(pd, new BukkitPlacePacket(block,
                block.getFace(e.getPlayer().getLocation().getBlock()).ordinal(), e));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent e) {
        Player p = e.getPlayer();
        PlayerData pd = playerManager.get(p.getUniqueId());
        if (pd == null) return;
        double vx = e.getVelocity().getX() / 20.0;
        double vy = e.getVelocity().getY() / 20.0;
        double vz = e.getVelocity().getZ() / 20.0;
        pd.applyVelocity(vx, vy, vz);
        playerManager.markVelocity(p.getUniqueId());
        engine.dispatchVelocity(pd, new BukkitVelocityPacket(vx, vy, vz, e));
    }

    /** Called once per second to advance decay + per-tick checks. */
    public void tick() {
        engine.tick();
        // touch TimeUtils to keep import meaningful for future packet-rate windows
        TimeUtils.nanos();
    }
}
