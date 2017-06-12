/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.Arrays;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Creates a module dependency graph from provided {@link Module}s and
 * provides a {@link #sort(Module...)} method. It is topological sort and
 * returns modules in order in which they should be processed (e.g. if A imports
 * B, sort returns {B, A}).
 *
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.ModuleDependencySort} instead.
 */
@Deprecated
public final class ModuleDependencySort {
    /**
     * It is not desirable to instance this class
     */
    private ModuleDependencySort() {
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @param modules YANG modules
     * @return Sorted list of Modules. Modules can be further processed in
     *         returned order.
     */
    public static List<Module> sort(final Module... modules) {
        return sort(Arrays.asList(modules));
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @param modules YANG modules
     * @return Sorted list of Modules. Modules can be further processed in
     *         returned order.
     */
    public static List<Module> sort(final Iterable<Module> modules) {
        try {
            return org.opendaylight.yangtools.yang.model.util.ModuleDependencySort.sort(modules);
        } catch (IllegalArgumentException ex) {
            throw new YangValidationException(ex.getMessage(), ex.getCause());
        }
    }
}
