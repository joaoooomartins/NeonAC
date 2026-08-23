package com.neonac.api.check.listener;

import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;

public interface AttackListener {
    void onAttack(NeonACPlayer player, PlayerAttackPacket packet);
}
