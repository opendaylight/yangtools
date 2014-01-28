/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import org.opendaylight.yangtools.sal.binding.generator.util.ClassLoaderUtils;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public abstract class GeneratedClassLoadingStrategy {

    private static final GeneratedClassLoadingStrategy TCCL_STRATEGY = new TCCLClassLoadingStrategy();

    private static final GeneratedClassLoadingStrategy ALWAYS_FAIL_STRATEGY = new GeneratedClassLoadingStrategy() {

        @Override
        public Class<?> loadClass(String fullyQualifiedName) throws ClassNotFoundException {
            throw new ClassNotFoundException(fullyQualifiedName);
        }
    };

    public Class<?> loadClass(Type type) throws ClassNotFoundException {
        return loadClass(type.getFullyQualifiedName());
    }

    public abstract Class<?> loadClass(String fullyQualifiedName) throws ClassNotFoundException;

    public static final GeneratedClassLoadingStrategy getTCCLClassLoadingStrategy() {
        return TCCL_STRATEGY;
    }

    public static final GeneratedClassLoadingStrategy getAlwaysFailClassLoadingStrategy() {
        return ALWAYS_FAIL_STRATEGY;
    }

    private static final class TCCLClassLoadingStrategy extends GeneratedClassLoadingStrategy {

        @Override
        public Class<?> loadClass(String fullyQualifiedName) throws ClassNotFoundException {
            return ClassLoaderUtils.loadClassWithTCCL(fullyQualifiedName);
        }
    }
}
