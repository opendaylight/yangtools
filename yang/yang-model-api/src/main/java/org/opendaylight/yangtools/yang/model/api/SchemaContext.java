/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * The interface represents static view of compiled yang files,
 * contains the methods for obtaining all the top level context
 * data (data from all modules) like YANG notifications, extensions,
 * operations...
 * Instances MUST be immutable and thus usage in multi threaded
 * environment is safe.
 */
@Immutable
public interface SchemaContext extends ContainerSchemaNode {
    /**
     * QName of NETCONF top-level data node.
     */
    QName NAME = QName.create(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), null, "data").intern();

    /**
     * Returns data schema node instances which represents direct subnodes (like
     * leaf, leaf-list, list, container) in all YANG modules in the context.
     *
     * @return set of <code>DataSchemaNode</code> instances which represents
     *         YANG data nodes at the module top level
     */
    Set<DataSchemaNode> getDataDefinitions();

    /**
     * Returns modules which are part of the schema context.
     *
     * @return set of the modules which belong to the schema context
     */
    Set<Module> getModules();

    /**
     * Returns rpc definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     *
     * @return set of <code>RpcDefinition</code> instances which represents
     *         nodes defined via <code>rpc</code> YANG keyword
     */
    Set<RpcDefinition> getOperations();

    /**
     * Returns extencion definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     *
     * @return set of <code>ExtensionDefinition</code> instances which
     *         represents nodes defined via <code>extension</code> YANG keyword
     */
    Set<ExtensionDefinition> getExtensions();

    /**
     * Returns module instance (from the context) with concrete name and
     * revision date.
     *
     * @param name
     *            string with the module name
     * @param revision
     *            date of the module revision
     * @return module instance which has name and revision (if specified) the
     *         same as are the values specified in parameters <code>name</code>
     *         and <code>revision</code>. In other cases the <code>null</code>
     *         value is returned.
     *
     */
    Module findModuleByName(String name, Date revision);

    /**
     * Returns module instance (from the context) with concrete namespace.
     *
     * @param namespace
     *            URI instance with specified namespace
     * @return module instance which has namespace equal to the
     *         <code>namespace</code> or <code>null</code> in other cases
     */
    default Set<Module> findModuleByNamespace(final URI namespace) {
        return Sets.filter(getModules(), m -> namespace.equals(m.getNamespace()));
    }

    /**
     * Returns module instance based on given namespace and revision. If
     * revision is not specified, returns module with newest revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision, may be null
     * @return Matching module or null if a match is not found
     */
    default Module findModuleByNamespaceAndRevision(final URI namespace, final @Nullable Revision revision) {
        for (Module module : findModuleByNamespace(namespace)) {
            if (revision == null || revision.equals(module.getRevision())) {
                return module;
            }
        }
        return null;
    }

    /**
     * Get yang source code represented as string for matching
     * {@link org.opendaylight.yangtools.yang.model.api.ModuleIdentifier}.
     *
     * @param moduleIdentifier must provide a non-null
     *        {@link org.opendaylight.yangtools.yang.model.api.ModuleIdentifier#getName()}, other methods might return
     *        null.
     * @return value iif matching module is found in schema context.
     */
    Optional<String> getModuleSource(ModuleIdentifier moduleIdentifier);

    /**
     * Get all module and submodule identifiers.
     */
    Set<ModuleIdentifier> getAllModuleIdentifiers();
}
