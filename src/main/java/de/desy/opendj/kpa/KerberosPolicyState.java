/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2017 Deutsches Elektronen-Synchrotron DESY.
 *      Copyright 2013 Plausible Labs Cooperative, Inc.
 *      Copyright 2011 ForgeRock AS.
 */

package de.desy.opendj.kpa;

import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.ResultCode;

import com.sun.security.auth.module.Krb5LoginModule;
import org.opends.server.api.AuthenticationPolicy;
import org.opends.server.api.AuthenticationPolicyState;
import org.opends.server.types.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import java.io.IOException;
import java.util.*;
import org.forgerock.i18n.LocalizableMessage;
import static org.opends.messages.ExtensionMessages.ERR_LDAP_PTA_MAPPING_ATTRIBUTE_NOT_FOUND;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kerberos authentication policy state.
 */
class KerberosPolicyState extends AuthenticationPolicyState {
    /** The parent kerberos policy. */
    private final KerberosPolicy policy;

    private static final Logger logger = LoggerFactory.getLogger(KerberosPolicyState.class);

    /**
     * Create a new policy state instance.
     *
     * @param policy The parent kerberos policy.
     * @param userEntry The user entry to be managed.
     */
    public KerberosPolicyState (KerberosPolicy policy, Entry userEntry) {
        super(userEntry);
        this.policy = policy;
    }

    @Override
    public AuthenticationPolicy getAuthenticationPolicy () {
        return policy;
    }

    @Override
    public boolean passwordMatches (final ByteString byteString) throws DirectoryException {
        /**
         * It's not possible to authenticate an arbitrary principal if this system property
         * is set, as it will override the principal we provide below.
         */
        if (System.getProperty("sun.security.krb5.principal") != null) {
            logger.error("The 'sun.security.krb5.principal' system property is set. This will override all " +
                "the authentication principal when performing Kerberos pass-through authentication.");
            return false;
        }

        /* Find the first available user attribute */
        String userPrincipal = null;
        for (AttributeType at : this.policy.getConfig().getMappedAttribute()) {
            final List<Attribute> attributes = userEntry.getAttribute(at);
            if (attributes == null || attributes.isEmpty())
                continue;

            for (Attribute attr : attributes) {
                if (attr.isEmpty())
                    continue;

                userPrincipal = attr.iterator().next().toString();
                break;
            }

            if (userPrincipal != null)
                break;
        }

        if (userPrincipal == null) {
            LocalizableMessage message = ERR_LDAP_PTA_MAPPING_ATTRIBUTE_NOT_FOUND.get(
                    userEntry.getName(),
                    this.policy.getConfig().dn(),
                    mappedAttributesAsString(this.policy.getConfig().getMappedAttribute()));

            throw new DirectoryException(ResultCode.INVALID_CREDENTIALS, message);
        }

	String krb5Principal = userPrincipal + "@" + this.policy.getConfig().getKrb5Realm();
        /* Kerberos module options */
        final Map<String,Object> options = new HashMap<String, Object>();
        options.put("refreshKrb5Config", "true"); // Fetch most up-to-date configuration
        options.put("useTicketCache", "true"); // Do not reference the hosts' ticket cache
        options.put("doNotPrompt", "true"); // Fetch principal et al from the shared state
        options.put("useFirstPass", "true"); // Use auth info from the shared state, do not retry

        /* Kerberos module state */
        final Map<String,Object> state = new HashMap<String, Object>();
        state.put("javax.security.auth.login.name", krb5Principal);
        state.put("javax.security.auth.login.password", byteString.toString().toCharArray());

        /* Create the noop handler */
        CallbackHandler cbh = new CallbackHandler() {
            @Override
            public void handle (Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback " + callback);
                }
            }
        };

        /* Instantiate the login context */
        final Krb5LoginModule loginModule = new Krb5LoginModule();
        loginModule.initialize(new Subject(), cbh, state, options);
        try {
            loginModule.login();
            loginModule.logout();
        } catch (FailedLoginException e) {
            logger.debug("login failed: {}", e.getMessage());
            return false;
        } catch (LoginException e) {
            logger.error("Failed to issue Kerberos login request: {}", e.getMessage());
            logger.debug("Failed to issue login request:", e);
            return false;
        }

        return true;
    }

    // This was copied from ForgeRock's LDAPPassThroughAuthenticationPolicyFactory
    private static String mappedAttributesAsString (final Collection<AttributeType> attributes) {
        switch (attributes.size()) {
            case 0:
                return "";
            case 1:
                return attributes.iterator().next().getNameOrOID();
            default:
                final StringBuilder builder = new StringBuilder();
                final Iterator<AttributeType> i = attributes.iterator();
                builder.append(i.next().getNameOrOID());
                while (i.hasNext())
                {
                    builder.append(", ");
                    builder.append(i.next().getNameOrOID());
                }
                return builder.toString();
        }
    }
}
