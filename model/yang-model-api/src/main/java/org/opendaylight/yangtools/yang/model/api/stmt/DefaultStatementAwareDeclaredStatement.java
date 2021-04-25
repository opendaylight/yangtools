/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Marker interface for statements which may contain a 'default' statement, as defined in RFC7950.
 */
@Beta
public interface DefaultStatementAwareDeclaredStatement extends DeclaredStatement<QName> {
    /**
     * Return a {@link DefaultStatement} child, if present.
     *
     * @return A {@link DefaultStatement}, or empty if none is present.
     */
    default @NonNull Optional<DefaultStatement> getDefault() {
        return findFirstDeclaredSubstatement(DefaultStatement.class);
    }
}
