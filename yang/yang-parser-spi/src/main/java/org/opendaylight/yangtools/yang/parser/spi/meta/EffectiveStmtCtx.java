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
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Effective view of a {@link StmtContext} for the purposes of creating an {@link EffectiveStatement}.
 */
@Beta
public interface EffectiveStmtCtx extends CommonStmtCtx, Immutable {
    /**
     * Return parent of this context, if there is one. All statements except for top-level source statements, such as
     * {@code module} and {@code submodule}.
     *
     * @return Parent context, or null if this statement is the root
     */
    // FIXME: YANGTOOLS-1185: rename to effectiveParent()
    @Nullable Parent parent();

    /**
     * Return parent of this context.
     *
     * @return Parent context
     * @throws VerifyException if this context is already the root
     */
    // FIXME: YANGTOOLS-1185: rename to getEffectiveParent()
    default @NonNull Parent getParent() {
        return verifyNotNull(parent(), "Attempted to access beyond root context");
    }

    /**
     * Minimum amount of parent state required to build an accurate effective view of a particular child. Child state
     * is expressed as {@link Current}.
     */
    @Beta
    interface Parent extends EffectiveStmtCtx {
        // FIXME: 7.0.0: this should be Optional<Boolean>
        boolean effectiveConfig();

        // FIXME: 7.0.0: this needs to be a tri-state present/absent/disabled
        @Deprecated
        @NonNull Optional<SchemaPath> schemaPath();

        @Deprecated
        default @NonNull SchemaPath getSchemaPath() {
            return schemaPath().orElseThrow();
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
    interface Current<A, D extends DeclaredStatement<A>> extends Parent {

        @NonNull CopyHistory history();

        @NonNull D declared();

        <K, V, T extends K, N extends IdentifierNamespace<K, V>> @Nullable V getFromNamespace(Class<@NonNull N> type,
            T key);

        <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(Class<N> type);

        @Nullable A argument();

        default @NonNull A coerceArgument() {
            return verifyNotNull(argument(), "Attempted to use non-existent argument");
        }

        @Nullable EffectiveStatement<?, ?> original();

        @NonNull YangVersion yangVersion();

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
