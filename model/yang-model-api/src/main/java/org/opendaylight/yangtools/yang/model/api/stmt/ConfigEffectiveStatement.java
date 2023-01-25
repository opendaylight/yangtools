/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code config} statement.
 *
 * <p>
 * Note that unlike almost all other representations, these statements are only ever a reflection of a declared
 * {@code config} statement. The truly effective status of a particular statement within a tree depends on its parent
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
 *         config false;
 *         uses foo;
 *       }
 *
 *       uses bar;
 *     }
 *   </pre>
 * </code>
 * The object model will only reflect the {@code config} statement in {@code container bar}, but will not be present in
 * {@code bar}'s {@code baz} leaf. The real effective {@code config} of {@code leaf baz} is a tri-state value:
 * <ol>
 *   <li>within {@code grouping foo} it is not applicable, that is to say undefined</li>
 *   <li>within {@code container bar} it is inherited, that is to say {@code false}</li>
 *   <li>within {@code module foo} it is inherited from default, that is to say {@code true}</li>.
 * </ol>
 *
 * <p>
 * Users are advised to use utility classes related to statement inference which consider parent/child relationships
 * of statements.
 */
public interface ConfigEffectiveStatement extends EffectiveStatement<Boolean, ConfigStatement> {
    @Override
    default  StatementDefinition statementDefinition() {
        return YangStmtMapping.CONFIG;
    }
}
