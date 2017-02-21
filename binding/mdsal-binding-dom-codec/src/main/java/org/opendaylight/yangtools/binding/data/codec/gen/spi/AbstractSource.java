/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.gen.spi;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.Type;

public abstract class AbstractSource {

    private final Set<StaticConstantDefinition> staticConstants = new HashSet<>();

    public final <T> void staticConstant(final String name, final Class<T> type, final T value) {
        staticConstants.add(new StaticConstantDefinition(name, type, value));
    }

    public final Set<StaticConstantDefinition> getStaticConstants() {
        return Collections.unmodifiableSet(staticConstants);
    }

    protected final CharSequence invoke(final CharSequence object, final String methodName, final Object... args) {
        StringBuilder builder = new StringBuilder();
        if (object != null) {
            builder.append(object);
            builder.append('.');
        }
        builder.append(methodName);
        builder.append('(');

        UnmodifiableIterator<Object> iterator = Iterators.forArray(args);
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(')');
        return builder;
    }

    protected final CharSequence assign(final String var, final CharSequence value) {
        return assign((String) null, var, value);
    }

    protected final CharSequence assign(final String type, final String var, final CharSequence value) {
        StringBuilder builder = new StringBuilder();
        if(type != null) {
            builder.append(type);
            builder.append(' ');
        }
        builder.append(var);
        builder.append(" = ");
        builder.append(value);
        return builder;
    }

    protected final CharSequence assign(final Type type, final String var, final CharSequence value) {
        return assign(type.getFullyQualifiedName(), var, value);
    }

    protected final CharSequence cast(final Type type, final CharSequence value) {
        return cast(type.getFullyQualifiedName(), value);
    }

    protected final CharSequence forEach(final String iterable,final String iteratorName, final String valueType,final String valueName, final CharSequence body) {
        StringBuilder b = new StringBuilder();
        b.append(statement(assign(java.util.Iterator.class.getName(), iteratorName,invoke(iterable, "iterator"))));
        b.append("while (").append(invoke(iteratorName, "hasNext")).append(") {\n");
        b.append(statement(assign(valueType, valueName,cast(valueType, invoke(iteratorName, "next")))));
        b.append(body);
        b.append("\n}\n");
        return b;
    }

    protected final CharSequence statement(final CharSequence statement) {
        return new StringBuilder().append(statement).append(";\n");
    }

    protected final CharSequence cast(final String type, final CharSequence value) {
        StringBuilder builder = new StringBuilder();
        builder.append("((");
        builder.append(type);
        builder.append(") ");
        builder.append(value);
        builder.append(')');
        return builder;
    }

}
