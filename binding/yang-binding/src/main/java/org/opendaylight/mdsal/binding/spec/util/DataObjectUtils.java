/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.util;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Various utility methods for bridging generated interfaces and human-written code. Methods in this class should be
 * statically imported into user code, so that the resulting code is expressive:
 *
 * <code>
 * import static org.opendaylight.yangtools.yang.binding.DataObjectUtils.nullToEmpty;
 *
 * FooWithList foo;
 * for (ListItem item : nulltoEmpty(foo.getList())) {
 *     // ..
 * }
 * </code>
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class DataObjectUtils {
    private DataObjectUtils() {

    }

    /**
     * Utility method for dealing with nullable Lists returned from Binding DTOs.
     *
     * @param input input list, may be null
     * @return Input list or an empty list.
     */
    public static <T> List<T> nullToEmpty(final @Nullable List<T> input) {
        return input != null ? input : ImmutableList.of();
    }
}
