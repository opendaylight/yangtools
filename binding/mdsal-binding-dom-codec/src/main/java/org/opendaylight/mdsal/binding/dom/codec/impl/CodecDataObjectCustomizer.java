/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.Customizer;
import org.opendaylight.mdsal.binding.dom.codec.loader.StaticClassPool;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating AbstractDataObject specializations.
 */
final class CodecDataObjectCustomizer implements Customizer {
    static final int KEY_OFFSET = -1;

    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectCustomizer.class);
    private static final CtClass CT_ARFU = StaticClassPool.findClass(AtomicReferenceFieldUpdater.class);
    private static final CtClass CT_BOOLEAN = StaticClassPool.findClass(boolean.class);
    private static final CtClass CT_INT = StaticClassPool.findClass(int.class);
    private static final CtClass CT_OBJECT = StaticClassPool.findClass(Object.class);
    private static final CtClass CT_HELPER = StaticClassPool.findClass(ToStringHelper.class);
    private static final CtClass CT_DATAOBJECT = StaticClassPool.findClass(DataObject.class);
    private static final CtClass[] EMPTY_ARGS = new CtClass[0];
    private static final CtClass[] EQUALS_ARGS = new CtClass[] { CT_DATAOBJECT };
    private static final CtClass[] TOSTRING_ARGS = new CtClass[] { CT_HELPER };

    private final List<Method> properties;
    private final Method keyMethod;

    CodecDataObjectCustomizer(final List<Method> properties, final @Nullable Method keyMethod) {
        this.properties = requireNonNull(properties);
        this.keyMethod = keyMethod;
    }

    @Override
    public List<Class<?>> customize(final CodecClassLoader loader, final CtClass bindingClass, final CtClass generated)
            throws NotFoundException, CannotCompileException {
        // Generate members for all methods ...
        LOG.trace("Generating class {}", generated.getName());
        generated.addInterface(bindingClass);

        int offset = 0;
        for (Method method : properties) {
            generateMethod(loader, generated, method, offset++);
        }
        if (keyMethod != null) {
            generateMethod(loader, generated, keyMethod, KEY_OFFSET);
        }

        // Final bits: codecHashCode() ...
        final CtMethod codecHashCode = new CtMethod(CT_INT, "codecHashCode", EMPTY_ARGS, generated);
        codecHashCode.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecHashCode.setBody(codecHashCodeBody());
        generated.addMethod(codecHashCode);

        // ... equals
        final CtMethod codecEquals = new CtMethod(CT_BOOLEAN, "codecEquals", EQUALS_ARGS, generated);
        codecEquals.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecEquals.setBody(codecEqualsBody(bindingClass.getName()));
        generated.addMethod(codecEquals);

        // ... and codecFillToString()
        final CtMethod codecFillToString = new CtMethod(CT_HELPER, "codecFillToString", TOSTRING_ARGS, generated);
        codecFillToString.setModifiers(Modifier.PROTECTED | Modifier.FINAL);
        codecFillToString.setBody(codecFillToStringBody());
        generated.addMethod(codecFillToString);

        generated.setModifiers(Modifier.FINAL | Modifier.PUBLIC);
        return ImmutableList.of();
    }

    private String codecHashCodeBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final int prime = 31;\n")
                .append("int result = 1;\n");

        for (Method method : properties) {
            sb.append("result = prime * result + java.util.").append(utilClass(method)).append(".hashCode(")
            .append(method.getName()).append("());\n");
        }

        return sb.append("return result;\n")
                .append('}').toString();
    }

    private String codecEqualsBody(final String ifaceName) {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final ").append(ifaceName).append(" other = $1;")
                .append("return true");

        for (Method method : properties) {
            final String methodName = method.getName();
            sb.append("\n&& java.util.").append(utilClass(method)).append(".equals(").append(methodName)
            .append("(), other.").append(methodName).append("())");
        }

        return sb.append(";\n")
                .append('}').toString();
    }

    private String codecFillToStringBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("return $1");
        for (Method method : properties) {
            final String methodName = method.getName();
            sb.append("\n.add(\"").append(methodName).append("\", ").append(methodName).append("())");
        }

        return sb.append(";\n")
                .append('}').toString();
    }

    private static void generateMethod(final CodecClassLoader loader, final CtClass generated, final Method method,
            final int offset) throws CannotCompileException, NotFoundException {
        LOG.trace("Generating for method {}", method);
        final String methodName = method.getName();
        final String methodArfu = methodName + "$$$ARFU";

        // AtomicReferenceFieldUpdater ...
        final CtField arfuField = new CtField(CT_ARFU, methodArfu, generated);
        arfuField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        generated.addField(arfuField, new StringBuilder().append(CT_ARFU.getName()).append(".newUpdater(")
            .append(generated.getName()).append(".class, java.lang.Object.class, \"").append(methodName)
            .append("\")").toString());

        // ... corresponding volatile field ...
        final CtField field = new CtField(CT_OBJECT, methodName, generated);
        field.setModifiers(Modifier.PRIVATE | Modifier.VOLATILE);
        generated.addField(field);

        // ... and the getter
        final Class<?> retType = method.getReturnType();
        final CtMethod getter = new CtMethod(loader.findClass(retType), methodName, EMPTY_ARGS, generated);
        final String retName = retType.isArray() ? retType.getSimpleName() : retType.getName();

        getter.setBody(new StringBuilder()
            .append("{\n")
            .append("return (").append(retName).append(") codecMember(").append(methodArfu).append(", ").append(offset)
                .append(");\n")
            .append('}').toString());
        getter.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        generated.addMethod(getter);
    }

    private static String utilClass(final Method method) {
        // We can either have objects or byte[], we cannot have Object[]
        return method.getReturnType().isArray() ? "Arrays" : "Objects";
    }
}