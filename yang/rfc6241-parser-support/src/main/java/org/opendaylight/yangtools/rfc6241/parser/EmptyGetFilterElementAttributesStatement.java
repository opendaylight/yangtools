/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithoutArgument;

final class EmptyGetFilterElementAttributesStatement extends WithoutArgument
        implements GetFilterElementAttributesStatement {
    static final @NonNull EmptyGetFilterElementAttributesStatement INSTANCE =
            new EmptyGetFilterElementAttributesStatement();
}
