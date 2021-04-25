/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;

/**
 * Namespace of available extensions. According to RFC7950 section 6.2.1:
 * <pre>
 *     All extension names defined in a module and its submodules share
 *     the same extension identifier namespace.
 * </pre>
 *
 * @author Robert Varga
 */
@Beta
public abstract class ExtensionEffectiveStatementNamespace
        extends EffectiveStatementNamespace<ExtensionEffectiveStatement> {
    private ExtensionEffectiveStatementNamespace() {
        // Should never be instantiated
    }
}
