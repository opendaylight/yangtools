/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.AbstractQName;

@NonNullByDefault
final class ModuleNamingStrategy extends YangIdentifierClassNamingStrategy {
    ModuleNamingStrategy(final AbstractQName name) {
        super(name);
    }

    @Override
    @Nullable ClassNamingStrategy fallback() {
        return null;
    }
}
