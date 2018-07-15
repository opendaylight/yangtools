/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface ConditionalDeclaredStatement<A> extends DeclaredStatement<A>, ConditionalDataDefinition {
    @Override
    default @Nullable WhenStatement getWhenStatement() {
        final Optional<WhenStatement> opt = findFirstDeclaredSubstatement(WhenStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
