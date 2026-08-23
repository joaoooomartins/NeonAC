package com.neonac.core.check;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckConfig;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.packet.PlayerDigPacket;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.packet.PlayerTransactionPacket;
import com.neonac.api.packet.PlayerVelocityPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.MinecraftVersion;

import java.util.HashMap;
import java.util.Map;
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
        return "neonac.bypass." + category.getLowerCaseName() + "." + id;
    }

    @Override
    public void resetPlayer(String playerUuid) {
    }

    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
    }

    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
    }

    public void onDig(NeonACPlayer player, PlayerDigPacket packet) {
    }

    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
    }

    public void onVelocity(NeonACPlayer player, PlayerVelocityPacket packet) {
    }

    public void onTransaction(NeonACPlayer player, PlayerTransactionPacket packet) {
    }

    public void onTick(NeonACPlayer player, long tick) {
    }

    protected void flag(NeonACPlayer player, double confidence, Map<String, ?> info) {
        if (!enabled) return;
        if (confidence <= 0.0) return;
        engine.flag(this, player, confidence, info != null ? info : new HashMap<>());
    }

    protected void flag(NeonACPlayer player, double confidence, String key, Object value) {
        Map<String, Object> info = new HashMap<>();
        info.put(key, value);
        flag(player, confidence, info);
    }
    protected boolean isExempt(NeonACPlayer player) {
        return engine.getExemptionManager().isExempt(player, this);
    }
}
