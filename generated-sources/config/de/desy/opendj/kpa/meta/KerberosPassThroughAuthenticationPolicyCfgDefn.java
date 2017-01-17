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
package de.desy.opendj.kpa.meta;



import de.desy.opendj.kpa.client.KerberosPassThroughAuthenticationPolicyCfgClient;
import de.desy.opendj.kpa.server.KerberosPassThroughAuthenticationPolicyCfg;
import java.util.Collection;
import java.util.SortedSet;
import org.opends.server.admin.AdministratorAction;
import org.opends.server.admin.AttributeTypePropertyDefinition;
import org.opends.server.admin.ClassPropertyDefinition;
import org.opends.server.admin.DefaultBehaviorProvider;
import org.opends.server.admin.DefinedDefaultBehaviorProvider;
import org.opends.server.admin.ManagedObjectAlreadyExistsException;
import org.opends.server.admin.ManagedObjectDefinition;
import org.opends.server.admin.PropertyOption;
import org.opends.server.admin.PropertyProvider;
import org.opends.server.admin.StringPropertyDefinition;
import org.opends.server.admin.Tag;
import org.opends.server.admin.UndefinedDefaultBehaviorProvider;
import org.opends.server.admin.client.AuthorizationException;
import org.opends.server.admin.client.CommunicationException;
import org.opends.server.admin.client.ConcurrentModificationException;
import org.opends.server.admin.client.ManagedObject;
import org.opends.server.admin.client.MissingMandatoryPropertiesException;
import org.opends.server.admin.client.OperationRejectedException;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.server.ServerManagedObject;
import org.opends.server.admin.std.meta.AuthenticationPolicyCfgDefn;
import org.opends.server.admin.std.server.AuthenticationPolicyCfg;
import org.opends.server.types.AttributeType;
import org.opends.server.types.DN;
import org.opends.server.types.LDAPException;



/**
 * An interface for querying the Kerberos Pass Through Authentication
 * Policy managed object definition meta information.
 * <p>
 * An authentication policy for users whose credentials are managed by
 * an external Kerberos realm.
 */
public final class KerberosPassThroughAuthenticationPolicyCfgDefn extends ManagedObjectDefinition<KerberosPassThroughAuthenticationPolicyCfgClient, KerberosPassThroughAuthenticationPolicyCfg> {

  /** The singleton configuration definition instance. */
  private static final KerberosPassThroughAuthenticationPolicyCfgDefn INSTANCE = new KerberosPassThroughAuthenticationPolicyCfgDefn();



  /** The "java-class" property definition. */
  private static final ClassPropertyDefinition PD_JAVA_CLASS;



  /** The "krb5-realm" property definition. */
  private static final StringPropertyDefinition PD_KRB5_REALM;



  /** The "mapped-attribute" property definition. */
  private static final AttributeTypePropertyDefinition PD_MAPPED_ATTRIBUTE;



  /** Build the "java-class" property definition. */
  static {
      ClassPropertyDefinition.Builder builder = ClassPropertyDefinition.createBuilder(INSTANCE, "java-class");
      builder.setOption(PropertyOption.MANDATORY);
      builder.setOption(PropertyOption.ADVANCED);
      builder.setAdministratorAction(new AdministratorAction(AdministratorAction.Type.COMPONENT_RESTART, INSTANCE, "java-class"));
      DefaultBehaviorProvider<String> provider = new DefinedDefaultBehaviorProvider<String>("de.desy.opendj.kpa.KerberosPolicyFactory");
      builder.setDefaultBehaviorProvider(provider);
      builder.addInstanceOf("org.opends.server.api.AuthenticationPolicyFactory");
      PD_JAVA_CLASS = builder.getInstance();
      INSTANCE.registerPropertyDefinition(PD_JAVA_CLASS);
  }



  /** Build the "krb5-realm" property definition. */
  static {
      StringPropertyDefinition.Builder builder = StringPropertyDefinition.createBuilder(INSTANCE, "krb5-realm");
      builder.setOption(PropertyOption.MANDATORY);
      builder.setAdministratorAction(new AdministratorAction(AdministratorAction.Type.NONE, INSTANCE, "krb5-realm"));
      builder.setDefaultBehaviorProvider(new UndefinedDefaultBehaviorProvider<String>());
      builder.setPattern("^.+$", "REALM");
      PD_KRB5_REALM = builder.getInstance();
      INSTANCE.registerPropertyDefinition(PD_KRB5_REALM);
  }



