/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Common capture of declared traits shared by {@code action} and {@code rpc} statements.
 */
public sealed interface DeclaredOperationStatement extends DeclaredStatement<QName>,
        DescriptionStatement.OptionalIn<QName>, IfFeatureStatement.MultipleIn<QName>,
        GroupingStatementMultipleIn<QName>, ReferenceStatement.OptionalIn<QName>, StatusStatement.OptionalIn<QName>,
        TypedefStatement.MultipleIn<QName> permits ActionStatement, RpcStatement {

    default @NonNull Optional<InputStatement> getInput() {
        return findFirstDeclaredSubstatement(InputStatement.class);
    }

    default @NonNull Optional<OutputStatement> getOutput() {
        return findFirstDeclaredSubstatement(OutputStatement.class);
    }
}
