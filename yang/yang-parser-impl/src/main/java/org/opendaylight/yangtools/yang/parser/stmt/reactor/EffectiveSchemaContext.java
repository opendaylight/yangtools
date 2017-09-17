/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.util.SimpleSchemaContext;

@VisibleForTesting
public final class EffectiveSchemaContext extends SimpleSchemaContext {
    private final List<DeclaredStatement<?>> rootDeclaredStatements;
    private final List<EffectiveStatement<?, ?>> rootEffectiveStatements;

    private EffectiveSchemaContext(final Set<Module> modules, final List<DeclaredStatement<?>> rootDeclaredStatements,
            final List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        super(modules);
        this.rootDeclaredStatements = ImmutableList.copyOf(rootDeclaredStatements);
        this.rootEffectiveStatements = ImmutableList.copyOf(rootEffectiveStatements);
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

    /**
     * Resolve SchemaContext for a set of modules.
     *
     * @deprecated Use {@link SimpleSchemaContext#forModules(Set)} instead.
     */
    @Deprecated
    public static SchemaContext resolveSchemaContext(final Set<Module> modules) {
        return SimpleSchemaContext.forModules(modules);
    }

    @VisibleForTesting
    public List<DeclaredStatement<?>> getRootDeclaredStatements() {
        return rootDeclaredStatements;
    }

    @VisibleForTesting
    public List<EffectiveStatement<?, ?>> getRootEffectiveStatements() {
        return rootEffectiveStatements;
    }
}
