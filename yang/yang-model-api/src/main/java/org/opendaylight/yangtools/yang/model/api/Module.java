/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * This interface contains the methods for getting the data from the YANG
 * module.<br>
 * <br>
 * <i>Example of YANG module</i> <code><br>
 * {@link #getName() <b><font color="#FF0000">module</font></b>} module_name{<br>
    &nbsp;&nbsp;{@link #getYangVersion() <b><font color="#8b4513">yang-version</font></b>} "1";<br><br>

    &nbsp;&nbsp;{@link #getNamespace() <b><font color="#00FF00">namespace</font></b>} "urn:module:namespace";<br>
    &nbsp;&nbsp;{@link #getPrefix() <b><font color="#0000FF">prefix</font></b><a name="prefix"></a>} "prefix";<br><br>

    &nbsp;&nbsp;{@link #getDescription() <b><font color="#b8860b">description</font></b>} "description test";<br>
    &nbsp;&nbsp;{@link #getReference() <b><font color="#008b8b">reference</font></b>} "reference test";<br><br>

    &nbsp;&nbsp;{@link #getOrganization() <b><font color="#606060">organization</font></b>}
    "John Doe, john.doe@email.com";<br>
    &nbsp;&nbsp;{@link #getContact() <b><font color="#FF9900">contact</font></b>} "http://www.opendaylight.org/";<br>
    <br>

    &nbsp;&nbsp;{@link #getFeatures() <b><font color="#8b0000">feature</font></b>} feature-test{<br>
    &nbsp;&nbsp;&nbsp;&nbsp; description "description of some feature";<br>
    &nbsp;&nbsp;}<br>

    &nbsp;&nbsp;{@link #getNotifications() <b><font color="#b22222">notification</font></b>} notification-test;<br>
    &nbsp;&nbsp;{@link #getRpcs() <b><font color="#d2691e">rpc</font></b>} rpc-test;<br>
    <!-- &nbsp;&nbsp;{@link #getDeviations() <b><font color="#b8860b">deviation</font></b>} deviation-test;<br> -->
    &nbsp;&nbsp;{@link #getIdentities() <b><font color="#bdb76b">identity</font></b>} identity-test;<br>
    &nbsp;&nbsp;{@link #getExtensionSchemaNodes() <b><font color="#808000">extension</font></b>} extension-test;<br>


    &nbsp;&nbsp;{@link #getRevision() <b><font color="#339900">revision</font></b>} 2011-08-27 {<br>

    &nbsp;&nbsp;{@link #getImports() <b><font color="#9400d3">import</font></b>} other_module {<br>
    &nbsp;&nbsp;&nbsp;&nbsp;prefix "other_module_prefix"<br>
    &nbsp;&nbsp;&nbsp;&nbsp;revision-date 2011-08-27<br>
    &nbsp;&nbsp;}<br><br>

    &nbsp;&nbsp;container cont {<br>
    &nbsp;&nbsp;}<br>

    &nbsp;&nbsp;{@link #getAugmentations() <b><font color="#dc143c">augment</font></b>} "/cont" { ;<br>
    &nbsp;&nbsp;}<br>
    }

    </code>
 */
@Immutable
public interface Module extends DataNodeContainer, SourceStreamAware, ModuleIdentifier, NotificationNodeContainer {
    /**
     * Default semantic version of Module.
     */
    SemVer DEFAULT_SEMANTIC_VERSION = SemVer.create(0, 0, 0);

    /**
     * Returns the prefix of the module.
     *
     * @return string with the module prefix which is specified as argument of
     *         YANG {@link Module <b><font color="#0000FF">prefix</font></b>}
     *         keyword
     */
    String getPrefix();

    /**
     * Returns the YANG version. Default value is 1.
     *
     * @return string with the module YANG version which is specified as
     *         argument of YANG {@link Module <b> <font
     *         color="#8b4513">yang-version</font></b>} keyword
     */
    // FIXME: version 2.0.0: return YangVersion
    String getYangVersion();

    /**
     * Returns the module description.
     *
     * @return string with the module description which is specified as argument
     *         of YANG {@link Module <b><font
     *         color="#b8860b">description</font></b>} keyword
     */
    String getDescription();

    /**
     * Returns the module reference.
     *
     * @return string with the module reference which is specified as argument
     *         of YANG {@link Module <b><font
     *         color="#008b8b">reference</font></b>} keyword
     */
    String getReference();

    /**
     * Returns the module organization.
     *
     * @return string with the name of the organization specified in the module
     *         as the argument of YANG {@link Module <b><font
     *         color="#606060">organization</font></b>} keyword
     */
    String getOrganization();

    /**
     * Returns the module contact.
     *
     * <p>
     * The contact represents the person or persons to whom technical queries
     * concerning this module should be sent, such as their name, postal
     * address, telephone number, and electronic mail address.
     *
     * @return string with the contact data specified in the module as the
     *         argument of YANG {@link Module <b><font
     *         color="#FF9900">contact</font></b>} keyword
     */
    String getContact();

    /**
     * Returns imports which represents YANG modules which are imported to this
     * module via <b>import</b> statement.
     *
     * @return set of module imports which are specified in the module as the
     *         argument of YANG {@link Module <b><font
     *         color="#9400d3">import</font></b>} keywords.
     */
    Set<ModuleImport> getImports();

    Set<Module> getSubmodules();

    /**
     * Returns <code>FeatureDefinition</code> instances which contain data from
     * <b>feature</b> statements defined in the module.
     *
     * <p>
     * The feature is used to define a mechanism by which portions of the schema
     * are marked as conditional.
     *
     * @return feature statements in lexicographical order which are specified
     *         in the module as the argument of YANG {@link Module <b><font
     *         color="#8b0000">feature</font></b>} keywords.
     */
    Set<FeatureDefinition> getFeatures();

    /**
     * Returns <code>AugmentationSchema</code> instances which contain data from
     * <b>augment</b> statements defined in the module.
     *
     * @return set of the augmentation schema instances which are specified in
     *         the module as YANG {@link Module <b><font
     *         color="#dc143c">augment</font></b>} keyword and are
     *         lexicographically ordered
     */
    Set<AugmentationSchema> getAugmentations();

    /**
     * Returns <code>RpcDefinition</code> instances which contain data from
     * <b>rpc</b> statements defined in the module.
     *
     * @return set of the rpc definition instances which are specified in the
     *         module as YANG {@link Module <b><font
     *         color="#d2691e">rpc</font></b>} keywords and are lexicographicaly
     *         ordered
     */
    Set<RpcDefinition> getRpcs();

    /**
     * Returns <code>Deviation</code> instances which contain data from
     * <b>deviation</b> statements defined in the module.
     *
     * @return set of the deviation instances
     */
    Set<Deviation> getDeviations();

    /**
     * Returns <code>IdentitySchemaNode</code> instances which contain data from
     * <b>identity</b> statements defined in the module.
     *
     * @return set of identity schema node instances which are specified in the
     *         module as YANG {@link Module <b><font
     *         color="#bdb76b">identity</font></b>} keywords and are
     *         lexicographically ordered
     */
    Set<IdentitySchemaNode> getIdentities();

    /**
     * Returns <code>ExtensionDefinition</code> instances which contain data
     * from <b>extension</b> statements defined in the module.
     *
     * @return set of extension definition instances which are specified in the
     *         module as YANG {@link Module <b><font
     *         color="#808000">extension</font></b>} keyword and are
     *         lexicographically ordered
     */
    List<ExtensionDefinition> getExtensionSchemaNodes();

    /**
     * Returns unknown nodes defined in module.
     *
     * @return unknown nodes in lexicographical order
     */
    @Nonnull
    List<UnknownSchemaNode> getUnknownSchemaNodes();

    /**
     * Get YANG source.
     *
     * @return YANG text of this module, or null if the source is not available.
     * @deprecated Use {@link org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository#getSchemaSource(
     *             org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier, Class)} instead.
     */
    @Deprecated
    @Nullable String getSource();

    /**
     * Returns declared statement of this Module.
     *
     * @return declared statement of this Module
     */
    DeclaredStatement<?> getDeclared();
}
