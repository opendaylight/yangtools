/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link EffectiveStatement} representation of a {@code augment} statement as defined by
 * <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-7.17">RFC7950</a>.
 *
 * <p>
 * This statement has two logical views on its {@code schema tree} children:
 * <ol>
 *   <li>The primary view, visible from this statement's parent, corresponds to {@link #withDeclaredSchemaTree()}.
 *       {@link SchemaTreeEffectiveStatement} substatements reflect their declared view. This closely corresponding to
 *       {@code getDeclared().declaredSubstatements()}, without any effects of further {@code augment} and
 *       {@code deviate} statements in the target subtree.</li>
 *   <li>The secondary view is available via {@link #withTargetSchemaTree()}. It corresponds to how this {@code augment}
 *       statement's children manifest at the target statement. These include the effects of other {@code augment}
 *       and {@code deviate} statements on the statement tree.</li>
 * </ol>
 */
@Beta
public interface AugmentEffectiveStatement
        extends SchemaTreeAwareEffectiveStatement<SchemaNodeIdentifier, AugmentStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.AUGMENT;
    }

    /**
     * Return the {@code declared view} of this statement. Its {@code schema tree} <b>does not</b> include effects of
     * {@code deviate} and {@code augment} statements targeting the same node.
     *
     * @return Declared view of this statement
     */
    @NonNull AugmentEffectiveStatement withDeclaredSchemaTree();

    /**
     * Return the {@code target view} of this statement. Its {@code schema tree} <b>includes</b> effects of
     * {@code deviate} and {@code augment} statements targeting the same node.
     *
     * @return Target view of this statement
     */
    @NonNull AugmentEffectiveStatement withTargetSchemaTree();
}
