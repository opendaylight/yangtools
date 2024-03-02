/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.util.Iterator;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class Bits32 extends AbstractBits implements Bits {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isSet(final int position) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSet(final String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Bit> setBits() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NonNull Iterator<String> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    Object writeReplace() {
        return new B32v1();
    }
}
