/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

final class Bv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public Bv1() {
        // Visible for Exernalizable
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
    }

    @java.io.Serial
    Object readReplace() {

    }
}
