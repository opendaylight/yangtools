/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.util.function.BiFunction;

/**
 * Utility helping to implement readable equals() methods.
 *
 * <p>See the example equals() of class Thing in the MoreObjects2Test for how to use this.
 *
 * <p>See <a href="https://github.com/google/guava/issues/2521">Guava issue proposing contributing this</a>.
 *
 * @author Michael Vorburger, Red Hat
 */
public final class MoreObjects2 {

    @SuppressWarnings("unchecked")
    public static <T> boolean equalsHelper(T self, Object other, BooleanEqualsFunction<T> equals) {
        if (other == null) {
            return false;
        }
        if (other == self) {
            return true;
        }
        if (self.getClass() != other.getClass()) {
            return false;
        }
        return equals.apply(self, (T) other);
    }

    @FunctionalInterface
    public interface BooleanEqualsFunction<T> extends BiFunction<T, T, Boolean> { }

    private MoreObjects2() { }
}
