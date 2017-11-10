/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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
    QName NAME = QName.create(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), "data").intern();

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
     * Returns module instance (from the context) with concrete name and revision date.
     *
     * @param name
     *            string with the module name
     * @param revision
     *            date of the module revision
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>name</code> and <code>revision</code>.
     */
    Optional<Module> findModule(String name, Optional<Revision> revision);

    /**
     * Returns module instance (from the context) with concrete name and revision date.
     *
     * @param name
     *            string with the module name
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>name</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final String name) {
        return findModule(name, Optional.empty());
    }

    /**
     * Returns module instance (from the context) with concrete name and revision date.
     *
     * @param name
     *            string with the module name
     * @param revision
     *            date of the module revision, may be null
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>name</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final String name, @Nullable final Revision revision) {
        return findModule(name, Optional.ofNullable(revision));
    }

    default Optional<Module> findModule(final URI namespace) {
        return findModule(QNameModule.create(namespace));
    }

    default Optional<Module> findModule(final URI namespace, @Nullable final Revision revision) {
        return findModule(QNameModule.create(namespace, revision));
    }

    default Optional<Module> findModule(final URI namespace, final Optional<Revision> revision) {
        return findModule(QNameModule.create(namespace, revision));
    }

    default Optional<Module> findModule(final QNameModule qnameModule) {
        return getModules().stream().filter(m -> qnameModule.equals(m.getQNameModule())).findAny();
    }

    /**
     * Returns module instances (from the context) with a concrete name.
     *
     * @param name
     *            string with the module name
     * @return set of module instances with specified name.
     */
    default Set<Module> findModules(final String name) {
        return Sets.filter(getModules(), m -> name.equals(m.getName()));
    }

    /**
     * Returns module instance (from the context) with concrete namespace.
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
    default Set<ActionDefinition> getActions() {
        return ImmutableSet.of();
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
    default Collection<MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }
}
