/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Source-specific mapping of prefixes to namespaces. This namespace is populated by all statements which have impact
 * on the XML namespace, for example {@code import}, {@code belongs-to} and really anywhere a {@code prefix} statement
 * is present.
 *
 * @see PrefixResolver
 */
public final class PrefixToModule extends ParserNamespace<String, QNameModule> {
    public static final @NonNull PrefixToModule INSTANCE = new PrefixToModule();

    private PrefixToModule() {
        super(NamespaceBehaviour.global(PrefixToModule.class));
    }
}
