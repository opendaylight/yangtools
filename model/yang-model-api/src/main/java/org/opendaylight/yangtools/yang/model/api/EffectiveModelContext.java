/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeRoot;

/**
 * {@link EffectiveStatement}-based result of YANG parser compilation. Unlike a SchemaContext, which it extends,
 * it gives access to individual {@link ModuleEffectiveStatement}s that comprise it. It also supports resolution of
 * schema node identifiers via {@link #findSchemaTreeNode(SchemaNodeIdentifier)}.
 *
 * @author Robert Varga
 */
@Beta
// FIXME: 8.0.0: evaluate if we still need to extend SchemaContext here
public interface EffectiveModelContext extends SchemaContext, SchemaTreeRoot {

    @NonNull Map<QNameModule, ModuleEffectiveStatement> getModuleStatements();

    default @NonNull Optional<ModuleEffectiveStatement> findModuleStatement(final QNameModule moduleName) {
        return Optional.ofNullable(getModuleStatements().get(requireNonNull(moduleName)));
    }

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
    default @NonNull Collection<@NonNull ModuleEffectiveStatement> findModuleStatements(final String name) {
        return Collections2.transform(findModules(name), Module::asEffectiveStatement);
    }

    /**
     * Returns module instance (from the context) with concrete namespace. Returned collection is required to have its
     * iteration order guarantee that the latest revision is encountered first.
     *
     * @param namespace XMLNamespace instance with specified namespace
     * @return module instance which has namespace equal to the {@code namespace} or {@code null} in other cases
     */
    default @NonNull Collection<@NonNull ModuleEffectiveStatement> findModuleStatements(
            final XMLNamespace namespace) {
        return Collections2.transform(findModules(namespace), Module::asEffectiveStatement);
    }

    default @NonNull ModuleEffectiveStatement getModuleStatement(final QNameModule moduleName) {
        return verifyNotNull(getModuleStatements().get(requireNonNull(moduleName)));
    }

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
        return findModuleStatement(path.firstNodeIdentifier().getModule())
            .flatMap(module -> module.findSchemaTreeNode(path));
    }
}
