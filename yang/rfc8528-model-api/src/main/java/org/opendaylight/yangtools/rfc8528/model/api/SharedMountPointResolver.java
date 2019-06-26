/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface provider resolving a mount point label, as defined by the effective module statement. This interface
 * support shared schema mount points, i.e. those which are fully defined by the label.
 */
@Beta
@NonNullByDefault
public interface SharedMountPointResolver {
    /**
     * Resolve the SchemaContext for specified shared schema mount point label.
     *
     * @param label Mount point label, as defined in the effective model
     * @return Optional SchemaContext, empty if the label is not defined
     * @throws NullPointerException if label is null
     */
    // FIXME: 5.0.0: require EffectiveModelContext here
    Optional<? extends SchemaContext> resolveLabel(QName label);

    /**
     * Return a {@link SharedMountPointResolver} which reports every label as undefined.
     *
     * @return A resolver instance
     */
    static SharedMountPointResolver noop() {
        return NoopSharedMountPointResolver.INSTANCE;
    }
}
