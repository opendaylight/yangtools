/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import com.google.common.annotations.Beta;

import javassist.CtClass;

/**
 * Interface allowing customization of classes after loading.
 */
@Beta
public interface ClassCustomizer {
    /**
     * Customize a class.
     *
     * @param cls Class to be customized
     * @throws Exception when a problem ensues.
     */
    void customizeClass(CtClass cls) throws Exception;
}
