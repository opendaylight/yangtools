/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;

@Beta
@ThreadSafe
public interface XPathParserFactory {
    /**
     * Return an {@link XPathParser} compliant with {@link XPathParserNumberCompliance}.
     *
     * @param numberCompliance Requested XPath number compliance
     * @return An XPathParser
     * @throws NullPointerException if {@code numberCompliance} is null
     * @throws IllegalArgumentException if {@code numberCompliance} is not supported.
     */
    XPathParser parserWithCompliance(XPathParserNumberCompliance numberCompliance);
}
