/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code status} statement.
 *
 * <p>
 * Note that unlike almost all other representations, these statements are only ever a reflection of a declared
 * {@code status} statement. The truly effective status of a particular statement within a tree depends on its parent
 * statements. As an example, given this YANG module:
 * <code>
 *   <pre>
 *     module foo {
 *
 *       grouping foo {
 *         leaf baz {
 *           type string;
 *         }
 *       }
 *
 *       container bar {
 *         status deprecated;
 *         uses foo;
 *       }
 *
 *       uses foo;
 *     }
 *   </pre>
 * </code>
 * The object model will only reflect the {@code status} statement in {@code container bar}, but will not be present in
 * {@code bar}'s {@code baz} leaf. Users are advised to use utility classes related to statement inference which
 * consider parent/child relationships of statements.
 */
public interface StatusEffectiveStatement extends EffectiveStatement<Status, StatusStatement> {
    @Override
    default  StatementDefinition statementDefinition() {
        return YangStmtMapping.STATUS;
    }
}
