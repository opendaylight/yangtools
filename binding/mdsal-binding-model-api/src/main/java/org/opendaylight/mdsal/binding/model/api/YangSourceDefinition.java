/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;

/**
 * DTO capturing the YANG source definition which lead to a {@link GeneratedType} being emitted.
 */
@Beta
@NonNullByDefault
public abstract class YangSourceDefinition {
    public static final class Multiple extends YangSourceDefinition {
        private final List<? extends SchemaNode> nodes;

        Multiple(final ModuleEffectiveStatement module, final Collection<? extends SchemaNode> nodes) {
            super(module);
            this.nodes = ImmutableList.copyOf(nodes);
        }

        /**
         * Return the defining SchemaNodes. Each node is guaranteed to implement {@link EffectiveStatement} and have
         * a corresponding declared statement.
         *
         * @return defining SchemaNodes, guaranteed to be non-empty
         */
        public List<? extends SchemaNode> getNodes() {
            return nodes.stream().filter(node -> node instanceof EffectiveStatement
                && ((EffectiveStatement<?, ?>) node).getDeclared() != null).collect(Collectors.toList());
        }
    }

    public static final class Single extends YangSourceDefinition {
        private final DocumentedNode node;

        Single(final ModuleEffectiveStatement module, final DocumentedNode node) {
            super(module);
            this.node = requireNonNull(node);
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
    }

    private final ModuleEffectiveStatement module;

    private YangSourceDefinition(final ModuleEffectiveStatement module) {
        this.module = requireNonNull(module);
    }

    public static Optional<YangSourceDefinition> of(final Module module) {
        final ModuleEffectiveStatement effective = module.asEffectiveStatement();
        final ModuleStatement declared = effective.getDeclared();
        if (declared != null) {
            return Optional.of(new Single(effective, module));
        }
        return Optional.empty();
    }

    public static Optional<YangSourceDefinition> of(final Module module, final SchemaNode node) {
        return of(module.asEffectiveStatement(), node);
    }

    public static Optional<YangSourceDefinition> of(final ModuleEffectiveStatement module, final SchemaNode node) {
        if (node instanceof EffectiveStatement) {
            final DeclaredStatement<?> declared = ((EffectiveStatement<?, ?>) node).getDeclared();
            if (declared != null) {
                return Optional.of(new Single(module, node));
            }
        }
        return Optional.empty();
    }

    public static Optional<YangSourceDefinition> of(final Module module, final Collection<? extends SchemaNode> nodes) {
        checkArgument(!nodes.isEmpty());

        final boolean anyDeclared = nodes.stream().anyMatch(
            node -> node instanceof EffectiveStatement && ((EffectiveStatement<?, ?>) node).getDeclared() != null);
        if (anyDeclared) {
            return Optional.of(new Multiple(module.asEffectiveStatement(), nodes));
        }
        return Optional.empty();
    }

    /**
     * Return the defining YANG module.
     *
     * @return Defining YANG module.
     */
    public final ModuleEffectiveStatement getModule() {
        return module;
    }
}
