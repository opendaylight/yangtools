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
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyStep;

final class KIIv4<T extends KeyAware<K> & DataObject, K extends Key<T>> extends IIv4<T> {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private K key;

    @SuppressWarnings("redundantModifier")
    public KIIv4() {
        // For Externalizable
    }

    KIIv4(final KeyedInstanceIdentifier<T, K> source) {
        super(source);
        key = source.getKey();
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

    @java.io.Serial
    @Override
    Object readResolve() throws ObjectStreamException {
        return new KeyedInstanceIdentifier<>(new KeyStep<>(getTargetType(), key), getPathArguments(),
            isWildcarded(), getHash());
    }
}
