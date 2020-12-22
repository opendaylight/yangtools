/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;

/**
 * A {@link GeneratedTransferObject} created for run-time representation of a {@code union}.
 */
@Beta
public interface RuntimeGeneratedUnion extends GeneratedTransferObject {
    /**
     * List of property names corresponding to individual {@code type} statements within this union. The ordering of
     * the returned list matches the ordering of the type statements.
     *
     * @return A list of property names.
     */
    @NonNull List<String> typePropertyNames();
}
