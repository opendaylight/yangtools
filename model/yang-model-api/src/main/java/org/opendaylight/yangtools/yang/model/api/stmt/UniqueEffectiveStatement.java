/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Interface describing YANG {@code unique} statement.
 *
 * <p>
 * The 'unique' constraint specifies that the combined values of all the leaf instances specified in the argument
 * string, including leafs with default values, MUST be unique within all list entry instances in which all referenced
 * leafs exist (for more information see RFC-6020 section 7.8.3.).
 */
public interface UniqueEffectiveStatement extends EffectiveStatement<Set<Descendant>, UniqueStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.UNIQUE;
    }
}
