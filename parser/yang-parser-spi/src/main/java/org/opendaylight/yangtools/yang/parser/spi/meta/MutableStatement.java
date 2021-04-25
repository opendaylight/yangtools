/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

/**
 * Mutable statement interface. Each mutable statement must be sealed as the
 * last step of statement parser processing.
 */
public interface MutableStatement {
    /**
     * Finish statement and make it immutable. After this method is invoked, any
     * further modifications of current object are not allowed.
     */
    void seal();
}
