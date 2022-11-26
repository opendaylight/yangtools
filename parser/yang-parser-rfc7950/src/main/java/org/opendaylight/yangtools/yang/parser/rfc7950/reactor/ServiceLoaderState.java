/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.reactor;

import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.XPathSupport;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * State derived from ServiceLoader. We statically bind to this state. If you need more dynamics, you should not be
 * showing up here at all.
 */
@NonNullByDefault
final class ServiceLoaderState {
    static final class DefaultReactor {
        static final CrossSourceStatementReactor INSTANCE = RFC7950Reactors.defaultReactorBuilder().build();

        private DefaultReactor() {
            // Hidden on putpose
        }
    }

    static final class VanillaReactor {
        static final CrossSourceStatementReactor INSTANCE = RFC7950Reactors.vanillaReactorBuilder().build();

        private VanillaReactor() {
            // Hidden on putpose
        }
    }

    static final class XPath {
        static final XPathSupport INSTANCE = new XPathSupport(ServiceLoader.load(YangXPathParserFactory.class)
            .findFirst().orElseThrow(() -> new ExceptionInInitializerError("No YangXPathParserFactory found")));

        private XPath() {
            // Hidden on putpose
        }
    }

    private ServiceLoaderState() {
        // Hidden on putpose
    }
}
