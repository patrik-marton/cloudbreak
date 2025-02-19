package com.sequenceiq.freeipa.client;

import java.util.Set;

import com.sequenceiq.freeipa.client.model.Config;

public class FreeIpaCapabilities {

    // Attribute name to store users passwords (enabled through FreeIPA plugin)
    private static final String CDP_USER_ATTRIBUTE = "cdpUserAttr";

    private FreeIpaCapabilities() {
    }

    public static boolean hasSetPasswordHashSupport(Config ipaConfig) {
        Set<String> ipauserobjectclasses = ipaConfig.getIpauserobjectclasses();
        return ipauserobjectclasses != null && ipauserobjectclasses.contains(CDP_USER_ATTRIBUTE);
    }
}
