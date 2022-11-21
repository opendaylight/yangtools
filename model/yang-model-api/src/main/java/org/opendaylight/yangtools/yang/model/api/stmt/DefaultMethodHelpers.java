/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.base.VerifyException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Simple helper methods used in default implementations.
 */
final class DefaultMethodHelpers {
    private DefaultMethodHelpers() {
        // Hidden on purpose
    }

    static <E> @NonNull Optional<E> filterOptional(final @NonNull Optional<?> optional, final @NonNull Class<E> type) {
        return optional.filter(type::isInstance).map(type::cast);
    }

    static @NonNull InputEffectiveStatement verifyInputSubstatement(final EffectiveStatement<?, ?> stmt) {
        return verifySubstatement(stmt, InputEffectiveStatement.class);
    }

    static @NonNull OutputEffectiveStatement verifyOutputSubstatement(final EffectiveStatement<?, ?> stmt) {
        return verifySubstatement(stmt, OutputEffectiveStatement.class);
    }

    static <T extends EffectiveStatement<?, ?>> @NonNull T verifySubstatement(
            final EffectiveStatement<?, ?> stmt, final Class<T> type) {
        return stmt.findFirstEffectiveSubstatement(type).orElseThrow(
            () -> new VerifyException(stmt + " does not define a " + type.getSimpleName() + " substatement"));
    }
}