  /** Build the "mapped-attribute" property definition. */
  static {
      AttributeTypePropertyDefinition.Builder builder = AttributeTypePropertyDefinition.createBuilder(INSTANCE, "mapped-attribute");
      builder.setOption(PropertyOption.MULTI_VALUED);
      builder.setOption(PropertyOption.MANDATORY);
      builder.setAdministratorAction(new AdministratorAction(AdministratorAction.Type.NONE, INSTANCE, "mapped-attribute"));
      builder.setDefaultBehaviorProvider(new UndefinedDefaultBehaviorProvider<AttributeType>());
      PD_MAPPED_ATTRIBUTE = builder.getInstance();
      INSTANCE.registerPropertyDefinition(PD_MAPPED_ATTRIBUTE);
  }



  // Register the tags associated with this managed object definition.
  static {
    INSTANCE.registerTag(Tag.valueOf("user-management"));
  }



  /**
   * Get the Kerberos Pass Through Authentication Policy configuration
   * definition singleton.
   *
   * @return Returns the Kerberos Pass Through Authentication Policy
   *         configuration definition singleton.
   */
  public static KerberosPassThroughAuthenticationPolicyCfgDefn getInstance() {
    return INSTANCE;
  }



  /**
   * Private constructor.
   */
  private KerberosPassThroughAuthenticationPolicyCfgDefn() {
    super("kerberos-pass-through-authentication-policy", AuthenticationPolicyCfgDefn.getInstance());
  }



  /** {@inheritDoc} */
  public KerberosPassThroughAuthenticationPolicyCfgClient createClientConfiguration(
            ManagedObject<? extends KerberosPassThroughAuthenticationPolicyCfgClient> impl) {
    return new KerberosPassThroughAuthenticationPolicyCfgClientImpl(impl);
  }



  /** {@inheritDoc} */
  public KerberosPassThroughAuthenticationPolicyCfg createServerConfiguration(
            ServerManagedObject<? extends KerberosPassThroughAuthenticationPolicyCfg> impl) {
    return new KerberosPassThroughAuthenticationPolicyCfgServerImpl(impl);
  }



  /** {@inheritDoc} */
  public Class<KerberosPassThroughAuthenticationPolicyCfg> getServerConfigurationClass() {
    return KerberosPassThroughAuthenticationPolicyCfg.class;
  }



  /**
   * Get the "java-class" property definition.
   * <p>
   * Specifies the fully-qualified name of the Java class which
   * provides the Kerberos Pass Through Authentication Policy
   * implementation.
   *
   * @return Returns the "java-class" property definition.
   */
  public ClassPropertyDefinition getJavaClassPropertyDefinition() {
    return PD_JAVA_CLASS;
  }



  /**
   * Get the "krb5-realm" property definition.
   * <p>
   * Specifies the kerberos realm.
   * <p>
   * Kerberos realm of for pass-trough authentication.
   *
   * @return Returns the "krb5-realm" property definition.
   */
  public StringPropertyDefinition getKrb5RealmPropertyDefinition() {
    return PD_KRB5_REALM;
  }



  /**
   * Get the "mapped-attribute" property definition.
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
   * @return Returns the "mapped-attribute" property definition.
   */
  public AttributeTypePropertyDefinition getMappedAttributePropertyDefinition() {
    return PD_MAPPED_ATTRIBUTE;
  }



