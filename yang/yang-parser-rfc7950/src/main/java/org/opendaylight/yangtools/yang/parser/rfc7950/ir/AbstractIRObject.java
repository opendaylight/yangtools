/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import com.google.common.base.MoreObjects;
import org.opendaylight.yangtools.concepts.Immutable;

abstract class AbstractIRObject implements Immutable {
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("fragment", toYangFragment(new StringBuilder())).toString();
    }

    abstract StringBuilder toYangFragment(StringBuilder sb);
}
