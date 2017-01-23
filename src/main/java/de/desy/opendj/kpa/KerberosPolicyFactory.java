package de.desy.opendj.kpa;

import de.desy.opendj.kpa.server.KerberosPassThroughAuthenticationPolicyCfg;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.opendj.config.server.ConfigException;

import org.opends.server.api.AuthenticationPolicy;
import org.opends.server.api.AuthenticationPolicyFactory;
import org.opends.server.core.ServerContext;
import org.opends.server.types.InitializationException;

import java.util.List;

/**
 * Kerberos pass-through authentication policy factory.
 */
public class KerberosPolicyFactory implements AuthenticationPolicyFactory<KerberosPassThroughAuthenticationPolicyCfg> {

    private ServerContext serverContext;

    /**
     * Default constructor used by the admin framework when instantiating
     * the plugin.
     */
    public KerberosPolicyFactory () {
        super();
    }

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
            throw new ConfigException(LocalizableMessage.raw(
                    "The 'sun.security.krb5.principal' system property is set. "
                    + "Can't initialize Kerberos pass-through authentication."));
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
   /**
    * Sets the server context.
    *
    * @param serverContext
    *            The server context.
    */
   @Override
   public void setServerContext(ServerContext serverContext) {
     this.serverContext = serverContext;
   }

}
