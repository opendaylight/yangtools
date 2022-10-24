/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.xpath.impl;

import com.google.common.collect.ImmutableBiMap;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;

final class TestUtils {

    private TestUtils() {
        // utility class
    }

    static final QNameModule DEFAULT_NS = QNameModule.create(XMLNamespace.of("defaultns"));

    static final YangNamespaceContext NAMESPACE_CONTEXT = new BiMapYangNamespaceContext(ImmutableBiMap.of(
            "def", DEFAULT_NS,
            "foo", QNameModule.create(XMLNamespace.of("foo")),
            "bar", QNameModule.create(XMLNamespace.of("bar"))));

}
