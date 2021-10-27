/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.common.NetconfError;

/**
 * Baseline interface for metadata associated with a NETCONF notion of an 'error'. Except what NETCONF regards as
 * an 'error', really means 'notable event' in that it can either be a warning or an error. No warnings were defined
 * at the time this distinction was made.
 */
@Beta
@Value.Immutable(copy = false)
@Value.Style(strictBuilder = true, allowedClasspathAnnotations = {
    SuppressWarnings.class, Generated.class, SuppressFBWarnings.class,
})
public interface YangNetconfError extends NetconfError<@NonNull YangNetconfError> {
    @Override
    default Class<@NonNull YangNetconfError> representation() {
        return YangNetconfError.class;
    }

    @Override
    @Nullable YangInstanceIdentifier path();

    @Override
    @Nullable YangErrorInfo<?, ?> info();
}
