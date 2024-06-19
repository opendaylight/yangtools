/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.di;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;

@Beta
@Singleton
public final class SingletonBindingRuntimeGenerator extends DefaultBindingRuntimeGenerator {
    @Inject
    public SingletonBindingRuntimeGenerator() {
        // exposed for DI
    }
}
