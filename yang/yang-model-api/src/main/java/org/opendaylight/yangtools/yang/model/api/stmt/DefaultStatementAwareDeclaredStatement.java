/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Marker interface for statements which may contain a 'default' statement, as defined in RFC7950.
 */
public interface DefaultStatementAwareDeclaredStatement extends DeclaredStatement<QName>,
        DefaultStatementContainer {
    @Override
    default DefaultStatement getDefault() {
        final Optional<DefaultStatement> opt = findFirstDeclaredSubstatement(DefaultStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
