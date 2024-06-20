/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.framework.Constants;

/**
 * Additional interface for exposing linear generation of the backing effective model. Implementations of this interface
 * are expected to be effectively-immutable.
 *
 * @param <S> service type
 */
@Beta
@NonNullByDefault
public interface ModelGenerationAware<S> extends Immutable {

    Uint64 generation();

    @NonNull S service();

    /**
     * Get service ranking based on the generation. Higher generation results in a higher ranking.
     *
     * @return Ranging for use with {@link Constants#SERVICE_RANKING}
     */
    default int getServiceRanking() {
        return computeServiceRanking(generation().longValue());
    }

    /**
     * Calculate service ranking based on generation. Higher generation results in a higher ranking.
     *
     * @param generation generation number, treated as an unsigned long
     * @return Ranging for use with {@link Constants#SERVICE_RANKING}
     */
    static int computeServiceRanking(final long generation) {
        return generation >= 0 && generation <= Integer.MAX_VALUE ? (int) generation : Integer.MAX_VALUE;
    }
}
