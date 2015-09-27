/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.Map.Entry;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;

public class ValueWithQName<V> implements Entry<QName, V>{
    final QName qname;
    final V value;

    public ValueWithQName(final QName qname, final V value) {
        super();
        this.qname = qname;
        this.value = value;
    }

    public QName getQname() {
        return qname;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public QName getKey() {
        return qname;
    }

    @Override
    public V setValue(final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        ValueWithQName other = (ValueWithQName) obj;
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
