/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

public interface BitStatement extends DocumentedDeclaredStatement.WithStatus<QName>,
        IfFeatureAwareDeclaredStatement<QName> {
    default @NonNull String getName() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument()).getLocalName();
    }

    default @Nullable PositionStatement getPosition() {
        final Optional<PositionStatement> opt = findFirstDeclaredSubstatement(PositionStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
