package com.earac.api.packet;

/**
 * Abstract attack (use-entity) packet carrying the attacked entity id.
 */
public interface PlayerAttackPacket extends EarACPacket {

    int getTargetEntityId();

    /**
     * @return the platform entity that was attacked (resolved by the protocol layer),
     * or null if it could not be resolved. Cast to the platform entity type in checks.
     */
    Object getTargetEntity();

    /**
     * @return true if the attack was a "sword swing" (0) vs interaction (1) on modern clients.
     */
    boolean isAttack();
}
