/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;

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
public interface Module extends DataNodeContainer, DocumentedNode, Immutable, NotificationNodeContainer,
        NamespaceRevisionAware {
    /**
     * Returns the name of the module which is specified as argument of YANG
     * {@link Module <b><font color="#FF0000">module</font></b>} keyword.
     *
     * @return string with the name of the module
     */
    String getName();

    /**
     * Returns a {@link QNameModule}, which contains the namespace and
     * the revision of the module.
     *
     * @return QNameModule identifier.
     */
    QNameModule getQNameModule();

    /**
     * Returns the namespace of the module which is specified as argument of
     * YANG {@link Module <b><font color="#00FF00">namespace</font></b>}
     * keyword. If you need both namespace and revision, please consider using
     * {@link #getQNameModule()}.
     *
     * @return URI format of the namespace of the module
     */
    @Override
    default URI getNamespace() {
        return getQNameModule().getNamespace();
    }

    /**
     * Returns the revision date for the module. If you need both namespace and
     * revision, please consider using {@link #getQNameModule()}.
     *
     * @return date of the module revision which is specified as argument of
     *         YANG {@link Module <b><font color="#339900">revison</font></b>}
     *         keyword
     */
    @Override
    default Optional<Revision> getRevision() {
        return getQNameModule().getRevision();
    }

    /**
     * Returns the semantic version of yang module.
     *
     * <p>
     * If the semantic version is not specified, default semantic version of
     * module is returned.
     *
     * @return SemVer semantic version of yang module which is specified as
     *         argument of
     *         (urn:opendaylight:yang:extension:semantic-version?revision
     *         =2016-02-02)semantic-version statement
     */
    Optional<SemVer> getSemanticVersion();

    /**
     * Returns the prefix of the module.
     *
     * @return string with the module prefix which is specified as argument of
     *         YANG {@link Module <b><font color="#0000FF">prefix</font></b>}
     *         keyword
     */
    String getPrefix();

    /**
     * Returns the YANG version.
     *
     * @return YANG version of this module.
     */
    YangVersion getYangVersion();

    /**
     * Returns the module organization.
     *
     * @return string with the name of the organization specified in the module
     *         as the argument of YANG {@link Module <b><font
     *         color="#606060">organization</font></b>} keyword
     */
    Optional<String> getOrganization();

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
    Optional<String> getContact();

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
    Set<AugmentationSchemaNode> getAugmentations();

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
}
