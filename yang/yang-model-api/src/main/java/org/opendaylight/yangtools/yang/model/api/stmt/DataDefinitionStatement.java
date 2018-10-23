/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Statement that defines new data nodes.
 * One of container, leaf, leaf-list, list, choice, case,
 * augment, uses, anyxml and anydata.
 *
 * <p>
 * Defined in: <a href="https://tools.ietf.org/html/rfc6020#section-3">RFC6020, Section 3</a>
 */
@Rfc6020AbnfRule("data-def-stmt")
public interface DataDefinitionStatement extends DocumentedDeclaredStatement.WithStatus<QName>,
        WhenStatementAwareDeclaredStatement<QName> {
    default @NonNull QName getName() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }
}
