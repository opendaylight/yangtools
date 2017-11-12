/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import static org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.sourceLocal;

import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public final class CustomInferencePipeline {
    public static final CrossSourceStatementReactor CUSTOM_REACTOR = RFC7950Reactors.defaultReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, ThirdPartyExtensionSupport.getInstance())
            .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, sourceLocal(ThirdPartyNamespace.class))
            .build();

    private CustomInferencePipeline() {
        throw new UnsupportedOperationException("Utility class");
    }
}
