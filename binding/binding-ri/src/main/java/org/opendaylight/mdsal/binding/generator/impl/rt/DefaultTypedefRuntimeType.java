/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.TypedefRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

public final class DefaultTypedefRuntimeType extends AbstractGeneratedRuntimeType<TypedefEffectiveStatement>
        implements TypedefRuntimeType {
    public DefaultTypedefRuntimeType(final GeneratedType bindingType, final TypedefEffectiveStatement statement) {
        super(bindingType, statement);
    }
}
