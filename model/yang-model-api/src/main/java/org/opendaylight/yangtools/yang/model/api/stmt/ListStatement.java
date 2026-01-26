/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code list} statement.
 */
public non-sealed interface ListStatement extends DataDefinitionStatement, DataDefinitionStatement.MultipleIn<QName>,
        ActionStatement.MultipleIn<QName>, ConfigStatement.OptionalIn<QName>, GroupingStatementMultipleIn<QName>,
        MaxElementsStatement.OptionalIn<QName>, MinElementsStatement.OptionalIn<QName>,
        MustStatement.MultipleIn<QName>, NotificationStatement.MultipleIn<QName>, OrderedByStatement.OptionalIn<QName>,
        TypedefStatement.MultipleIn<QName> {
    /**
     * The definition of {@code list} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull ListStatement, @NonNull ListEffectiveStatement> DEF =
        StatementDefinition.of(ListStatement.class, ListEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "list", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: rename and document
    default @Nullable KeyStatement getKey() {
        final var opt = findFirstDeclaredSubstatement(KeyStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }

    // FIXME: rename and document
    default @NonNull Collection<? extends @NonNull UniqueStatement> getUnique() {
        return declaredSubstatements(UniqueStatement.class);
    }
}
