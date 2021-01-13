/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective view of a {@link StmtContext} for the purposes of creating an {@link EffectiveStatement}.
 */
@Beta
public interface EffectiveStmtCtx extends CommonStmtCtx, StmtContextCompat, Immutable {
    /**
     * Return parent of this context, if there is one. All statements except for top-level source statements, such as
     * {@code module} and {@code submodule}.
     *
     * @return Parent context, or null if this statement is the root
     */
    @Nullable Parent effectiveParent();

    /**
     * Return parent of this context.
     *
     * @return Parent context
     * @throws VerifyException if this context is already the root
     */
    default @NonNull Parent getEffectiveParent() {
        return verifyNotNull(effectiveParent(), "Attempted to access beyond root context");
    }

    /**
     * Minimum amount of parent state required to build an accurate effective view of a particular child. Child state
     * is expressed as {@link Current}.
     */
    @Beta
    interface Parent extends EffectiveStmtCtx {
        /**
         * Effective {@code config} statement value.
         */
        @Beta
        enum EffectiveConfig {
            /**
             * We have an effective {@code config true} statement.
             */
            TRUE(Boolean.TRUE),
            /**
             * We have an effective {@code config false} statement.
             */
            FALSE(Boolean.FALSE),
            /**
             * We are in a context where {@code config} statements are ignored.
             */
            IGNORED(null),
            /**
             * We are in a context where {@code config} is not determined, such as within a {@code grouping}.
             */
            UNDETERMINED(null);

            private final Boolean config;

            EffectiveConfig(final @Nullable Boolean config) {
                this.config = config;
            }

            /**
             * Return this value as a {@link Boolean} for use with {@link DataSchemaNode#effectiveConfig()}.
             *
             * @return A boolean or null
             */
            public @Nullable Boolean asNullable() {
                return config;
            }
        }

        /**
         * Return the effective {@code config} statement value.
         *
         * @return This statement's effective config
         */
        @NonNull EffectiveConfig effectiveConfig();

        /**
         * Return the {@link SchemaPath} of this statement. Not all statements have a SchemaPath, in which case
         * {@link Optional#empty()} is returned.
         *
         * @return Optional SchemaPath
         * @deprecated Use of SchemaPath in the context of effective statements is going away. Consider not providing
         *             this information, if your users can exist without it.
         */
        // FIXME: 7.0.0: this needs to be a tri-state present/absent/disabled
        @Deprecated
        @NonNull Optional<SchemaPath> schemaPath();

        @Deprecated
        default @NonNull SchemaPath getSchemaPath() {
            return schemaPath().orElseThrow();
        }

        @Deprecated
        default @Nullable SchemaPath wrapSchemaPath() {
            return SchemaPathSupport.wrap(getSchemaPath());
        }
    }

    /**
     * Minimum amount of state required to build an accurate effective view of a statement. This is a strict superset
     * of information available in {@link Parent}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement
     */
    @Beta
    interface Current<A, D extends DeclaredStatement<A>> extends Parent, NamespaceStmtCtx, BoundStmtCtxCompat<A, D> {

        @NonNull QName moduleName();

        @Nullable EffectiveStatement<?, ?> original();

        /**
         * Summon the <a href="https://en.wikipedia.org/wiki/Rabbit_of_Caerbannog">Rabbit of Caerbannog</a>.
         *
         * @param <E> Effective Statement representation
         * @return The {@code Legendary Black Beast of Arrrghhh}.
         */
        // FIXME: YANGTOOLS-1186: lob the Holy Hand Grenade of Antioch
        @Deprecated
        <E extends EffectiveStatement<A, D>> @NonNull StmtContext<A, D, E> caerbannog();
    }
}
