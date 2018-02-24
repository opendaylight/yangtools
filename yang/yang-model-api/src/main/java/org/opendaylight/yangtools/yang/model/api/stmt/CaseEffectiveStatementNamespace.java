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
 * Namespace of available cases in a choice node. According to RFC7950 section 6.2.1:
 * <pre>
 *     All cases within a choice share the same case identifier
 *     namespace.  This namespace is scoped to the parent choice node.
 * </pre>
 *
 * @author Robert Varga
 */
@Beta
public abstract class CaseEffectiveStatementNamespace extends EffectiveStatementNamespace<CaseEffectiveStatement> {
    private CaseEffectiveStatementNamespace() {
        // Should never be instantiated
    }
}
