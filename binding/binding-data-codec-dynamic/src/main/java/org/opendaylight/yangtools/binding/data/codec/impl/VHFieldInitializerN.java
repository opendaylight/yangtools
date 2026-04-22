/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Verify.verify;

import java.util.ArrayList;
import java.util.List;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;

/**
 * A {@link VHFieldInitializer} encapsulating more two or more fields.
 */
final class VHFieldInitializerN extends VHFieldInitializer {
    private final List<VHFieldUser> vhUsers;

    VHFieldInitializerN(final List<? extends VHFieldUser> vhUsers) {
        verify(vhUsers.size() > 1);
        this.vhUsers = List.copyOf(vhUsers);
    }

    @Override
    StackManipulation.Compound initFields(final TypeDescription generatedType) {
        // We would like to generate something line
        //
        //     var lookup = MethodHandles.lookup()
        //     getFoo$$$V = lookup.findVarHandle(This.class, "getFoo", Object.class);
        //     getBar$$$V = lookup.findVarHandle(This.class, "getBar", Object.class);
        //
        // except we do not have an OpCodes.ASTORE StackManipulation, so we fudge it by issuing Opcodes.DUP to maintain
        // lookup at the top of the stack after all invocation but the last.
        final var stackManipulations = new ArrayList<StackManipulation>(vhUsers.size() * 3 - 1);
        final var classConstant = ClassConstant.of(generatedType);

        final var it = vhUsers.iterator();
        var current = it.next();
        while (it.hasNext()) {
            stackManipulations.add(Duplication.SINGLE);
            stackManipulations.add(classConstant);
            stackManipulations.add(current.initVarHandle(generatedType));
            current = it.next();
        }
        stackManipulations.add(classConstant);
        stackManipulations.add(current.initVarHandle(generatedType));

        return new StackManipulation.Compound(stackManipulations);
    }
}
