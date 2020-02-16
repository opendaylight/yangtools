/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;

final class EmptyForeignKeyEffectiveStatement extends AbstractKeyEffectiveStatement.Foreign {
    EmptyForeignKeyEffectiveStatement(final KeyStatement declared, final Set<QName> argument) {
        super(declared, argument);
    }
}
