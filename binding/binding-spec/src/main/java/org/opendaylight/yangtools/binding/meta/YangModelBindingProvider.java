/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provider of YangModuleInfo for specified package / model. Implementations of this interface should be discoverable
 * via {@link ServiceLoader}.
 */
@NonNullByDefault
public interface YangModelBindingProvider {
    /**
     * Returns YangModuleInfo associated with a package.
     *
     * @return YangModuleInfo associated with a package.
     */
    YangModuleInfo getModuleInfo();
}
