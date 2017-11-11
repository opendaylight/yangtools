/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.reactor;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

/**
 * Utility class holding entrypoints for assembling RFC6020/RFC7950 statement {@link CrossSourceStatementReactor}s.
 *
 * @author Robert Varga
 */
@Beta
public final class RFC7950Reactors {
    private static final CrossSourceStatementReactor DEFAULT_RFC6020_RFC7950_REACTOR =
            YangInferencePipeline.newReactorBuilder().build();
    // FIXME: this is incorrect, as it includes semver support.
    private static final CrossSourceStatementReactor VANILLA_RFC6020_RFC7950_REACTOR =
            YangInferencePipeline.newReactorBuilder().build();

    private RFC7950Reactors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a pre-built {@link CrossSourceStatementReactor} supporting RFC6020 and RFC7950, along with OpenConfig's
     * semantic-version extension. This is useful for parsing near-vanilla YANG models while providing complete
     * support for semantic versions.
     *
     * @return A shared reactor instance.
     */
    public static CrossSourceStatementReactor defaultReactor() {
        return DEFAULT_RFC6020_RFC7950_REACTOR;
    }

    /**
     * Returns a pre-built {@link CrossSourceStatementReactor} supporting both RFC6020 and RFC7950. This is useful
     * for parsing vanilla YANG models without any semantic support for extensions. Notably missing is the semantic
     * version extension, hence attempts to use semantic version mode will cause failures.
     *
     * @return A shared reactor instance.
     */
    public static CrossSourceStatementReactor vanillaReactor() {
        return VANILLA_RFC6020_RFC7950_REACTOR;
    }
}
