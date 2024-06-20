/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serial;

final class KeyedInstanceIdentifierV2<T extends KeyAware<K> & DataObject, K extends Key<T>>
        extends InstanceIdentifierV3<T> {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private K key;

    @SuppressWarnings("redundantModifier")
    public KeyedInstanceIdentifierV2() {
        // For Externalizable
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(key);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        key = (K) in.readObject();
    }

    @Serial
    @Override
    Object readResolve() throws ObjectStreamException {
        return new KeyedInstanceIdentifier<>(new KeyStep<>(getTargetType(), key), getPathArguments(),
            isWildcarded(), getHash());
    }
}
