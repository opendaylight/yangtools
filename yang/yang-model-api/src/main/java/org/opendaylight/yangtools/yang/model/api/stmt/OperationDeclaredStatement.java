/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Common interface for action and rpc statements.
 */
@Beta
public interface OperationDeclaredStatement extends DocumentedDeclaredStatement.WithStatus<QName>, OperationGroup,
        ConditionalDeclaredStatement<QName> {
    @Override
    default @Nonnull QName getName() {
        return argument();
    }

    @Override
    default InputStatement getInput() {
        final Optional<InputStatement> opt = findFirstDeclaredSubstatement(InputStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default OutputStatement getOutput() {
        final Optional<OutputStatement> opt = findFirstDeclaredSubstatement(OutputStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default @Nonnull Collection<? extends TypedefStatement> getTypedefs() {
        return declaredSubstatements(TypedefStatement.class);
    }

    @Override
    default @Nonnull Collection<? extends GroupingStatement> getGroupings() {
        return declaredSubstatements(GroupingStatement.class);
    }
}
