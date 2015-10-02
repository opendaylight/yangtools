/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

abstract class AbstractIntegerBuiltInTypeEffectiveStatement extends AbstractBuiltInTypeEffectiveStatement<IntegerTypeDefinition> implements IntegerTypeDefinition {
    @Override
    public final List<RangeConstraint> getRangeConstraints() {
        return delegate().getRangeConstraints();
    }
}
