/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.spi;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.Type;

/**
 * An abstract source.
 *
 * @deprecated This class is superseded by an internal implementation.
 */
@Deprecated
public abstract class AbstractSource {
    private final Set<StaticConstantDefinition> staticConstants = new HashSet<>();

    public final <T> void staticConstant(final String name, final Class<T> type, final T value) {
        staticConstants.add(new StaticConstantDefinition(name, type, value));
    }

    public final Set<StaticConstantDefinition> getStaticConstants() {
        return Collections.unmodifiableSet(staticConstants);
    }

    private static StringBuilder commonInvoke(final CharSequence object, final String methodName) {
        final StringBuilder sb = new StringBuilder();
        if (object != null) {
            sb.append(object).append('.');
        }
        return sb.append(methodName).append('(');
    }

    protected static final CharSequence invoke(final CharSequence object, final String methodName, final Object arg) {
        return commonInvoke(object, methodName).append(arg).append(')');
    }

    protected static final CharSequence invoke(final CharSequence object, final String methodName,
            final Object... args) {
        final StringBuilder sb = commonInvoke(object, methodName);

        final UnmodifiableIterator<Object> iterator = Iterators.forArray(args);
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        return sb.append(')');
    }

    protected static final CharSequence assign(final String var, final CharSequence value) {
        return assign((String) null, var, value);
    }

    protected static final CharSequence assign(final String type, final String var, final CharSequence value) {
        final StringBuilder sb = new StringBuilder();
        if (type != null) {
            sb.append(type).append(' ');
        }
        return sb.append(var).append(" = ").append(value);
    }

    protected static final CharSequence assign(final Class<?> type, final String var, final CharSequence value) {
        return assign(type.getName(), var, value);
    }

    protected static final CharSequence assign(final Type type, final String var, final CharSequence value) {
        return assign(type.getFullyQualifiedName(), var, value);
    }

    protected static final CharSequence cast(final Class<?> type, final CharSequence value) {
        return cast(type.getName(), value);
    }

    protected static final CharSequence cast(final Type type, final CharSequence value) {
        return cast(type.getFullyQualifiedName(), value);
    }

    protected static final CharSequence cast(final String type, final CharSequence value) {
        return "((" + type + ") " + value + ')';
    }

    protected static final CharSequence forEach(final String iterable, final String iteratorName,
            final String valueType, final String valueName, final CharSequence body) {
        return new StringBuilder()
                .append(statement(assign(Iterator.class, iteratorName, invoke(iterable, "iterator"))))
                .append("while (").append(invoke(iteratorName, "hasNext")).append(") {\n")
                .append(statement(assign(valueType, valueName, cast(valueType, invoke(iteratorName, "next")))))
                .append(body)
                .append("\n}\n");
    }

    protected static final CharSequence statement(final CharSequence statement) {
        return new StringBuilder(statement).append(";\n");
    }
}
