package com.neonac.api.check.listener;

import com.neonac.api.packet.PlayerVelocityPacket;
import com.neonac.api.player.NeonACPlayer;

public interface VelocityListener {
    void onVelocity(NeonACPlayer player, PlayerVelocityPacket packet);
}
