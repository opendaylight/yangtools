/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.putField;

import java.lang.invoke.VarHandle;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link MethodImplementation} of a getter method with accompanying field and {@link VarHandle}.
 */
abstract class GetterMethodImplementation extends MethodImplementation implements VHFieldUser {
    private static final Generic BB_HANDLE = TypeDefinition.Sort.describe(VarHandle.class);
    private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);

    private static final int PRIV_VOLATILE = Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC;
    static final int PRIV_CONST = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    // getFoo$$$V
    final @NonNull String handleName;

    @NonNullByDefault
    GetterMethodImplementation(final String methodName, final TypeDescription retType) {
        super(methodName, retType);
        handleName = methodName + "$$$V";
    }

    @Override
    public InstrumentedType prepare(final InstrumentedType instrumentedType) {
        return instrumentedType
            // private static final VarHandle getFoo$$$V;
            .withField(new FieldDescription.Token(handleName, PRIV_CONST, BB_HANDLE))
            // private volatile Object getFoo;
            .withField(new FieldDescription.Token(methodName, PRIV_VOLATILE, BB_OBJECT));
    }

    @Override
    public final StackManipulation.Compound initVarHandle(final TypeDescription instrumentedType) {
        return new StackManipulation.Compound(
            new TextConstant(methodName),
            OBJECT_CLASS,
            FIND_VAR_HANDLE,
            putField(instrumentedType, handleName));
    }
}
