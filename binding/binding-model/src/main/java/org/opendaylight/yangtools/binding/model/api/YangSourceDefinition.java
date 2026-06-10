/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * DTO capturing the YANG source definition which lead to a {@link LegacyArchetype} being emitted.
 */
@Beta
@NonNullByDefault
public final class YangSourceDefinition {
    private final ModuleEffectiveStatement module;
    private final DocumentedNode node;

    private YangSourceDefinition(final ModuleEffectiveStatement module, final DocumentedNode node) {
        this.module = requireNonNull(module);
        this.node = requireNonNull(node);
    }

    public static @Nullable YangSourceDefinition of(final ModuleEffectiveStatement module) {
        return module.declared() == null ? null : new YangSourceDefinition(module, module.toDataNodeContainer());
    }

    public static Optional<YangSourceDefinition> of(final Module module, final SchemaNode node) {
        return of(module.asEffectiveStatement(), node);
    }

    public static Optional<YangSourceDefinition> of(final ModuleEffectiveStatement module, final SchemaNode node) {
        return hasDeclaredStatement(node) ? Optional.of(new YangSourceDefinition(module, node)) : Optional.empty();
    }

    public static Optional<YangSourceDefinition> of(final ModuleEffectiveStatement module,
            final EffectiveStatement<?, ?> effective) {
        return effective instanceof DocumentedNode node && effective.declared() != null
                ? Optional.of(new YangSourceDefinition(module, node)) : Optional.empty();
    }

    /**
     * Return the defining YANG module.
     *
     * @return Defining YANG module.
     */
    public ModuleEffectiveStatement getModule() {
        return module;
    }

    /**
     * Return the defining DocumentedNode. The node is guaranteed to implement {@link EffectiveStatement} and have
     * a corresponding declared statement.
     *
     * @return defining SchemaNodes, guaranteed to be non-empty
     */
    public DocumentedNode getNode() {
        return node;
    }

    private static boolean hasDeclaredStatement(final SchemaNode schemaNode) {
        return schemaNode instanceof EffectiveStatement<?, ?> effective && effective.declared() != null;
    }
}
