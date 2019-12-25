/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.AbstractMap.SimpleImmutableEntry;
import org.opendaylight.yangtools.yang.common.QName;

public class ValueWithQName<V> extends SimpleImmutableEntry<QName, V> {
    private static final long serialVersionUID = 1L;

    public ValueWithQName(final QName qname, final V value) {
        super(qname, value);
    }

    @Deprecated(forRemoval = true)
    @SuppressFBWarnings(value = "NM_CONFUSING", justification = "Legacy typo")
    public QName getQname() {
        return getKey();
    }

    public QName getQName() {
        return getKey();
    }
}
