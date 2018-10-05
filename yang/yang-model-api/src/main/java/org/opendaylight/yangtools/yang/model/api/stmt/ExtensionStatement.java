/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

public interface ExtensionStatement extends DocumentedDeclaredStatement.WithStatus<QName> {
    default @Nullable ArgumentStatement getArgument() {
        final Optional<ArgumentStatement> opt = findFirstDeclaredSubstatement(ArgumentStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
