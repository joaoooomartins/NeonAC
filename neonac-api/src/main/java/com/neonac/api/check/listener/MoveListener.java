package com.neonac.api.check.listener;

import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;

public interface MoveListener {
    void onMove(NeonACPlayer player, PlayerMovePacket packet);
}
