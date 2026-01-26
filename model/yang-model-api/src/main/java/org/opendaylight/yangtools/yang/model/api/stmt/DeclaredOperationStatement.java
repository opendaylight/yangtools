/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Common capture of declared traits shared by {@code action} and {@code rpc} statements.
 */
public sealed interface DeclaredOperationStatement extends DeclaredStatement<QName>,
        DescriptionStatement.OptionalIn<QName>, IfFeatureStatement.MultipleIn<QName>,
        GroupingStatementMultipleIn<QName>, InputStatement.OptionalIn<QName>, OutputStatement.OptionalIn<QName>,
        ReferenceStatement.OptionalIn<QName>, StatusStatement.OptionalIn<QName>, TypedefStatement.MultipleIn<QName>
        permits ActionStatement, RpcStatement {
    // Nothing else
}
