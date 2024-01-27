/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * The interface represents static view of compiled yang files,
 * contains the methods for obtaining all the top level context
 * data (data from all modules) like YANG notifications, extensions,
 * operations...
 * Instances MUST be immutable and thus usage in multi threaded
 * environment is safe.
 */
// FIXME: 7.0.0: ContainerLike is far too broad. A combination of DataNodeContainer, NotificationNodeContainer
//               and possibly DataSchemaNode would reflect SchemaContext traits better.
// FIXME: 7.0.0: consider deprecating this class in favor of EffectiveModelContext
public interface SchemaContext extends ContainerLike, Immutable {
    /**
     * QName of NETCONF top-level data node.
     */
    // FIXME: YANGTOOLS-1074: we do not want this name
    @NonNull QName NAME = QName.create(YangConstants.NETCONF_NAMESPACE, "data").intern();

    /**
     * Returns data schema node instances which represents direct subnodes (like
     * leaf, leaf-list, list, container) in all YANG modules in the context.
     *
     * @return set of <code>DataSchemaNode</code> instances which represents
     *         YANG data nodes at the module top level
     */
    @NonNull Collection<? extends @NonNull DataSchemaNode> getDataDefinitions();

    /**
     * Returns modules which are part of the schema context. Returned set is required to have its iteration ordered
     * by module revision, so that if modules are filtered by {@link Module#getName()} or {@link Module#getNamespace()},
     * modules having the same attribute are encountered newest revision first.
     *
     * @return set of the modules which belong to the schema context
     */
    @NonNull Collection<? extends @NonNull Module> getModules();

    /**
     * Returns rpc definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     *
     * @return set of <code>RpcDefinition</code> instances which represents
     *         nodes defined via <code>rpc</code> YANG keyword
     */
    @NonNull Collection<? extends @NonNull RpcDefinition> getOperations();

    /**
     * Returns extension definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     *
     * @return set of <code>ExtensionDefinition</code> instances which
     *         represents nodes defined via <code>extension</code> YANG keyword
     */
    @NonNull Collection<? extends ExtensionDefinition> getExtensions();

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
    default Optional<Module> findModule(final @NonNull XMLNamespace namespace) {
        return findModule(QNameModule.of(namespace));
    }

    /**
     * Returns module instance (from the context) with specified namespace and revision.
     *
     * @param namespace module namespace
     * @param revision module revision, may be null
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>namespace</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final @NonNull XMLNamespace namespace, final @Nullable Revision revision) {
        return findModule(QNameModule.ofRevision(namespace, revision));
    }

    /**
     * Returns module instance (from the context) with specified namespace and revision.
     *
     * @param namespace module namespace
     * @param revision module revision, may be null
     * @return module instance which has name and revision the same as are the values specified in parameters
     *         <code>namespace</code> and <code>revision</code>.
     */
    default Optional<Module> findModule(final @NonNull XMLNamespace namespace,
            final @NonNull Optional<Revision> revision) {
        return findModule(QNameModule.ofRevision(namespace, revision.orElse(null)));
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
    default Optional<? extends Module> findModule(final String name, final Optional<Revision> revision) {
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
    default Optional<? extends Module> findModule(final String name, final @Nullable Revision revision) {
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
    default Optional<? extends Module> findModule(final String name) {
        return findModule(name, Optional.empty());
    }

    /**
     * Returns module instances (from the context) with a concrete name. Returned Set is required to have its iteration
     * order guarantee that the latest revision is encountered first.
     *
     * @param name string with the module name
     * @return set of module instances with specified name.
     */
    default @NonNull Collection<? extends @NonNull Module> findModules(final String name) {
        return Collections2.filter(getModules(), m -> name.equals(m.getName()));
    }

    /**
     * Returns module instance (from the context) with concrete namespace. Returned Set is required to have its
     * iteration order guarantee that the latest revision is encountered first.
     *
     * @param namespace XMLNamespace instance with specified namespace
     * @return module instance which has namespace equal to the {@code namespace} or {@code null} in other cases
     */
    default @NonNull Collection<? extends @NonNull Module> findModules(final XMLNamespace namespace) {
        return Collections2.filter(getModules(), m -> namespace.equals(m.getNamespace()));
    }

    @Override
    @Deprecated
    default Collection<? extends ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    @Deprecated
    default Optional<ActionDefinition> findAction(final QName qname) {
        requireNonNull(qname);
        return Optional.empty();
    }

    @Override
    default Optional<NotificationDefinition> findNotification(final QName qname) {
        final var defs = findModule(qname.getModule()).map(Module::getNotifications);
        if (defs.isPresent()) {
            for (var def : defs.orElseThrow()) {
                if (qname.equals(def.getQName())) {
                    return Optional.of(def);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    @Deprecated
    default Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    @Deprecated
    default Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    @Deprecated
    default Collection<? extends @NonNull MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    @Deprecated
    default Optional<? extends QualifiedBound> getWhenCondition() {
        return Optional.empty();
    }

    @Override
    @Deprecated
    default boolean isAugmenting() {
        return false;
    }

    @Override
    @Deprecated
    default boolean isAddedByUses() {
        return false;
    }

    @Override
    @Deprecated
    default Optional<Boolean> effectiveConfig() {
        return Optional.empty();
    }

    @Override
    @Deprecated
    default QName getQName() {
        // FIXME: YANGTOOLS-1074: we do not want this name
        return NAME;
    }

    @Override
    @Deprecated
    default Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    @Deprecated
    default Collection<? extends UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    @Deprecated
    default Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
        return Collections.emptySet();
    }

    @Beta
    @Override
    default Optional<DataSchemaNode> findDataTreeChild(final QName name) {
        return findModule(name.getModule()).flatMap(mod -> mod.findDataTreeChild(name));
    }

    /**
     * Get identities derived from a selected identity.
     *
     * @param identity base identity
     * @return collection of identities derived from this identity
     * @throws NullPointerException if identity is null
     * @throws IllegalArgumentException if the specified identity is not present in this context
     */
    @Beta
    @NonNull Collection<? extends IdentitySchemaNode> getDerivedIdentities(IdentitySchemaNode identity);
}
