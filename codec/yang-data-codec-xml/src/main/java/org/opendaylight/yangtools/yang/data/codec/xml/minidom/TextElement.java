/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An XML element containing text. YANG expectation is that this text is going to be treated as a value of, for example,
 * a {@code leaf}.
 */
public non-sealed interface TextElement extends Element {
    /**
     * Text of this element.
     *
     * @return Test string
     */
    @NonNull String text();
}
