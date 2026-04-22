/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.List;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;

/**
 * A combined {@link VarHandle} initializer. We choose this complexity because {@link MethodHandles#lookup()} is
 * relatively expensive to
 */
abstract sealed class VHFieldInitializer implements ByteCodeAppender permits VHFieldInitializer1, VHFieldInitializerN {
    private static final StackManipulation LOOKUP = invokeMethod(MethodHandles.class, "lookup");

    static final <T> Builder<T> initializeVarHandles(final Builder<T> builder,
            final List<? extends VHFieldUser> vhUsers) {
        return switch (vhUsers.size()) {
            case 0 -> builder;
            case 1 -> builder.initializer(new VHFieldInitializer1(vhUsers.getFirst()));
            default -> builder.initializer(new VHFieldInitializerN(vhUsers));
        };
    }

    @Override
    public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
            final MethodDescription instrumentedMethod) {
        return new ByteCodeAppender.Simple(LOOKUP, initFields(implementationContext.getInstrumentedType()))
            .apply(methodVisitor, implementationContext, instrumentedMethod);
    }

    /**
     * Return the byte code required to initialize {@link VarHandle} fields using the {@link Lookup} object on the top
     * of the stack.
     *
     * @param generatedType the class being generated
     * @return the initialization bytecode
     */
    abstract StackManipulation.Compound initFields(TypeDescription generatedType);
}
