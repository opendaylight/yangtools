/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Common interface for all YANG XPath constant expressions. Each constant has a unique {@link QName}, which acts as its
 * globally-unique identifier.
 *
 * @author Robert Varga
 */
@Beta
public interface YangConstantExpr<T> extends YangExpr, Identifiable<QName> {
    /**
     * Return this constant's value.
     *
     * @return this constant's value.
     */
    // FIXME: this seems dicey w.r.t. empty nodeset constant.
    T getValue();
}
