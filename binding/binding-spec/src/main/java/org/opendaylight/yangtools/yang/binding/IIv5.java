/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.impl.ORv1;

final class IIv5 extends ORv1 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("redundantModifier")
    public IIv5() {
        // For Externalizable
    }

    IIv5(final InstanceIdentifier<?> source) {
        super(source);
    }

    @Override
    protected DataObjectReference<?> resolve(final ImmutableList<@NonNull DataObjectStep<?>> toResolve) {
        return InstanceIdentifier.unsafeOf(toResolve);
    }
}
