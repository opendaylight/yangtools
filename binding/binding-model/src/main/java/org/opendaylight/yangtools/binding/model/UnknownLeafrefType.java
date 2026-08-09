/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;

/**
 * A {@link ReturnType} representing a {@code type leafref} which is using a relative {@code path} that points outside
 * of its defining construct. This typically occurs within {@code grouping}s, like
 * {@snippet lang="yang" :
 *   module foo {
 *     grouping bar {
 *       leaf baz {
 *         type leafref {
 *           path ../xyzzy; // no xyzzy in grouping, so this references siblings of uses
 *         }
 *       }
 *     }
 *
 *     uses bar;
 *
 *     leaf xyzzy {
 *       type string;
 *     }
 *   }
 *
 *   module qux {
 *     import foo {
 *       prefix foo;
 *     }
 *
 *     uses foo:bar;
 *
 *     leaf xyzzy {
 *       type uint64;
 *     }
 *   }
 * }
 *
 * <p>In this particular example, {@code Bar} has to define a {@code getBaz()} method which is usable by both modules,
 * so that both {@code FooData extends Bar} and {@code QuxData extends Bar} are valid Java. The actual type must be
 * specialized in each concrete user, so that {@code FooData} defines narrows the method return type to
 * {@code String getBaz()} and {@code QuzData} narrows it to {@code Uint64 getBaz()}.
 *
 * @since 16.0.0
 */
// FIXME: clarify the above so that binding-generator deals with all the non-exported groupings as much as possible
@NonNullByDefault
public final class UnknownLeafrefType implements ReturnType {
    /**
     * The singleton instance.
     */
    public static final UnknownLeafrefType INSTANCE = new UnknownLeafrefType();

    private static final TypeName OBJECT = TypeName.ofClass(Object.class);

    private UnknownLeafrefType() {
        // hidden on purpose
    }

    @Override
    public TypeName name() {
        return OBJECT;
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public String toString() {
        return UnknownLeafrefType.class.getSimpleName();
    }
}
