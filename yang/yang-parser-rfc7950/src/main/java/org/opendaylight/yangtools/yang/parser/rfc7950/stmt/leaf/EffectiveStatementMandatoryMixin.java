/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import org.opendaylight.yangtools.yang.model.api.MandatoryAware;

interface EffectiveStatementMandatoryMixin extends EffectiveStatementFlagMixin, MandatoryAware {
    @Override
    default boolean isMandatory() {
        return (flags() & MANDATORY) != 0;
    }
}
