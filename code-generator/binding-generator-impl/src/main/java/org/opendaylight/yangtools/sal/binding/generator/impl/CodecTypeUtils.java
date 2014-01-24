/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;

public class CodecTypeUtils {

    @SuppressWarnings({"unchecked","rawtypes"})
    public static IdentifiableItem<?, ?> newIdentifiableItem(Class<?> type, Object key) {
        Class<? extends Identifiable<?>> identifiableType = (Class<? extends Identifiable<?>>) type;
        Identifier<? extends Identifiable<?>> identifier = (Identifier<? extends Identifiable<?>>) key;
        return new IdentifiableItem(identifiableType,identifier);
    }
}
