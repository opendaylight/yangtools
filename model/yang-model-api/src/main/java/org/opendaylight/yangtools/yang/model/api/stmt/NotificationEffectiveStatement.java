/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code notification} statement.
 */
public interface NotificationEffectiveStatement extends SchemaTreeEffectiveStatement<NotificationStatement>,
        DataTreeAwareEffectiveStatement<QName, NotificationStatement>,
        TypedefAwareEffectiveStatement<QName, NotificationStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.NOTIFICATION;
    }
}
