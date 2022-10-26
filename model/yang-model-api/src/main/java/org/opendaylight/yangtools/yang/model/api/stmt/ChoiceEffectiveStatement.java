/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code choice} statement.
 */
public interface ChoiceEffectiveStatement
        extends SchemaTreeEffectiveStatement<ChoiceStatement>, DataTreeAwareEffectiveStatement<QName, ChoiceStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.CHOICE;
    }

    /**
     * Namespace of available cases in a choice node. According to RFC7950 section 6.2.1:
     * <pre>
     *     All cases within a choice share the same case identifier
     *     namespace.  This namespace is scoped to the parent choice node.
     * </pre>
     */
    default @NonNull Optional<CaseEffectiveStatement> findCase(final @NonNull QName qname) {
        return DefaultMethodHelpers.filterOptional(findSchemaTreeNode(qname), CaseEffectiveStatement.class);
    }
}
