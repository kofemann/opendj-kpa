/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2008 Sun Microsystems, Inc.
 */
package de.desy.opendj.kpa.server;



import java.util.SortedSet;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.AuthenticationPolicyCfg;
import org.opends.server.types.AttributeType;



/**
 * A server-side interface for querying Kerberos Pass Through
 * Authentication Policy settings.
 * <p>
 * An authentication policy for users whose credentials are managed by
 * an external Kerberos realm.
 */
public interface KerberosPassThroughAuthenticationPolicyCfg extends AuthenticationPolicyCfg {

  /**
   * Gets the configuration class associated with this Kerberos Pass Through Authentication Policy.
   *
   * @return Returns the configuration class associated with this Kerberos Pass Through Authentication Policy.
   */
  Class<? extends KerberosPassThroughAuthenticationPolicyCfg> configurationClass();



  /**
   * Register to be notified when this Kerberos Pass Through Authentication Policy is changed.
   *
   * @param listener
   *          The Kerberos Pass Through Authentication Policy configuration change listener.
   */
  void addKerberosPassThroughChangeListener(ConfigurationChangeListener<KerberosPassThroughAuthenticationPolicyCfg> listener);



  /**
   * Deregister an existing Kerberos Pass Through Authentication Policy configuration change listener.
   *
   * @param listener
   *          The Kerberos Pass Through Authentication Policy configuration change listener.
   */
  void removeKerberosPassThroughChangeListener(ConfigurationChangeListener<KerberosPassThroughAuthenticationPolicyCfg> listener);



  /**
   * Gets the "java-class" property.
   * <p>
   * Specifies the fully-qualified name of the Java class which
   * provides the Kerberos Pass Through Authentication Policy
   * implementation.
   *
   * @return Returns the value of the "java-class" property.
   */
  String getJavaClass();



  /**
   * Gets the "krb5-realm" property.
   * <p>
   * Specifies the kerberos realm.
   * <p>
   * Kerberos realm of for pass-trough authentication.
   *
   * @return Returns the value of the "krb5-realm" property.
   */
  String getKrb5Realm();



  /**
   * Gets the "mapped-attribute" property.
   * <p>
   * Specifies one of more attributes in the user's entry whose
   * value(s) will be used as the kerberos principal when
   * authenticating with the remote KDC.
   * <p>
   * At least one value must be provided. All values must refer to the
   * name or OID of an attribute type defined in the directory server
   * schema. At least one of the named attributes must exist in a
   * user's local entry in order for authentication to proceed. The
   * user's entry will be searched for the listed attribute types, in
   * order, and the first available attribute will be used.
   *
   * @return Returns an unmodifiable set containing the values of the "mapped-attribute" property.
   */
  SortedSet<AttributeType> getMappedAttribute();

}
