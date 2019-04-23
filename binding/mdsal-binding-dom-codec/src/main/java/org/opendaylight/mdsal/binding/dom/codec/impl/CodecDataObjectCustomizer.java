/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.NonNull;
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
    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectCustomizer.class);
    private static final CtClass CT_ARFU = StaticClassPool.findClass(AtomicReferenceFieldUpdater.class);
    private static final CtClass CT_BOOLEAN = StaticClassPool.findClass(boolean.class);
    private static final CtClass CT_DATAOBJECT = StaticClassPool.findClass(DataObject.class);
    private static final CtClass CT_HELPER = StaticClassPool.findClass(ToStringHelper.class);
    private static final CtClass CT_IIC = StaticClassPool.findClass(IdentifiableItemCodec.class);
    private static final CtClass CT_INT = StaticClassPool.findClass(int.class);
    private static final CtClass CT_NCS = StaticClassPool.findClass(NodeContextSupplier.class);
    private static final CtClass CT_OBJECT = StaticClassPool.findClass(Object.class);
    private static final CtClass[] EMPTY_ARGS = new CtClass[0];
    private static final CtClass[] EQUALS_ARGS = new CtClass[] { CT_DATAOBJECT };
    private static final CtClass[] TOSTRING_ARGS = new CtClass[] { CT_HELPER };

    private final ImmutableMap<Method, NodeContextSupplier> properties;
    private final Entry<Method, IdentifiableItemCodec> keyMethod;

    CodecDataObjectCustomizer(final ImmutableMap<Method, NodeContextSupplier> properties,
            final @Nullable Entry<Method, IdentifiableItemCodec> keyMethod) {
        this.properties = requireNonNull(properties);
        this.keyMethod = keyMethod;
    }

    @Override
    public List<Class<?>> customize(final CodecClassLoader loader, final CtClass bindingClass, final CtClass generated)
            throws NotFoundException, CannotCompileException {
        // Generate members for all methods ...
        LOG.trace("Generating class {}", generated.getName());
        generated.addInterface(bindingClass);

        for (Method method : properties.keySet()) {
            generateMethod(loader, generated, method, CT_NCS, "resolve");
        }
        if (keyMethod != null) {
            generateMethod(loader, generated, keyMethod.getKey(), CT_IIC, "resolveKey");
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

    @Override
    public Class<?> customizeLoading(final @NonNull Supplier<Class<?>> loader) {
        final CodecDataObjectCustomizer prev = CodecDataObjectBridge.setup(this);
        try {
            final Class<?> result = loader.get();

            /*
             * This a bit of magic to support NodeContextSupplier constants. These constants need to be resolved while
             * we have the information needed to find them -- that information is being held in this instance and we
             * leak it to a thread-local variable held by CodecDataObjectBridge.
             *
             * By default the JVM will defer class initialization to first use, which unfortunately is too late for
             * us, and hence we need to force class to initialize.
             */
            try {
                Class.forName(result.getName(), true, result.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new LinkageError("Failed to find newly-defined " + result, e);
            }

            return result;
        } finally {
            CodecDataObjectBridge.tearDown(prev);
        }
    }


    @NonNull NodeContextSupplier resolve(final @NonNull String methodName) {
        final Optional<Entry<Method, NodeContextSupplier>> found = properties.entrySet().stream()
                .filter(entry -> methodName.equals(entry.getKey().getName())).findAny();
        verify(found.isPresent(), "Failed to find property for %s in %s", methodName, this);
        return verifyNotNull(found.get().getValue());
    }

    @NonNull IdentifiableItemCodec resolveKey(final @NonNull String methodName) {
        return verifyNotNull(verifyNotNull(keyMethod, "No key method attached for %s in %s", methodName, this)
            .getValue());
    }

    private String codecHashCodeBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append("final int prime = 31;\n")
                .append("int result = 1;\n");

        for (Method method : properties.keySet()) {
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

        for (Method method : properties.keySet()) {
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
        for (Method method : properties.keySet()) {
            final String methodName = method.getName();
            sb.append("\n.add(\"").append(methodName).append("\", ").append(methodName).append("())");
        }

        return sb.append(";\n")
                .append('}').toString();
    }

    private static void generateMethod(final CodecClassLoader loader, final CtClass generated, final Method method,
            final CtClass contextType, final String resolveMethod) throws CannotCompileException, NotFoundException {
        LOG.trace("Generating for method {}", method);
        final String methodName = method.getName();
        final String methodArfu = methodName + "$$$ARFU";
        final String methodNcs = methodName + "$$$NCS";

        // NodeContextSupplier ...
        final CtField ncsField = new CtField(contextType, methodNcs, generated);
        ncsField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        generated.addField(ncsField, new StringBuilder().append(CodecDataObjectBridge.class.getName())
            .append('.').append(resolveMethod).append("(\"").append(methodName).append("\")").toString());

        // ... AtomicReferenceFieldUpdater ...
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
            .append("return (").append(retName).append(") codecMember(").append(methodArfu).append(", ")
                .append(methodNcs).append(");\n")
            .append('}').toString());
        getter.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        generated.addMethod(getter);
    }

    private static String utilClass(final Method method) {
        // We can either have objects or byte[], we cannot have Object[]
        return method.getReturnType().isArray() ? "Arrays" : "Objects";
    }
}