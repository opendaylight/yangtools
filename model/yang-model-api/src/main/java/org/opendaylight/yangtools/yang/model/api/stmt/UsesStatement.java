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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code uses} statement.
 */
public interface UsesStatement extends DataDefinitionStatement {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.USES;
    }

    default @NonNull Collection<? extends RefineStatement> getRefines() {
        return declaredSubstatements(RefineStatement.class);
    }

    default @NonNull Collection<? extends AugmentStatement> getAugments() {
        return declaredSubstatements(AugmentStatement.class);
    }
}
