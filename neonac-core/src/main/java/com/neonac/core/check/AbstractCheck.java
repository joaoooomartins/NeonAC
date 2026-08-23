package com.neonac.core.check;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckConfig;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.check.listener.AttackListener;
import com.neonac.api.check.listener.DigListener;
import com.neonac.api.check.listener.MoveListener;
import com.neonac.api.check.listener.PlaceListener;
import com.neonac.api.check.listener.TickListener;
import com.neonac.api.check.listener.VelocityListener;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.packet.PlayerDigPacket;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.packet.PlayerTransactionPacket;
import com.neonac.api.packet.PlayerVelocityPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.MinecraftVersion;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCheck implements Check, MoveListener, AttackListener,
        DigListener, PlaceListener, VelocityListener, TickListener {

    protected final CheckEngine engine;
    protected final String id;
    protected final String name;
    protected final CheckCategory category;
    protected final String description;
    protected final int since;
    protected final int until;
    protected final double annotationDecay;
    protected final double annotationSetback;
    protected final boolean annotationExperimental;

    protected boolean enabled;
    protected CheckConfig config;

    protected boolean cachedExempt;
    protected boolean cachedNoSetback;
    protected long permissionCacheTick;

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
        this.annotationDecay = info.decay();
        this.annotationSetback = info.setback();
        this.annotationExperimental = info.experimental();
    }

    public void initialize(MinecraftVersion serverVersion) {
        this.config = new CheckConfigImpl(engine.getPlugin().getConfigManager(),
                category.getLowerCaseName(), id, serverVersion, this);
        this.enabled = config.getBoolean("enabled", true);
    }

    public void reload() {
        this.config = new CheckConfigImpl(engine.getPlugin().getConfigManager(),
                category.getLowerCaseName(), id, engine.getPlugin().getServerVersion(), this);
        this.enabled = config.getBoolean("enabled", true);
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public CheckCategory getCategory() { return category; }

    @Override
    public String getDescription() { return description; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public CheckConfig getConfig() { return config; }

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
    public void resetPlayer(String playerUuid) {}

    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {}
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {}
    public void onDig(NeonACPlayer player, PlayerDigPacket packet) {}
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {}
    public void onVelocity(NeonACPlayer player, PlayerVelocityPacket packet) {}
    public void onTransaction(NeonACPlayer player, PlayerTransactionPacket packet) {}
    public void onTick(NeonACPlayer player, long tick) {}

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

    public void updatePermissions(NeonACPlayer player) {
        long now = engine.getTick();
        if (now - permissionCacheTick < 20) return;
        permissionCacheTick = now;
        Player bp = (Player) player.getPlatformPlayer();
        if (bp == null) return;
        cachedExempt = bp.hasPermission(getBypassPermission())
                || bp.hasPermission("neonac.bypass." + category.getLowerCaseName())
                || bp.hasPermission("neonac.bypass");
        cachedNoSetback = bp.hasPermission("neonac.nosetback." + id)
                || bp.hasPermission("neonac.nosetback");
    }
}
