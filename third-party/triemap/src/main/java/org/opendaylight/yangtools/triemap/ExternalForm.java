/*
 * (C) Copyright 2017 Pantheon Technologies, s.r.o. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.triemap;

import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.Map.Entry;

/**
 * External serialization object for use with TrieMap objects. This hides the implementation details, such as object
 * hierarchy. It also makes handling read-only snapshots more elegant.
 *
 * @author Robert Varga
 */
final class ExternalForm implements Externalizable {
    private static final long serialVersionUID = 1L;

    private TrieMap<Object, Object> map;
    private boolean readOnly;

    public ExternalForm() {
        // For Externalizable
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    ExternalForm(final TrieMap<?, ?> map) {
        this.map = ((TrieMap)map).readOnlySnapshot();
        this.readOnly = map.isReadOnly();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(map.equiv());
        out.writeInt(map.size());
        for (Entry<Object, Object> e : map.entrySet()) {
            out.writeObject(e.getKey());
            out.writeObject(e.getValue());
        }
        out.writeBoolean(readOnly);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        final Equivalence<Object> equiv = (Equivalence<Object>) in.readObject();
        Preconditions.checkArgument(equiv != null);
        map = new TrieMap<>(equiv);

        final int size = in.readInt();
        for (int i = 0; i < size; ++i) {
            map.add(in.readObject(), in.readObject());
        }

        readOnly = in.readBoolean();
    }

    private Object readResolve() throws ObjectStreamException {
        return Verify.verifyNotNull(readOnly ? map.readOnlySnapshot() : map);
    }
}
