/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link ListGenerator} with a key.
 */
final class EntryObjectGenerator extends ListGenerator {
    private final @NonNull KeyGenerator keyGenerator;

    @NonNullByDefault
    EntryObjectGenerator(final ListEffectiveStatement statement,  final AbstractCompositeGenerator<?, ?> parent,
            final KeyEffectiveStatement key) {
        super(statement, parent);
        keyGenerator = new KeyGenerator(key, parent, this);
    }

    @Override
    @NonNull KeyGenerator keyGenerator() {
        // guard against invocations during construction
        return verifyNotNull(keyGenerator);
    }
}
