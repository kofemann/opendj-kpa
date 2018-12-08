package de.desy.opendj.kpa;

import de.desy.opendj.kpa.server.KerberosPassThroughAuthenticationPolicyCfg;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.opendj.config.server.ConfigException;

import org.opends.server.api.AuthenticationPolicy;
import org.opends.server.api.AuthenticationPolicyFactory;
import org.opends.server.types.InitializationException;

import java.util.List;

import static de.desy.opendj.kpa.OpendjKpaMessages.KPA_PLUGIN_INIT_FAILED;

/**
 * Kerberos pass-through authentication policy factory.
 */
public final class KerberosPolicyFactory implements AuthenticationPolicyFactory<KerberosPassThroughAuthenticationPolicyCfg> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationPolicy createAuthenticationPolicy (KerberosPassThroughAuthenticationPolicyCfg config)
            throws ConfigException, InitializationException {
        /**
         * It's not possible to authenticate an arbitrary principal if this
         * system property is set, as it will override the principal we provide
         * below.
         */
        if (System.getProperty("sun.security.krb5.principal") != null) {
            throw new ConfigException(KPA_PLUGIN_INIT_FAILED.get());
        }
        return new KerberosPolicy(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationAcceptable(KerberosPassThroughAuthenticationPolicyCfg kerberosPluginCfg, List<LocalizableMessage> messages) {
        return true;
    }

}
