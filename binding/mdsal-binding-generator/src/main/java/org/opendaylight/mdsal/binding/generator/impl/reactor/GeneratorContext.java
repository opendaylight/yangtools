/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Abstract view on generation tree as viewed by a particular {@link Generator}.
 */
abstract class GeneratorContext {
    private final @NonNull EffectiveModelContext modelContext;

    GeneratorContext(final EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
    }

    final @NonNull EffectiveModelContext modelContext() {
        return modelContext;
    }

    /**
     * Resolve generator for the type object pointed to by a {@code path} expression, or {@code null} it the if cannot
     * the current generator is nested inside a {@code grouping} and the generator cannot be found.
     *
     * @param path A {@code path} expression
     * @return Resolved generator, or {@code null} if legally not found
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalStateException if this generator is not inside a {@code grouping} and the path cannot be resolved
     */
    abstract @Nullable AbstractTypeObjectGenerator<?, ?> resolveLeafref(@NonNull PathExpression path);

    /**
     * Resolve a tree-scoped namespace reference. This covers {@code typedef} and {@code grouping} statements, as per
     * bullets 5 and 6 of <a href="https://tools.ietf.org/html/rfc6020#section-6.2.1">RFC6020, section 6.2.1</a>.
     *
     * @param <E> {@link EffectiveStatement} type
     * @param type EffectiveStatement class
     * @param argument Statement argument
     * @return Resolved {@link Generator}
     * @throws NullPointerException if any argument is null
     * @throws IllegalStateException if the generator cannot be found
     */
    abstract <E extends EffectiveStatement<QName, ?>, G extends AbstractExplicitGenerator<E, ?>>
        @NonNull G resolveTreeScoped(@NonNull Class<G> type, @NonNull QName argument);

    abstract @NonNull ModuleGenerator resolveModule(@NonNull QNameModule namespace);

    final @NonNull IdentityGenerator resolveIdentity(final @NonNull QName name) {
        for (Generator gen : resolveModule(name.getModule())) {
            if (gen instanceof final IdentityGenerator idgen) {
                if (name.equals(idgen.statement().argument())) {
                    return idgen;
                }
            }
        }
        throw new IllegalStateException("Failed to find identity " + name);
    }

    final @NonNull TypedefGenerator resolveTypedef(final @NonNull QName qname) {
        return resolveTreeScoped(TypedefGenerator.class, qname);
    }
}
