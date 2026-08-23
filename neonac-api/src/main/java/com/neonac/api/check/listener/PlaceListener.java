package com.neonac.api.check.listener;

import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.player.NeonACPlayer;

public interface PlaceListener {
    void onPlace(NeonACPlayer player, PlayerPlacePacket packet);
}
