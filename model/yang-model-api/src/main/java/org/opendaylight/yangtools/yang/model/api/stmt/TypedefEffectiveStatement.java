/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective model statement which should be used to derive application behaviour related to {@code typedef}s.
 * All statements form the a tree-scoped namespace across {@link TypedefEffectiveStatement.MultipleIn}s, each of which
 * hold one level of this namespace.
 */
public interface TypedefEffectiveStatement
        extends EffectiveStatement<QName, @NonNull TypedefStatement>, TypeDefinitionAware {
    /**
     * An {@link EffectiveStatement} that is a parent of multiple {@link TypedefEffectiveStatement}s.
     *
     * <p>This constitutes the statament's contribution to the following in accordance with RFC7950 section 6.2.1:
     * <pre>
     *     All derived type names defined within a parent node or at the top
     *     level of the module or its submodules share the same type
     *     identifier namespace.  This namespace is scoped to all descendant
     *     nodes of the parent node or module.  This means that any
     *     descendant node may use that typedef, and it MUST NOT define a
     *     typedef with the same name.
     * </pre>
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     */
    @Beta
    interface MultipleIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return all {@code TypedefEffectiveStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull TypedefEffectiveStatement> typedefStatements() {
            return collectEffectiveSubstatements(TypedefEffectiveStatement.class);
        }

        /**
         * Find a {@link TypedefEffectiveStatement} based on its {@link TypedefEffectiveStatement#argument()}.
         *
         * @param qname Typedef name
         * @return {@link TypedefEffectiveStatement}, or {@code null}
         */
        default @Nullable TypedefEffectiveStatement lookupTypedefStatement(final @NonNull QName qname) {
            requireNonNull(qname);
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof TypedefEffectiveStatement typedef && qname.equals(typedef.argument())) {
                    return typedef;
                }
            }
            return null;
        }
    }

    @Override
    default StatementDefinition<QName, @NonNull TypedefStatement, ?> statementDefinition() {
        return TypedefStatement.DEF;
    }

    /**
     * {@return this type definition as an effective type statement}
     */
    @Beta
    @NonNull TypeEffectiveStatement asTypeEffectiveStatement();
}
