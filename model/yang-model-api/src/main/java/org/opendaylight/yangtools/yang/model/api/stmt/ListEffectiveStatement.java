/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountAware;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link EffectiveStatement} representation of a {@code list} statement as defined by
 * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-7.8">RFC7950</a>.
 */
public interface ListEffectiveStatement extends DataTreeEffectiveStatement<ListStatement>,
        DataTreeAwareEffectiveStatement<QName, ListStatement>, TypedefAwareEffectiveStatement<QName, ListStatement>,
        OrderedByAwareEffectiveStatement<QName, ListStatement>, ElementCountAware {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.LIST;
    }

    @Override
    default @Nullable ElementCountMatcher elementCountMatcher() {
        return ElementCountMatcher.ofStatement(this);
    }
}
