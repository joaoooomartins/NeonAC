package com.neonac.api.check.listener;

import com.neonac.api.packet.PlayerDigPacket;
import com.neonac.api.player.NeonACPlayer;

public interface DigListener {
    void onDig(NeonACPlayer player, PlayerDigPacket packet);
}
