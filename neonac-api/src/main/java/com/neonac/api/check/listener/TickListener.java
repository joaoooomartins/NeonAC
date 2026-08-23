package com.neonac.api.check.listener;

import com.neonac.api.player.NeonACPlayer;

public interface TickListener {
    void onTick(NeonACPlayer player, long tick);
}