  /**
   * Managed object client implementation.
   */
  private static class KerberosPassThroughAuthenticationPolicyCfgClientImpl implements
    KerberosPassThroughAuthenticationPolicyCfgClient {

    /** Private implementation. */
    private ManagedObject<? extends KerberosPassThroughAuthenticationPolicyCfgClient> impl;



    /** Private constructor. */
    private KerberosPassThroughAuthenticationPolicyCfgClientImpl(
        ManagedObject<? extends KerberosPassThroughAuthenticationPolicyCfgClient> impl) {
      this.impl = impl;
    }



    /** {@inheritDoc} */
    public String getJavaClass() {
      return impl.getPropertyValue(INSTANCE.getJavaClassPropertyDefinition());
    }



    /** {@inheritDoc} */
    public void setJavaClass(String value) {
      impl.setPropertyValue(INSTANCE.getJavaClassPropertyDefinition(), value);
    }



    /** {@inheritDoc} */
    public String getKrb5Realm() {
      return impl.getPropertyValue(INSTANCE.getKrb5RealmPropertyDefinition());
    }



    /** {@inheritDoc} */
    public void setKrb5Realm(String value) {
      impl.setPropertyValue(INSTANCE.getKrb5RealmPropertyDefinition(), value);
    }



    /** {@inheritDoc} */
    public SortedSet<AttributeType> getMappedAttribute() {
      return impl.getPropertyValues(INSTANCE.getMappedAttributePropertyDefinition());
    }



    /** {@inheritDoc} */
    public void setMappedAttribute(Collection<AttributeType> values) {
      impl.setPropertyValues(INSTANCE.getMappedAttributePropertyDefinition(), values);
    }



    /** {@inheritDoc} */
    public ManagedObjectDefinition<? extends KerberosPassThroughAuthenticationPolicyCfgClient, ? extends KerberosPassThroughAuthenticationPolicyCfg> definition() {
      return INSTANCE;
    }



    /** {@inheritDoc} */
    public PropertyProvider properties() {
      return impl;
    }



    /** {@inheritDoc} */
    public void commit() throws ManagedObjectAlreadyExistsException,
              MissingMandatoryPropertiesException, ConcurrentModificationException,
              OperationRejectedException, AuthorizationException, CommunicationException {
      impl.commit();
    }



    /** {@inheritDoc} */
    public String toString() {
      return impl.toString();
    }
  }



  /**
   * Managed object server implementation.
   */
  private static class KerberosPassThroughAuthenticationPolicyCfgServerImpl implements
    KerberosPassThroughAuthenticationPolicyCfg {

    /** Private implementation. */
    private ServerManagedObject<? extends KerberosPassThroughAuthenticationPolicyCfg> impl;

    /** The value of the "java-class" property. */
    private final String pJavaClass;

    /** The value of the "krb5-realm" property. */
    private final String pKrb5Realm;

    /** The value of the "mapped-attribute" property. */
    private final SortedSet<AttributeType> pMappedAttribute;



    /** Private constructor. */
    private KerberosPassThroughAuthenticationPolicyCfgServerImpl(ServerManagedObject<? extends KerberosPassThroughAuthenticationPolicyCfg> impl) {
      this.impl = impl;
      this.pJavaClass = impl.getPropertyValue(INSTANCE.getJavaClassPropertyDefinition());
      this.pKrb5Realm = impl.getPropertyValue(INSTANCE.getKrb5RealmPropertyDefinition());
      this.pMappedAttribute = impl.getPropertyValues(INSTANCE.getMappedAttributePropertyDefinition());
    }



    /** {@inheritDoc} */
    public void addKerberosPassThroughChangeListener(
              ConfigurationChangeListener<KerberosPassThroughAuthenticationPolicyCfg> listener) {
      impl.registerChangeListener(listener);
    }



    /** {@inheritDoc} */
    public void removeKerberosPassThroughChangeListener(
        ConfigurationChangeListener<KerberosPassThroughAuthenticationPolicyCfg> listener) {
      impl.deregisterChangeListener(listener);
    }
    /** {@inheritDoc} */
    public void addChangeListener(
        ConfigurationChangeListener<AuthenticationPolicyCfg> listener) {
      impl.registerChangeListener(listener);
    }



    /** {@inheritDoc} */
    public void removeChangeListener(
        ConfigurationChangeListener<AuthenticationPolicyCfg> listener) {
      impl.deregisterChangeListener(listener);
    }



    /** {@inheritDoc} */
    public String getJavaClass() {
      return pJavaClass;
    }



    /** {@inheritDoc} */
    public String getKrb5Realm() {
      return pKrb5Realm;
    }



    /** {@inheritDoc} */
    public SortedSet<AttributeType> getMappedAttribute() {
      return pMappedAttribute;
    }



    /** {@inheritDoc} */
    public Class<? extends KerberosPassThroughAuthenticationPolicyCfg> configurationClass() {
      return KerberosPassThroughAuthenticationPolicyCfg.class;
    }



    /** {@inheritDoc} */
    public DN dn() {
      return impl.getDN();
    }



    /** {@inheritDoc} */
    public String toString() {
      return impl.toString();
    }
  }
}
