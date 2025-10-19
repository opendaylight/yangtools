/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.IOException;
import java.io.ObjectInput;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;

@Deprecated(since = "14.0.0", forRemoval = true)
final class KeyedInstanceIdentifierV2<T extends EntryObject<T, K>, K extends Key<T>> extends InstanceIdentifierV3<T> {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    @SuppressWarnings("redundantModifier")
    public KeyedInstanceIdentifierV2() {
        // For Externalizable
    }

    @Override
    @SuppressWarnings("ReturnValueIgnored")
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Key.class.cast(in.readObject());
    }
}
