/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.mandatory;

import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;

abstract class AbstractMandatoryEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.DefaultArgument<Boolean, MandatoryStatement>
        implements MandatoryEffectiveStatement {
    AbstractMandatoryEffectiveStatement(final MandatoryStatement declared) {
        super(declared);
    }
}
