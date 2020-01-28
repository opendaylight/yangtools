/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag;

import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;

final class EmptyErrorAppTagEffectiveStatement extends DefaultArgument<String, ErrorAppTagStatement>
        implements ErrorAppTagEffectiveStatement {
    EmptyErrorAppTagEffectiveStatement(final ErrorAppTagStatement declared) {
        super(declared);
    }
}
