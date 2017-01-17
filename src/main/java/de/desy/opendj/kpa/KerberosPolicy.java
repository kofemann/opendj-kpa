/*
 * Author: Landon Fuller <landonf@mac68k.info>
 *
 * Copyright (c) 2013 Landon Fuller <landonf@mac68k.info>
 * Author: Landon Fuller <landonf@mac68k.info>
 *
 * Copyright (c) 2017 Tigran Mkrtchyan <tigran.mkrtchyan@desy.de>
 * All rights reserved.
 */

package de.desy.opendj.kpa;

import de.desy.opendj.kpa.server.KerberosPassThroughAuthenticationPolicyCfg;
import org.opends.server.api.AuthenticationPolicy;
import org.opends.server.api.AuthenticationPolicyState;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;

class KerberosPolicy extends AuthenticationPolicy {
    /** Backing configuration */
    private final KerberosPassThroughAuthenticationPolicyCfg config;

    /**
     * Create a new authentication policy instance.
     *
     * @param config
     */
    public KerberosPolicy (final KerberosPassThroughAuthenticationPolicyCfg config) {
        this.config = config;
    }

    @Override
    public DN getDN () {
        return config.dn();
    }

    @Override
    public AuthenticationPolicyState createAuthenticationPolicyState (Entry entry, long l) throws DirectoryException {
        return new KerberosPolicyState(this, entry);
    }

    /**
     * @return The kerberos pass-through authentication configuration.
     */
    public KerberosPassThroughAuthenticationPolicyCfg getConfig() {
        return config;
    }

}
