/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Common interface for action and rpc statements.
 */
@Beta
public interface OperationDeclaredStatement extends DocumentedDeclaredStatement.WithStatus<QName>,
        IfFeatureAwareDeclaredStatement<QName> {
    default @NonNull QName getName() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }

    default @NonNull Optional<InputStatement> getInput() {
        return findFirstDeclaredSubstatement(InputStatement.class);
    }

    default @NonNull Optional<OutputStatement> getOutput() {
        return findFirstDeclaredSubstatement(OutputStatement.class);
    }

    default @NonNull Collection<? extends TypedefStatement> getTypedefs() {
        return declaredSubstatements(TypedefStatement.class);
    }

    default @NonNull Collection<? extends GroupingStatement> getGroupings() {
        return declaredSubstatements(GroupingStatement.class);
    }
}
