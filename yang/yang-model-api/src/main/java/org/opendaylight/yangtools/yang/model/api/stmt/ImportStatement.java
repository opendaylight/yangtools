/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface ImportStatement extends DocumentedDeclaredStatement<String> {
    @Override
    @NonNull String rawArgument();

    default @NonNull String getModule() {
        return rawArgument();
    }

    default @NonNull PrefixStatement getPrefix() {
        return findFirstDeclaredSubstatement(PrefixStatement.class).get();
    }

    default @Nullable RevisionDateStatement getRevisionDate() {
        final Optional<RevisionDateStatement> opt = findFirstDeclaredSubstatement(RevisionDateStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
