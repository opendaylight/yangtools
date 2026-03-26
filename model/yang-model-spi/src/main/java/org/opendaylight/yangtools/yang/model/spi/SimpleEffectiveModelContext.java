/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * Default implementation of {@link EffectiveModelContext}.
 *
 * @since 16.0.0
 */
@Beta
public final class SimpleEffectiveModelContext extends SimpleSchemaContext implements EffectiveModelContext {
    private final @NonNull List<DeclaredStatement<?>> rootDeclaredStatements;
    private final @NonNull ImmutableMap<QNameModule, ModuleEffectiveStatement> rootEffectiveStatements;

    private SimpleEffectiveModelContext(final @NonNull Set<Module> modules,
            final @NonNull List<DeclaredStatement<?>> rootDeclaredStatements,
            final @NonNull List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        super(modules);
        this.rootDeclaredStatements = List.copyOf(rootDeclaredStatements);
        this.rootEffectiveStatements = rootEffectiveStatements.stream()
                .filter(ModuleEffectiveStatement.class::isInstance).map(ModuleEffectiveStatement.class::cast)
                .collect(ImmutableMap.toImmutableMap(ModuleEffectiveStatement::localQNameModule, Function.identity()));
    }

    public static @NonNull SimpleEffectiveModelContext of(
            final @NonNull List<DeclaredStatement<?>> rootDeclaredStatements,
            final @NonNull List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        final var modules = new HashSet<Module>();
        for (var stmt : rootEffectiveStatements) {
            if (stmt instanceof ModuleEffectiveStatement module) {
                // verify availability
                module.requireDeclared();
                modules.add(module.toDataNodeContainer());
            }
        }
        return new SimpleEffectiveModelContext(modules, rootDeclaredStatements, rootEffectiveStatements);
    }

    public List<DeclaredStatement<?>> getRootDeclaredStatements() {
        return rootDeclaredStatements;
    }

    @Override
    public Map<QNameModule, ModuleEffectiveStatement> namespaceToModule() {
        return rootEffectiveStatements;
    }

    @Override
    public Collection<ModuleEffectiveStatement> findModuleStatements(final String name) {
        return Collections2.transform(findModules(name), Module::asEffectiveStatement);
    }

    @Override
    public Collection<ModuleEffectiveStatement> findModuleStatements(final XMLNamespace namespace) {
        return Collections2.transform(findModules(namespace), Module::asEffectiveStatement);
    }
}
