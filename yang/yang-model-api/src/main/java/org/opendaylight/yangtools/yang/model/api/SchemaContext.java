/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * The interface represents static view of compiled yang files, contains the methods for obtaining all the top level
 * context data (data from all modules) like YANG notifications, extensions, operations...
 * Instances MUST be immutable and thus usage in multi threaded environment is safe.
 */
// FIXME: 4.0.0: DataSchemaNode is probably too broad. Reconsider it and impacts on downstreams
public interface SchemaContext extends DataSchemaNode, DataNodeContainer, NotificationNodeContainer, Immutable {
    /**
     * QName of NETCONF top-level data node.
     */
    @NonNull QName NAME = QName.create(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), "data").intern();

    /**
     * Returns data schema node instances which represents direct subnodes (like
     * leaf, leaf-list, list, container) in all YANG modules in the context.
     *
     * @return set of <code>DataSchemaNode</code> instances which represents
     *         YANG data nodes at the module top level
     */
    Set<DataSchemaNode> getDataDefinitions();

    /**
     * Returns modules which are part of the schema context. Returned set is required to have its iteration ordered
     * by module revision, so that if modules are filtered by {@link Module#getName()} or {@link Module#getNamespace()},
     * modules having the same attribute are encountered newest revision first.
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
     * Returns extension definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     *
     * @return set of <code>ExtensionDefinition</code> instances which
     *         represents nodes defined via <code>extension</code> YANG keyword
     */
    Set<ExtensionDefinition> getExtensions();

    /**
     * Returns the module matching specified {@link QNameModule}, if present.
     *
     * @param qnameModule requested QNameModule
     * @return Module, if present.
     * @throws NullPointerException if qnameModule is null
     */
    Optional<Module> findModule(@NonNull QNameModule qnameModule);

    /**
     * Returns module instance (from the context) with specified namespace and no revision.
     *
     * @param namespace module namespace
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>namespace</code> and no revision.
     */
    default Optional<Module> findModule(final @NonNull URI namespace) {
        return findModule(QNameModule.create(namespace));
    }

    /**
     * Returns module instance (from the context) with specified namespace and revision.
     *
     * @param namespace module namespace
     * @param revision module revision, may be null
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>namespace</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final @NonNull URI namespace, final @Nullable Revision revision) {
        return findModule(QNameModule.create(namespace, revision));
    }

    /**
     * Returns module instance (from the context) with specified namespace and revision.
     *
     * @param namespace module namespace
     * @param revision module revision, may be null
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>namespace</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final @NonNull URI namespace, final @NonNull Optional<Revision> revision) {
        return findModule(QNameModule.create(namespace, revision));
    }

    /**
     * Returns module instance (from the context) with specified name and an optional revision.
     *
     * @param name
     *            string with the module name
     * @param revision
     *            date of the module revision
     * @return module instance which has name and revision the same as are the values specified in parameters
     *                <code>name</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final String name, final Optional<Revision> revision) {
        return findModules(name).stream().filter(module -> revision.equals(module.getRevision())).findAny();
    }

    /**
     * Returns module instance (from the context) with specified name and revision.
     *
     * @param name
     *            string with the module name
     * @param revision
     *            date of the module revision, may be null
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>name</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final String name, final @Nullable Revision revision) {
        return findModule(name, Optional.ofNullable(revision));
    }

    /**
     * Returns module instance (from the context) with specified name and no revision.
     *
     * @param name string with the module name
     * @return module instance which has name and revision the same as are the values specified in <code>name</code>
     *                and no revision.
     * @throws NullPointerException if name is null
     */
    default Optional<Module> findModule(final String name) {
        return findModule(name, Optional.empty());
    }

    /**
     * Returns module instances (from the context) with a concrete name. Returned Set is required to have its iteration
     * order guarantee that the latest revision is encountered first.
     *
     * @param name
     *            string with the module name
     * @return set of module instances with specified name.
     */
    default Set<Module> findModules(final String name) {
        return Sets.filter(getModules(), m -> name.equals(m.getName()));
    }

    /**
     * Returns module instance (from the context) with concrete namespace. Returned Set is required to have its
     * iteration order guarantee that the latest revision is encountered first.
     *
     * @param namespace
     *            URI instance with specified namespace
     * @return module instance which has namespace equal to the
     *         <code>namespace</code> or <code>null</code> in other cases
     */
    default Set<Module> findModules(final URI namespace) {
        return Sets.filter(getModules(), m -> namespace.equals(m.getNamespace()));
    }

    @Override
    default Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    default Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    default Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.empty();
    }

    @Beta
    @Override
    default Optional<DataSchemaNode> findDataTreeChild(final QName name) {
        return findModule(name.getModule()).flatMap(mod -> mod.findDataTreeChild(name));
    }
}
