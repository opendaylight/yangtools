/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;

/**
 * Abstract base class for {@link ArgumentParser} implementations that need namespace binding to {@link QNameModule}
 * of the current module.
 *
 * @since 14.0.22
 */
@Beta
@NonNullByDefault
abstract class AbstractArgumentParser<@NonNull T> implements ArgumentParser<T> {
    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    /**
     * Add attributes to a {@link ToStringHelper}.
     *
     * @param helper the {@link ToStringHelper}
     * @return the {@link ToStringHelper}
     */
    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
}
