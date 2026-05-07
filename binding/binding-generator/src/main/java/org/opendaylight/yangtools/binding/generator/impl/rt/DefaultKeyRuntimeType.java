/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

@NonNullByDefault
public final class DefaultKeyRuntimeType extends AbstractRuntimeType<KeyEffectiveStatement, KeyArchetype>
        implements KeyRuntimeType {
    public DefaultKeyRuntimeType(final KeyArchetype archetype) {
        super(archetype);
    }
}
