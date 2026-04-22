/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;

/**
 * A user of a {@link CodecDataObject} field and its corresponding {@link VarHandle}.
 */
interface VHFieldUser {
    StackManipulation FIND_VAR_HANDLE =
        invokeMethod(Lookup.class, "findVarHandle", Class.class, String.class, Class.class);
    StackManipulation OBJECT_CLASS = ClassConstant.of(ForLoadedType.of(Object.class));

    /**
     * Return the byte code required to initialize the {@link VarHandle} field. The {@link Lookup} object and the first
     * argument to {@link Lookup#findVarHandle(Class, String, Class)} are expected to already be on the stack.
     *
     * @param instrumentedType the class being generated
     * @return the initialization bytecode
     */
    StackManipulation.Compound initVarHandle(TypeDescription instrumentedType);
}
