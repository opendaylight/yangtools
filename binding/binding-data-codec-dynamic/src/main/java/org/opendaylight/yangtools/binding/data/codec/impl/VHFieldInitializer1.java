/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link VHFieldInitializer} encapsulating exactly one field.
 */
final class VHFieldInitializer1 extends VHFieldInitializer {
    private final @NonNull VHFieldUser vhUser;

    VHFieldInitializer1(final VHFieldUser vhUser) {
        this.vhUser = requireNonNull(vhUser);
    }

    @Override
    StackManipulation.Compound initFields(final TypeDescription generatedType) {
        // getFoo$$$V = MethodHandles.lookup().findVarHandle(This.class, "getFoo", Object.class);
        return new StackManipulation.Compound(ClassConstant.of(generatedType), vhUser.initVarHandle(generatedType));
    }
}
