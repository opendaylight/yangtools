/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local hack to make recursive extensions work without too much hassle. The idea is that prior to instantiating
 * an extension, the definition object checks whether it is already present on the stack, recorded object is returned.
 *
 * <p>
 * If it is not, it will push itself to the stack as unresolved and invoke the constructor. The constructor's lowermost
 * class calls to this class and if the topmost entry is not resolved, it will leak itself.
 *
 * <p>
 * Upon return from the constructor, the topmost entry is removed and if the queue is empty, the thread-local variable
 * will be cleaned up.
 *
 * <p>
 * WARNING: BE CAREFUL WHEN USING THIS CLASS. IT LEAKS OBJECTS WHICH ARE NOT COMPLETELY INITIALIZED.
 *
 * <p>
 * WARNING: THIS CLASS EAVES THREAD-LOCAL RESIDUE. MAKE SURE IT IS OKAY OR CALL {@link #cleanup()} IN APPROPRIATE
 *          PLACES.
 *
 * <p>
 * THIS CLASS IS EXTREMELY DANGEROUS (okay, not as much as sun.misc.unsafe). YOU HAVE BEEN WARNED. IF SOMETHING BREAKS
 * IT IS PROBABLY YOUR FAULT AND YOU ARE ON YOUR OWN.
 *
 * @author Robert Varga
 */
@Beta
@ThreadSafe
public final class RecursiveObjectLeaker {
    // Logging note. Only keys passed can be logged, as objects beng resolved may not be properly constructed.
    private static final Logger LOG = LoggerFactory.getLogger(RecursiveObjectLeaker.class);

    // Initial value is set to null on purpose, so we do not allocate anything (aside the map)
    private static final ThreadLocal<Deque<Entry<?, Object>>> STACK = new ThreadLocal<>();

    private RecursiveObjectLeaker() {
        throw new UnsupportedOperationException();
    }

    // Key is checked for identity
    public static void beforeConstructor(final Object key) {
        Deque<Entry<?, Object>> stack = STACK.get();
        if (stack == null) {
            // Biased: this class is expected to be rarely and shallowly used
            stack = new ArrayDeque<>(1);
            STACK.set(stack);
        }

        LOG.debug("Resolving key {}", key);
        stack.push(new SimpleEntry<>(key, null));
    }

    // Can potentially store a 'null' mapping. Make sure cleanup() is called
    public static void inConstructor(final Object obj) {
        final Deque<Entry<?, Object>> stack = STACK.get();
        if (stack != null) {
            final Entry<?, Object> top = stack.peek();
            if (top != null) {
                if (top.getValue() == null) {
                    LOG.debug("Resolved key {}", top.getKey());
                    top.setValue(obj);
                }
            } else {
                LOG.info("Cleaned stale empty stack", new Exception());
                STACK.set(null);
            }
        } else {
            LOG.trace("No thread stack");
        }
    }

    // Make sure to call this from a finally block
    public static void afterConstructor(final Object key) {
        final Deque<Entry<?, Object>> stack = STACK.get();
        checkState(stack != null, "No stack allocated when completing %s", key);

        final Entry<?, Object> top = stack.pop();
        if (stack.isEmpty()) {
            LOG.trace("Removed empty thread stack");
            STACK.set(null);
        }

        checkState(key == top.getKey(), "Expected key %s, have %s", top.getKey(), key);
        checkState(top.getValue() != null, "");
    }

    // BEWARE: this method returns incpmpletely-initialized objects (that is the purpose of this class).
    //
    //         BE VERY CAREFUL WHAT OBJECT STATE YOU TOUCH
    public static @Nullable <T> T lookup(final Object key, final Class<T> requiredClass) {
        final Deque<Entry<?, Object>> stack = STACK.get();
        if (stack != null) {
            for (Entry<?, Object> e : stack) {
                // Keys are treated as identities
                if (key == e.getKey()) {
                    checkState(e.getValue() != null, "Object for %s is not resolved", key);
                    LOG.debug("Looked up key {}", e.getKey());
                    return requiredClass.cast(e.getValue());
                }
            }
        }

        return null;
    }

    // Be sure to call this in from a finally block when bulk processing is done, so that this class can be unloaded
    public static void cleanup() {
        STACK.remove();
        LOG.debug("Removed thread state");
    }
}
