/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.function.BiFunction;

/**
 * Utility helping to implement readable equals() methods.
 *
 * <p>Usage:
 * <pre>{@code
 * @Override
 *      public boolean equals(Object obj) {
 *          return EvenMoreObjects.equalsHelper(this, obj,
 *              (one, another) -> Objects.equals(one.name, another.name) && Objects.equals(one.age, another.age));
 *      }
 * }</pre>
 *
 * <p>See <a href="https://github.com/google/guava/issues/2521">Guava issue proposing contributing this</a>.
 *
 * @see MoreObjects
 *
 * @author Michael Vorburger, Red Hat
 */
@Beta
public final class EvenMoreObjects {

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

    private EvenMoreObjects() { }
}
