/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.runtime.api.LeafRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

public final class DefaultLeafRuntimeType extends AbstractRuntimeType<LeafEffectiveStatement, Type>
        implements LeafRuntimeType {
    public DefaultLeafRuntimeType(final Type bindingType, final LeafEffectiveStatement statement) {
        super(bindingType, statement);
    }
}
