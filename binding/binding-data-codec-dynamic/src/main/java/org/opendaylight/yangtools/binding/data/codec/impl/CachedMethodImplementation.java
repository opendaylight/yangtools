/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.putField;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link MethodImplementation} which caches its result in a field.
 */
abstract class CachedMethodImplementation extends MethodImplementation {
    private static final Generic BB_HANDLE = TypeDefinition.Sort.describe(VarHandle.class);
    private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
    private static final StackManipulation OBJECT_CLASS = ClassConstant.of(ForLoadedType.of(Object.class));
    private static final StackManipulation LOOKUP = invokeMethod(MethodHandles.class, "lookup");
    private static final StackManipulation FIND_VAR_HANDLE =
        invokeMethod(Lookup.class, "findVarHandle", Class.class, String.class, Class.class);

    private static final int PRIV_VOLATILE = Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC;
    static final int PRIV_CONST = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    // getFoo$$$V
    final @NonNull String handleName;

    @NonNullByDefault
    CachedMethodImplementation(final String methodName, final TypeDescription retType) {
        super(methodName, retType);
        handleName = methodName + "$$$V";
    }

    @Override
    public InstrumentedType prepare(final InstrumentedType instrumentedType) {
        final var tmp = instrumentedType
            // private static final VarHandle getFoo$$$V;
            .withField(new FieldDescription.Token(handleName, PRIV_CONST, BB_HANDLE))
            // private volatile Object getFoo;
            .withField(new FieldDescription.Token(methodName, PRIV_VOLATILE, BB_OBJECT));

        return tmp.withInitializer(new ByteCodeAppender.Simple(
            // TODO: acquiring lookup is expensive, we should share it across all initialization
            // getFoo$$$V = MethodHandles.lookup().findVarHandle(This.class, "getFoo", Object.class);
            LOOKUP,
            ClassConstant.of(tmp),
            new TextConstant(methodName),
            OBJECT_CLASS,
            FIND_VAR_HANDLE,
            putField(tmp, handleName)));
    }
}