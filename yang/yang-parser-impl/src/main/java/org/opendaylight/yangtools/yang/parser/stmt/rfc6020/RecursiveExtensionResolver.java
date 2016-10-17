/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;

/**
 * Thread-local hack to make recursive extensions work without too much hassle. The idea is that prior to instantiating
 * an extension, the definition object checks whether it is already present on the stack, recorded object is returned.
 *
 * If it is not, it will push itself to the stack as unresolved and invoke the constructor. The constructor's lowermost
 * class calls to this class and if the topmost entry is not resolved, it will leak itself.
 *
 * Upon return from the constructor, the topmost entry is removed and if the queue is empty, the thread-local variable
 * will be cleaned up.
 *
 * @author Robert Varga
 */
@Beta
public final class RecursiveExtensionResolver {
    private static final ThreadLocal<Deque<Entry<?, Object>>> STACK = new ThreadLocal<>();

    public static void beforeConstructor(final Object definition) {
        Deque<Entry<?, Object>> stack = STACK.get();
        if (stack == null) {
            stack = new ArrayDeque<>(1);
            STACK.set(stack);
        }

        stack.push(new SimpleEntry<>(definition, null));
    }

    public static void inConstructor(final Object effective) {
        final Deque<Entry<?, Object>> stack = STACK.get();
        if (stack != null) {
            final Entry<?, Object> top = Verify.verifyNotNull(stack.peek());
            if (top.getValue() == null) {
                top.setValue(effective);
            }
        }
    }

    public static void afterConstructor(final Object definition) {
        final Deque<Entry<?, Object>> stack = Verify.verifyNotNull(STACK.get());
        final Entry<?, Object> top = stack.pop();
        Verify.verify(definition.equals(top.getKey()));

        if (stack.isEmpty()) {
            STACK.remove();
        }
    }

    public static <T> T lookupInstance(final Object definition, final Class<T> requiredClass) {
        final Deque<Entry<?, Object>> stack = STACK.get();
        if (stack != null) {
            for (Entry<?, Object> e : stack) {
                if (definition.equals(e.getKey())) {
                    Preconditions.checkState(e.getValue() != null, "Object for %s is not resolved", definition);
                    return requiredClass.cast(e.getValue());
                }
            }
        }

        return null;
    }

    public static void cleanup() {
        STACK.remove();
    }
}
