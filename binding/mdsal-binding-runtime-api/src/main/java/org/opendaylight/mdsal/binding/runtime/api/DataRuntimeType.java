/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

/**
 * Marker interface for concrete {@link RuntimeType}s relevant when dealing data hierarchy. This interface does not have
 * a direct relationship with the {@code data tree} YANG construct, nor with {@code YangInstanceIdentifier} addressing.
 * Its primary role is differentiating between various types when considered in the context of
 * {@link RuntimeTypeContainer}'s child methods and related inferences. In particular, {@link DataRuntimeType}s are
 * relevant when considering whether or not a particular RuntimeType child is part of the data encoding (according to
 * the Binding Specification) to the {@code schema tree} layout.
 *
 * <p>
 * DataRuntimeTypes are considered part of the {@code data tree} information encoded by the Binding Specification and
 * thus need to be considered as child nodes when considering other data-bearing construct, such as
 * {@code NormalizedNode}. That relationship may not be straightforward, but notably it excludes constructs like
 * {@link NotificationRuntimeType}, {@link IdentityRuntimeType}, {@link ActionRuntimeType}, {@link GroupingRuntimeType},
 * and similar, as those exist in outside of {@code data tree} contract, but are still part of what
 * {@link RuntimeTypeContainer#bindingChild(JavaTypeName)} considers as child constructs.
 */
@Beta
public interface DataRuntimeType extends RuntimeType {
    // Marker interface, no further contract is specified
}
