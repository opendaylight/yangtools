/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;

final class EmptyLocalKeyEffectiveStatement extends AbstractKeyEffectiveStatement.Local {
    EmptyLocalKeyEffectiveStatement(final KeyStatement declared) {
        super(declared);
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }
}
