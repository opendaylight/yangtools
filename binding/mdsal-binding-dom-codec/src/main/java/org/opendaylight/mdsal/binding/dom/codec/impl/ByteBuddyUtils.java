/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldDescription.ForLoadedField;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.FieldAccess.Defined;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

final class ByteBuddyUtils {
    static final StackManipulation THIS = MethodVariableAccess.loadThis();

    private ByteBuddyUtils() {

    }

    static StackManipulation invokeMethod(final Method method) {
        return MethodInvocation.invoke(describe(method));
    }

    static StackManipulation invokeMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        return MethodInvocation.invoke(describe(clazz, name, args));
    }

    static AsmVisitorWrapper computeFrames() {
        return ComputeFrames.INSTANCE;
    }

    static StackManipulation ifEq(final Label label) {
        return new IfEq(label);
    }

    static StackManipulation markLabel(final Label label) {
        return new Mark(label);
    }

    static StackManipulation loadThis() {
        return THIS;
    }

    static StackManipulation getField(final Field field) {
        return FieldAccess.forField(new ForLoadedField(field).asDefined()).read();
    }

    static StackManipulation getField(final TypeDescription instrumentedType, final String fieldName) {
        return fieldAccess(instrumentedType, fieldName).read();
    }

    static StackManipulation putField(final TypeDescription instrumentedType, final String fieldName) {
        return fieldAccess(instrumentedType, fieldName).write();
    }

    private static ForLoadedMethod describe(final Method method) {
        return new ForLoadedMethod(method);
    }

    private static ForLoadedMethod describe(final Class<?> clazz, final String name, final Class<?>... args) {
        return describe(getMethod(clazz, name, args));
    }

    private static Defined fieldAccess(final TypeDescription instrumentedType, final String fieldName) {
        return FieldAccess.forField(instrumentedType.getDeclaredFields().filter(ElementMatchers.named(fieldName))
            .getOnly());
    }

    /**
     * Utility wrapper to force ASM to compute frames.
     */
    private enum ComputeFrames implements AsmVisitorWrapper {
        INSTANCE;

        @Override
        public int mergeWriter(final int flags) {
            return flags | ClassWriter.COMPUTE_FRAMES;
        }

        @Override
        public int mergeReader(final int flags) {
            return flags | ClassWriter.COMPUTE_FRAMES;
        }

        @Override
        public ClassVisitor wrap(final TypeDescription td, final ClassVisitor cv, final Implementation.Context ctx,
                final TypePool tp, final FieldList<FieldDescription.InDefinedShape> fields, final MethodList<?> methods,
                final int wflags, final int rflags) {
            return cv;
        }
    }

    /**
     * IFEQ opcode invocation, jumping to a particular label.
     */
    private static final class IfEq implements StackManipulation {
        private static final StackManipulation.Size SIZE = new StackManipulation.Size(-1, 0);

        private final Label label;

        IfEq(final Label label) {
            this.label = requireNonNull(label);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackManipulation.Size apply(final MethodVisitor mv, final Implementation.Context ctx) {
            mv.visitJumpInsn(Opcodes.IFEQ, label);
            return SIZE;
        }
    }

    /**
     * A label definition, marking the spot where IfEq should jump.
     */
    private static final class Mark implements StackManipulation {
        private static final StackManipulation.Size SIZE = new StackManipulation.Size(0, 0);

        private final Label label;

        Mark(final Label label) {
            this.label = requireNonNull(label);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackManipulation.Size apply(final MethodVisitor mv, final Implementation.Context ctx) {
            mv.visitLabel(label);
            return SIZE;
        }
    }

    private static Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        try {
            return clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
