/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveHumanTextStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code organization} statement.
 */
public interface OrganizationEffectiveStatement extends EffectiveHumanTextStatement<@NonNull OrganizationStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link OrganizationEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.2
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code OrganizationEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable OrganizationEffectiveStatement organizationStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof OrganizationEffectiveStatement organization) {
                    return organization;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code OrganizationEffectiveStatement}}
         */
        default @NonNull Optional<OrganizationEffectiveStatement> findOrganizationStatement() {
            return Optional.ofNullable(organizationStatement());
        }

        /**
         * {@return the {@code OrganizationEffectiveStatement}}
         *
         * @throws NoSuchElementException if not present
         */
        default @NonNull OrganizationEffectiveStatement getOrganizationStatement() {
            final var organization = organizationStatement();
            if (organization == null) {
                throw new NoSuchElementException("No organization statement present in " + this);
            }
            return organization;
        }
    }

    @Override
    default StatementDefinition<String, @NonNull OrganizationStatement, ?> statementDefinition() {
        return OrganizationStatement.DEF;
    }
}
