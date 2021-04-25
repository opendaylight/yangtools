/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

@Beta
public interface ContextReferenceEffectiveStatement
        extends UnknownEffectiveStatement<QName, ContextReferenceStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return OpenDaylightExtensionsStatements.CONTEXT_REFERENCE;
    }

    /**
     * Return the {@link IdentityEffectiveStatement} identifying the {@code context type} of this reference.
     *
     * @return An {@link IdentityEffectiveStatement}.
     */
    @NonNull IdentityEffectiveStatement contextType();
}
