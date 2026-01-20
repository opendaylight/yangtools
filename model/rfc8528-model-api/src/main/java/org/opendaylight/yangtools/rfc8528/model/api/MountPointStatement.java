/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatementAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedDeclaredStatement.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'mount-point' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8528">RFC8528</a>.
 */
@NonNullByDefault
public interface MountPointStatement extends UnknownStatement<MountPointLabel>, WithStatus<MountPointLabel>,
        ConfigStatementAwareDeclaredStatement<MountPointLabel> {
    /**
     * The definition of {@code nc:get-filter-element-attributes} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.attributeArg(SchemaMountConstants.RFC8528_MODULE,
        "mount-point", "label", MountPointStatement.class, MountPointEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    @Override
    default @NonNull String rawArgument() {
        return argument().qname().getLocalName();
    }
}
