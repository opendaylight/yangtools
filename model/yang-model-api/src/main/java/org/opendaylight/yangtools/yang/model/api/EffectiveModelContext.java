/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeRoot;

/**
 * {@link EffectiveStatement}-based result of YANG parser compilation. Unlike a SchemaContext, which it extends,
 * it gives access to individual {@link ModuleEffectiveStatement}s that comprise it. It also supports resolution of
 * schema node identifiers via {@link #findSchemaTreeNode(SchemaNodeIdentifier)}.
 */
public interface EffectiveModelContext extends Immutable, SchemaTreeRoot {

    @NonNull Map<QNameModule, ModuleEffectiveStatement> namespaceToModule();

    default @NonNull Collection<ModuleEffectiveStatement> modules() {
        return namespaceToModule().values();
    }

    default @Nullable ModuleEffectiveStatement moduleByNamespace(final @NonNull QNameModule namespace) {
        return namespaceToModule().get(requireNonNull(namespace));
    }

    default Optional<ModuleEffectiveStatement> findModuleByNamespace(final @NonNull QNameModule namespace) {
        return Optional.ofNullable(moduleByNamespace(namespace));
    }

    default @NonNull ModuleEffectiveStatement getModuleByNamespace(final @NonNull QNameModule namespace) {
        final var module = moduleByNamespace(namespace);
        if (module == null) {
            throw new NoSuchElementException("module for " + namespace + " not found");
        }
        return module;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    default @NonNull Map<QNameModule, ModuleEffectiveStatement> getModuleStatements() {
        return namespaceToModule();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    default @Nullable ModuleEffectiveStatement lookupModule(final QNameModule moduleName) {
        return moduleByNamespace(requireNonNull(moduleName));
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    default @NonNull Optional<ModuleEffectiveStatement> findModuleStatement(final QNameModule moduleName) {
        return findModuleByNamespace(requireNonNull(moduleName));
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    default @NonNull Optional<ModuleEffectiveStatement> findModuleStatement(final QName moduleName) {
        return findModuleStatement(moduleName.getModule());
    }

    /**
     * Returns module instances (from the context) with a concrete name. Returned collection is required to have its
     * iteration order guarantee that the latest revision is encountered first.
     *
     * @param name string with the module name
     * @return set of module instances with specified name.
     */
    // FIXME: rename
    @NonNull Collection<@NonNull ModuleEffectiveStatement> findModuleStatements(@NonNull String name);

    /**
     * Returns module instance (from the context) with concrete namespace. Returned collection is required to have its
     * iteration order guarantee that the latest revision is encountered first.
     *
     * @param namespace XMLNamespace instance with specified namespace
     * @return module instance which has namespace equal to the {@code namespace} or {@code null} in other cases
     */
    // FIXME: rename
    @NonNull Collection<@NonNull ModuleEffectiveStatement> findModuleStatements(@NonNull XMLNamespace namespace);

    @Deprecated(since = "16.0.0", forRemoval = true)
    default @NonNull ModuleEffectiveStatement getModuleStatement(final QNameModule moduleName) {
        return getModuleByNamespace(requireNonNull(moduleName));
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    default @NonNull ModuleEffectiveStatement getModuleStatement(final QName moduleName) {
        return getModuleStatement(moduleName.getModule());
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     *     Default implementation defers locates the module corresponding to the first element of path and then defers
     *     to {@link ModuleEffectiveStatement#findSchemaTreeNode(SchemaNodeIdentifier)}.
     */
    @Override
    default Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final SchemaNodeIdentifier path) {
        final var module = moduleByNamespace(path.firstNodeIdentifier().getModule());
        return module == null ? Optional.empty() : module.findSchemaTreeNode(path);
    }
}
