/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.util.List;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

public final class GeneratorUtil {
    private GeneratorUtil() {
        // Hidden on purpose
    }

    /**
     * Checks if the constant with the name <code>constName</code> is in the list of the constant definition for
     * <code>genTO</code>.
     *
     * @param constName string with the name of constant which is sought
     * @param genTO generated transfer object in which is <code>constName</code> sought
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>constName</code> is in the list of the
     *         constant definition for <code>genTO</code></li>
     *         <li>false - in other cases</li>
     *         </ul>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>constName</code> equals <code>null</code></li>
     *             <li>if <code>genTO</code> equals <code>null</code></li>
     *             </ul>
     */
    static boolean isConstantInTO(final String constName, final GeneratedTransferObject genTO) {
        if (constName == null || genTO == null) {
            throw new IllegalArgumentException();
        }
        List<Constant> consts = genTO.getConstantDefinitions();
        for (Constant cons : consts) {
            if (cons.getName().equals(constName)) {
                return true;
            }
        }
        return false;
    }

    static boolean strictTypeEquals(final Type type1, final Type type2) {
        if (!type1.equals(type2)) {
            return false;
        }
        if (type1 instanceof ParameterizedType param1) {
            return type2 instanceof ParameterizedType param2
                && param1.getActualTypeArguments().equals(param2.getActualTypeArguments());
        }
        return !(type2 instanceof ParameterizedType);
    }
}
