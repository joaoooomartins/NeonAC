package com.earac.core.check;

import com.earac.api.check.Check;
import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckConfig;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.EarACPacket;
import com.earac.api.packet.PlayerAttackPacket;
import com.earac.api.packet.PlayerDigPacket;
import com.earac.api.packet.PlayerMovePacket;
import com.earac.api.packet.PlayerPlacePacket;
import com.earac.api.packet.PlayerTransactionPacket;
import com.earac.api.packet.PlayerVelocityPacket;
import com.earac.api.player.EarACPlayer;
import com.earac.api.version.MinecraftVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all checks. Concrete checks override the relevant {@code on*}
 * hooks; the {@link CheckEngine} dispatches only the packet types they declare.
 *
 * <p>Checks never ban directly — they call {@link #flag} which routes the detection
 * through the violation system (VL accumulation, confidence, alerts, punishments).</p>
 */
public abstract class AbstractCheck implements Check {

    protected final CheckEngine engine;
    protected final String id;
    protected final String name;
    protected final CheckCategory category;
    protected final String description;
    protected final int since;
    protected final int until;

    protected boolean enabled;
    protected CheckConfig config;

    protected AbstractCheck(CheckEngine engine) {
        this.engine = engine;
        CheckInfo info = getClass().getAnnotation(CheckInfo.class);
        if (info == null) {
            throw new IllegalStateException("Check " + getClass().getName() + " missing @CheckInfo");
        }
        this.id = info.id();
        this.name = info.name();
        this.category = info.category();
        this.description = info.description();
        this.since = info.since();
        this.until = info.until();
    }

    /**
     * Called by the engine after construction. Resolves config + enabled state.
     */
    public void initialize(MinecraftVersion serverVersion) {
        this.config = new CheckConfigImpl(engine.getPlugin().getConfigManager(),
                category.getLowerCaseName(), id, serverVersion);
        this.enabled = config.getBoolean("enabled", true);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CheckCategory getCategory() {
        return category;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public CheckConfig getConfig() {
        return config;
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        if (version == MinecraftVersion.UNKNOWN) return true;
        if (since != 0 && version.getMinor() < since) return false;
        if (until != 0 && version.getMinor() > until) return false;
        return true;
    }

    @Override
    public String getBypassPermission() {
        return "earac.bypass." + category.getLowerCaseName() + "." + id;
    }

    @Override
    public void resetPlayer(String playerUuid) {
        // Optional override for checks keeping per-player state.
    }

    // ---- Detection hooks (override as needed) ----

    public void onMove(EarACPlayer player, PlayerMovePacket packet) {
    }

    public void onAttack(EarACPlayer player, PlayerAttackPacket packet) {
    }

    public void onDig(EarACPlayer player, PlayerDigPacket packet) {
    }

    public void onPlace(EarACPlayer player, PlayerPlacePacket packet) {
    }

    public void onVelocity(EarACPlayer player, PlayerVelocityPacket packet) {
    }

    public void onTransaction(EarACPlayer player, PlayerTransactionPacket packet) {
    }

    public void onTick(EarACPlayer player, long tick) {
    }

    // ---- Internal helpers ----

    /**
     * Reports a detection. Confidence in [0,1] scales the VL impact.
     */
    protected void flag(EarACPlayer player, double confidence, Map<String, ?> info) {
        if (!enabled) return;
        if (confidence <= 0.0) return;
        engine.flag(this, player, confidence, info != null ? info : new HashMap<>());
    }

    protected void flag(EarACPlayer player, double confidence, String key, Object value) {
        Map<String, Object> info = new HashMap<>();
        info.put(key, value);
        flag(player, confidence, info);
    }

    /**
     * @return true if the player is exempt (bypass perm, active exemptions or grace).
     */
    protected boolean isExempt(EarACPlayer player) {
        return engine.getExemptionManager().isExempt(player, this);
    }

    @SuppressWarnings("unchecked")
    protected <T extends EarACPacket> void record(EarACPlayer player, String key, T packet) {
        // hook for debug/metrics; default no-op to avoid allocation in production.
    }
}
