/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.file.api;

import com.google.common.annotations.Beta;
import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A single consistent YANG module. Provides namespace identification (via {@link #getIdentifier()}, which provides
 * name, namespace and revision identification about the packaged module.
 *
 * <p>
 * Implementations of this interface are expected to be discoverable via {@link ServiceLoader} mechanism.
 *
 * FIXME: expose constituent files and their representations
 *
 */
@Beta
@NonNullByDefault
public interface YangModule extends Immutable, Identifiable<QName> {
    /**
     * Returns YANG module name, as a composite {@link QName}. Module's namespace and revision maps to
     * {@link QName#getModule()} and module name maps to {@link QName#getLocalName()}.
     *
     * @return YANG module name.
     */
    @Override
    QName getIdentifier();
}
