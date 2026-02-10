/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.TypeDefinitionCompat;

/**
 * Effective statement representation of 'annotation' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc7952">RFC7952</a>.
 */
public interface AnnotationEffectiveStatement
        extends TypeDefinitionCompat<AnnotationName, @NonNull AnnotationStatement> {
    /**
     * An entity capable of finding {@link AnnotationEffectiveStatement}s.
     */
    @NonNullByDefault
    interface Index {
        /**
         * {@return the {@code AnnotationEffectiveStatement} with specified name, or {@code null} if not present}
         * @param name the {@link AnnotationName}
         */
        @Nullable AnnotationEffectiveStatement lookupAnnotation(AnnotationName name);
    }

    /**
     * {@return {@link AnnotationEffectiveStatement} corresponding to specified name, or {@code null} if not found}
     * @param modelContext to {@link EffectiveModelContext} to search
     * @param name the {@link AnnotationName} to look for
     */
    @NonNullByDefault
    static @Nullable AnnotationEffectiveStatement lookupIn(final EffectiveModelContext modelContext,
            final AnnotationName name) {
        return modelContext instanceof Index index ? index.lookupAnnotation(name) : searchIn(modelContext, name);
    }

    private static @Nullable AnnotationEffectiveStatement searchIn(final @NonNull EffectiveModelContext modelContext,
            final @NonNull AnnotationName name) {
        final var module = modelContext.lookupModule(name.qname().getModule());
        return switch (module) {
            case null -> null;
            case Index index -> index.lookupAnnotation(name);
            default -> {
                for (var stmt : module.filterEffectiveStatements(AnnotationEffectiveStatement.class)) {
                    if (name.equals(stmt.argument())) {
                        yield stmt;
                    }
                }
                yield null;
            }
        };
    }

    @Override
    default StatementDefinition<AnnotationName, @NonNull AnnotationStatement, ?> statementDefinition() {
        return AnnotationStatement.DEF;
    }
}
