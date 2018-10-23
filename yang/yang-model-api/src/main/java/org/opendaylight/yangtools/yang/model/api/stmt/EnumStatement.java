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

public interface EnumStatement extends DocumentedDeclaredStatement.WithStatus<String>,
        IfFeatureAwareDeclaredStatement<String> {
    default @NonNull String getName() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }

    default @Nullable ValueStatement getValue() {
        final Optional<ValueStatement> opt = findFirstDeclaredSubstatement(ValueStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
