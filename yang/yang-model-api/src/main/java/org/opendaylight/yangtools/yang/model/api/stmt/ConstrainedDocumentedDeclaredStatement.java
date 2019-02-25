/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Common interface for statements which contain either a description/reference or a description/reference/status combo.
 */
@Beta
public interface ConstrainedDocumentedDeclaredStatement<T> extends DocumentedDeclaredStatement<T> {

    default @NonNull Optional<ErrorAppTagStatement> getErrorAppTagStatement() {
        return findFirstDeclaredSubstatement(ErrorAppTagStatement.class);
    }

    default @NonNull Optional<ErrorMessageStatement> getErrorMessageStatement() {
        return findFirstDeclaredSubstatement(ErrorMessageStatement.class);
    }

    interface WithStatus<T> extends ConstrainedDocumentedDeclaredStatement<T>,
            DocumentedDeclaredStatement.WithStatus<T> {

    }
}
