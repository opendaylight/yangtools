/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code key} statement.
 */
public interface KeyEffectiveStatement extends EffectiveStatement<Set<QName>, KeyStatement> {
    @Override
    default  StatementDefinition statementDefinition() {
        return YangStmtMapping.KEY;
    }

    /**
     * {@inheritDoc}
     *
     * Iteration order of the returned set is required to match the order in which key components were declared.
     */
    @Override
    Set<QName> argument();
}
