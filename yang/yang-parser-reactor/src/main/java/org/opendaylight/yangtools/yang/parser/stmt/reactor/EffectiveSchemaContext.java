/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.util.SimpleSchemaContext;

@VisibleForTesting
public final class EffectiveSchemaContext extends SimpleSchemaContext implements EffectiveModelContext {
    private final ImmutableList<DeclaredStatement<?>> rootDeclaredStatements;
    private final ImmutableMap<QNameModule, ModuleEffectiveStatement> rootEffectiveStatements;

    private EffectiveSchemaContext(final Set<Module> modules, final List<DeclaredStatement<?>> rootDeclaredStatements,
            final List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        super(modules);
        this.rootDeclaredStatements = ImmutableList.copyOf(rootDeclaredStatements);
        this.rootEffectiveStatements = rootEffectiveStatements.stream()
                .filter(ModuleEffectiveStatement.class::isInstance).map(ModuleEffectiveStatement.class::cast)
                .collect(ImmutableMap.toImmutableMap(ModuleEffectiveStatement::localQNameModule, Function.identity()));
    }

    static EffectiveSchemaContext create(final List<DeclaredStatement<?>> rootDeclaredStatements,
            final List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        final Set<Module> modules = new HashSet<>();
        for (EffectiveStatement<?, ?> stmt : rootEffectiveStatements) {
            if (stmt.getDeclared() instanceof ModuleStatement) {
                Verify.verify(stmt instanceof Module);
                modules.add((Module) stmt);
            }
        }

        return new EffectiveSchemaContext(modules, rootDeclaredStatements, rootEffectiveStatements);
    }

    @VisibleForTesting
    public List<DeclaredStatement<?>> getRootDeclaredStatements() {
        return rootDeclaredStatements;
    }

    @Beta
    public ImmutableMap<QNameModule, ModuleEffectiveStatement> getModuleStatements() {
        return rootEffectiveStatements;
    }
}
