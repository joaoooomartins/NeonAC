package com.earac.api.check;

/**
 * SPI contract. Modules (e.g. {@code earac-checks}) implement this and declare it in
 * {@code META-INF/services/com.earac.api.check.CheckProvider}. The core loads all
 * providers via {@link java.util.ServiceLoader} and calls {@link #registerChecks}.
 */
public interface CheckProvider {

    void registerChecks(CheckRegistry registry);
}
