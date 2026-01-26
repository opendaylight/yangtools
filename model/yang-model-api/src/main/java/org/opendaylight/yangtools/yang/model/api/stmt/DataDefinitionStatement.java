/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Statement that defines new data nodes. One of container, leaf, leaf-list, list, choice, case, augment, uses, anyxml
 * or anydata.
 *
 * <p>Defined in <a href="https://www.rfc-editor.org/rfc/rfc7950#section-3">RFC7950, Section 3</a> and in section 12 as
 * {@code data-def-stmt} ABNF rule.
 */
public sealed interface DataDefinitionStatement
        extends DocumentedDeclaredStatement<QName>, IfFeatureStatement.MultipleIn<QName>,
                StatusStatement.OptionalIn<QName>, WhenStatement.OptionalIn<QName>
        permits AnydataStatement, AnyxmlStatement, ChoiceStatement, ContainerStatement, LeafListStatement,
                LeafStatement, ListStatement, UsesStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link DataDefinitionStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code DataDefinitionStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull DataDefinitionStatement> dataDefinitionStatements() {
            return declaredSubstatements(DataDefinitionStatement.class);
        }
    }
}
