/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangErrorInfo;

/**
 * A {@link YangErrorInfo} modeled as a {@link NormalizableAnydata}.
 */
@Beta
@NonNullByDefault
public interface NormalizedErrorInfo extends YangErrorInfo<NormalizedErrorInfo, NormalizedAnydata> {
    @Override
    default Class<NormalizedErrorInfo> representation() {
        return NormalizedErrorInfo.class;
    }

    @Override
    default Class<NormalizedAnydata> bodyRepresentation() {
        return NormalizedAnydata.class;
    }

    @Override
    NormalizedAnydata body();
}
