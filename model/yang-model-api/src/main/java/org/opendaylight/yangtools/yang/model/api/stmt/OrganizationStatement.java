/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredHumanTextStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code organization} statement.
 */
public interface OrganizationStatement extends DeclaredHumanTextStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ConfigStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code OrganizationStatement} or {@code null} if not present}
         */
        default @Nullable OrganizationStatement organizationStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof OrganizationStatement organization) {
                    return organization;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code OrganizationStatement}}
         */
        default @NonNull Optional<OrganizationStatement> findOrganizationStatement() {
            return Optional.ofNullable(organizationStatement());
        }

        /**
         * {@return the {@code ConfigStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull OrganizationStatement getOrganizationStatement() {
            final var organization = organizationStatement();
            if (organization == null) {
                throw new NoSuchElementException("No config statement present in " + this);
            }
            return organization;
        }
    }

    /**
     * The definition of {@code organization} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull OrganizationStatement, @NonNull OrganizationEffectiveStatement> DEF =
        StatementDefinition.of(OrganizationStatement.class, OrganizationEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "organization", YangArgumentDefinitions.TEXT_AS_STRING);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
