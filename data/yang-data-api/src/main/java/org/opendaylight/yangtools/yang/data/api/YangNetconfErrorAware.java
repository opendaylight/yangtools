/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface exposed from objects which have some association with {@link YangNetconfError}.
 */
@Beta
@NonNullByDefault
public interface YangNetconfErrorAware {
    /**
     * Return the {@link YangNetconfError}s associated with this objects.
     *
     * @return Associated YangNetconfErrors
     */
    List<YangNetconfError> getNetconfErrors();
}
