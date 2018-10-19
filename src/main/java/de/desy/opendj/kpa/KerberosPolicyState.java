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
 *      Copyright 2017 - 2018 Deutsches Elektronen-Synchrotron DESY.
 *      Copyright 2013 Plausible Labs Cooperative, Inc.
 *      Copyright 2011 ForgeRock AS.
 */

package de.desy.opendj.kpa;

import com.sun.security.auth.module.Krb5LoginModule;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.i18n.slf4j.LocalizedLogger;
import org.forgerock.opendj.ldap.Attribute;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.LdapException;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.opendj.ldap.schema.AttributeType;

import org.opends.server.api.AuthenticationPolicy;
import org.opends.server.api.AuthenticationPolicyState;
import org.opends.server.types.*;

import javax.security.auth.Subject;
import javax.security.auth.login.*;
import java.util.*;

import static org.opends.messages.ExtensionMessages.ERR_LDAP_PTA_MAPPING_ATTRIBUTE_NOT_FOUND;
import static de.desy.opendj.kpa.OpendjKpaMessages.*;

/**
 * Kerberos authentication policy state.
 */
class KerberosPolicyState extends AuthenticationPolicyState {
    /** The parent kerberos policy. */
    private final KerberosPolicy policy;

    private static final LocalizedLogger logger = LocalizedLogger.getLoggerForThisClass();

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
    public boolean passwordMatches (final ByteString byteString) throws LdapException {

        /* Find the first available user attribute */
        String userPrincipal = null;
        for (AttributeType at : this.policy.getConfig().getMappedAttribute()) {
            final Iterable<Attribute> attributes = userEntry.getAllAttributes(at.toAttributeDescription());
            if (attributes == null)
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

            throw LdapException.newLdapException(ResultCode.INVALID_CREDENTIALS, message);
        }

	String krb5Principal = userPrincipal + "@" + this.policy.getConfig().getKrb5Realm();
        /* Kerberos module options */
        final Map<String,Object> options = new HashMap<>();
        options.put("refreshKrb5Config", "true"); // Fetch most up-to-date configuration
        options.put("useTicketCache", "true"); // Do not reference the hosts' ticket cache
        options.put("doNotPrompt", "true"); // Fetch principal et al from the shared state
        options.put("useFirstPass", "true"); // Use auth info from the shared state, do not retry

        /* Kerberos module state */
        final Map<String,Object> state = new HashMap<>();
        state.put("javax.security.auth.login.name", krb5Principal);
        state.put("javax.security.auth.login.password", byteString.toString().toCharArray());

        /* Instantiate the login context */
        final Krb5LoginModule loginModule = new Krb5LoginModule();
        loginModule.initialize(new Subject(), null, state, options);
        try {
            loginModule.login();
            loginModule.logout();
        } catch (FailedLoginException e) {
            logger.debug(KPA_LOGIN_FAILED.get(userPrincipal, e.getMessage()));
            return false;
        } catch (LoginException e) {
            logger.error(KPA_PLUGIN_FAILED.get(e.getMessage()));
            logger.traceException(e, "Failed to issue login request");
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
                return attributes.iterator().next().getNameOrOid();
            default:
                final StringBuilder builder = new StringBuilder();
                final Iterator<AttributeType> i = attributes.iterator();
                builder.append(i.next().getNameOrOid());
                while (i.hasNext())
                {
                    builder.append(", ");
                    builder.append(i.next().getNameOrOid());
                }
                return builder.toString();
        }
    }
}
