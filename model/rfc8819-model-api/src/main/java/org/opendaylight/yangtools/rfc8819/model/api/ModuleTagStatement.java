/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'module-tag' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8819">RFC8819</a>.
 */
@NonNullByDefault
public interface ModuleTagStatement extends UnknownStatement<Tag> {
    /**
     * The definition of {@code tags:module-tag} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.attributeArg(ModuleTagConstants.RFC8819_MODULE,
        "module-tag", "tag", ModuleTagStatement.class, ModuleTagEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
