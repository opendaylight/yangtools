/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6241_YANG_MODULE;

/**
 * {@code bad-attribute}, {@link #value()} is the name of the attribute.
 */
public final class BadAttribute extends ErrorInfo<QName> {
    public static final QName QNAME = QName.create(RFC6241_YANG_MODULE, "bad-attribute").intern();

    public BadAttribute(final QName value) {
        super(QNAME, value);
    }
}