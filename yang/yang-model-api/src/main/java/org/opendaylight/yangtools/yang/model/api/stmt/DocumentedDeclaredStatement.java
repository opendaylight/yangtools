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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Common interface for statements which contain either a description/reference or a description/reference/status combo.
 */
@Beta
public interface DocumentedDeclaredStatement<T> extends DeclaredStatement<T>, DocumentationGroup {
    @Override
    default DescriptionStatement getDescription() {
        final Optional<DescriptionStatement> opt = findFirstDeclaredSubstatement(DescriptionStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default ReferenceStatement getReference() {
        final Optional<ReferenceStatement> opt = findFirstDeclaredSubstatement(ReferenceStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    interface WithStatus<T> extends DocumentedDeclaredStatement<T>, DocumentationGroup.WithStatus {
        @Override
        default StatusStatement getStatus() {
            final Optional<StatusStatement> opt = findFirstDeclaredSubstatement(StatusStatement.class);
            return opt.isPresent() ? opt.get() : null;
        }
    }
}
