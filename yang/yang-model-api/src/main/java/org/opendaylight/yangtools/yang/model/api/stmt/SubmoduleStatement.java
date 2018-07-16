/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SubmoduleStatement extends MetaDeclaredStatement<String>, LinkageDeclaredStatement,
        RevisionAwareDeclaredStatement, BodyDeclaredStatement {
    default @Nonnull String getName() {
        return rawArgument();
    }

    default @Nullable YangVersionStatement getYangVersion() {
        final Optional<YangVersionStatement> opt = findFirstDeclaredSubstatement(YangVersionStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    default @Nonnull BelongsToStatement getBelongsTo() {
        return findFirstDeclaredSubstatement(BelongsToStatement.class).get();
    }
}

