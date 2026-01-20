/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

@NonNullByDefault
public interface ContextReferenceStatement extends UnknownStatement<QName> {
    /**
     * The definition of {@code yang-ext:context-reference} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(
        ContextReferenceStatement.class, ContextReferenceEffectiveStatement.class,
        OpenDaylightExtensionsConstants.ORIGINAL_MODULE, "context-reference", "context-type");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
