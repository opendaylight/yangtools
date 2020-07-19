/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;

/**
 * State derived from ServiceLoader. We statically bind to this state. If you need more dynamics, you should not be
 * showing up here at all.
 */
@NonNullByDefault
final class ServiceLoaderState {
    static final class Generator {
        static final BindingRuntimeGenerator INSTANCE = ServiceLoader.load(BindingRuntimeGenerator.class).findFirst()
                .orElseThrow(() -> new ExceptionInInitializerError("No BindingRuntimeGenerator found"));
    }

    static final class ParserFactory {
        static final YangParserFactory INSTANCE = ServiceLoader.load(YangParserFactory.class).findFirst()
                .orElseThrow(() -> new ExceptionInInitializerError("No YangParserFactory found"));
    }
}
