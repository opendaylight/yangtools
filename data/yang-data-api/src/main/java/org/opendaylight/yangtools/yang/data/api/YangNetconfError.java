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
import java.util.List;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.Nullable;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.NetconfError;

/**
 * Baseline interface for metadata associated with a NETCONF notion of an 'error'. Except what NETCONF regards as
 * an 'error', really means 'notable event' in that it can either be a warning or an error. No warnings were defined
 * at the time this distinction was made.
 */
@Beta
@Value.Immutable(copy = false)
@Value.Style(stagedBuilder = true, allowedClasspathAnnotations = {
    SuppressWarnings.class, Generated.class, SuppressFBWarnings.class,
})
// FIXME: 8.0.0: Split this interface into two:
//               - yang.common.NetconfError, which does not have a builder sets up the stage
//               - data.api.schema.NormalizedNetconfError
public interface YangNetconfError extends NetconfError {
    @Override
    ErrorSeverity severity();

    @Override
    ErrorType type();

    @Override
    ErrorTag tag();

    @Override
    @Nullable String message();

    @Override
    @Nullable String appTag();

    @Override
    @Nullable YangInstanceIdentifier path();

    /**
     * Return this error's additional info.
     *
     * @return Additional info.
     */
    @Override
    // FIXME: 8.0.0: return NetconfErrorInfoRepresentation
    List<YangErrorInfo> info();
}
