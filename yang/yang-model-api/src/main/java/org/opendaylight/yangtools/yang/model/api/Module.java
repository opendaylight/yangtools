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
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * This interface contains the methods for getting the data from the YANG module.
 */
@Immutable
public interface Module extends DataNodeContainer, DocumentedNode, NotificationNodeContainer, NamespaceRevisionAware {
    /**
     * Returns the name of the module which is specified as argument of YANG {@code module} statement.
     *
     * @return string with the name of the module
     */
    String getName();

    /**
     * Returns a {@link QNameModule}, which contains the namespace and the revision of the module.
     *
     * @return QNameModule identifier.
     */
    QNameModule getQNameModule();

    /**
     * Returns the namespace of the module which is specified as argument of YANG {@code namespace}
     * keyword. If you need both namespace and revision, please consider using {@link #getQNameModule()}.
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
     * @return date of the module revision which is specified as argument of YANG {@code revison} statement
     */
    @Override
    default Optional<Revision> getRevision() {
        return getQNameModule().getRevision();
    }

    /**
     * Returns the semantic version of YANG module. If the semantic version is not specified, default semantic version
     * of module is returned.
     *
     * @return SemVer semantic version of YANG module which is specified as argument of
     *         {@code (urn:opendaylight:yang:extension:semantic-version?revision=2016-02-02)semantic-version} statement
     */
    Optional<SemVer> getSemanticVersion();

    /**
     * Returns the prefix of the module.
     *
     * @return string with the module prefix which is specified as argument of YANG {@code prefix} statement
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
     * @return string with the name of the organization specified in the module as the argument of YANG
     *         {@code organization} statement
     */
    Optional<String> getOrganization();

    /**
     * Returns the module contact.
     *
     * <p>
     * The contact represents the person or persons to whom technical queries concerning this module should be sent,
     * such as their name, postal address, telephone number, and electronic mail address.
     *
     * @return string with the contact data specified in the module as the argument of YANG {@code contact} statement
     */
    Optional<String> getContact();

    /**
     * Returns imports which represents YANG modules which are imported to this module via {@code import} statement.
     *
     * @return set of module imports which are specified in the module as the argument of YANG {@code import}
     *         statements.
     */
    Set<ModuleImport> getImports();

    Set<Module> getSubmodules();

    /**
     * Returns {@link FeatureDefinition} instances which contain data from {@code feature} statements defined in the
     * module.
     *
     * <p>
     * The feature is used to define a mechanism by which portions of the schema are marked as conditional.
     *
     * @return feature statements in lexicographical order which are specified in the module as the argument of YANG
     *         {@code feature} statements.
     */
    Set<FeatureDefinition> getFeatures();

    /**
     * Returns {@link AugmentationSchemaNode} instances which contain data from {@code augment} statements defined
     * in the module.
     *
     * @return set of the augmentation schema instances which are specified in the module as YANG {@code augment}
     *         statement and are lexicographically ordered
     */
    Set<AugmentationSchemaNode> getAugmentations();

    /**
     * Returns {@link RpcDefinition} instances which contain data from {@code rpc} statements defined in the module.
     *
     * @return set of the RPC definition instances which are specified in the module as YANG {@code rpc} statements and
     *         are lexicographicaly ordered
     */
    Set<RpcDefinition> getRpcs();

    /**
     * Returns {@link Deviation} instances which contain data from {@code deviation} statements defined in the module.
     *
     * @return set of the deviation instances
     */
    Set<Deviation> getDeviations();

    /**
     * Returns {@link IdentitySchemaNode} instances which contain data from {@code identity} statements defined in the
     * module.
     *
     * @return set of identity schema node instances which are specified in the module as YANG {@code identity}
     *         statements and are lexicographically ordered
     */
    Set<IdentitySchemaNode> getIdentities();

    /**
     * Returns {@link ExtensionDefinition} instances which contain data from {@code extension} statements defined in
     * the module.
     *
     * @return set of extension definition instances which are specified in the module as YANG {@code extension}
     *         statements and are lexicographically ordered
     */
    List<ExtensionDefinition> getExtensionSchemaNodes();
}
