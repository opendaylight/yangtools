/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * RFC7952 metadata counterpart to a {@link NormalizedNode}. This interface is meant to be used as a companion to
 * a NormalizedNode instance, hence it does not support iterating over its structure like it is possible with
 * {@link NormalizedNode#getValue()}.
 *
 * @author Robert Varga
 */
@Beta
public interface NormalizedMetadata extends Identifiable<PathArgument>, Immutable {

    Optional<? extends Object> findEntry(QName metadata);

}
