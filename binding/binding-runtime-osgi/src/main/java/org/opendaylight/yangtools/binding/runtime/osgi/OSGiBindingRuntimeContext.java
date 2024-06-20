/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.osgi;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

@Beta
public interface OSGiBindingRuntimeContext extends ModelGenerationAware<BindingRuntimeContext> {
    // Nothing else
}
