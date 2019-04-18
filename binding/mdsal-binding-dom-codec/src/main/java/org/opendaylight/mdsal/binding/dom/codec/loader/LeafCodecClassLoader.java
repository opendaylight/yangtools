/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.loader;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

// A leaf class loader, binding together a root class loader and some other class loader
final class LeafCodecClassLoader extends CodecClassLoader {
    static {
        verify(registerAsParallelCapable());
    }

    private final @NonNull ClassLoader target;
    private final @NonNull RootCodecClassLoader root;

    LeafCodecClassLoader(final RootCodecClassLoader root, final ClassLoader target) {
        super(root);
        this.root = requireNonNull(root);
        this.target = requireNonNull(target);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return target.loadClass(name);
    }

    @Override
    CodecClassLoader findClassLoader(final Class<?> bindingClass) {
        final ClassLoader bindingTarget = bindingClass.getClassLoader();
        return target.equals(bindingTarget) ? this : root.findClassLoader(bindingClass);
    }
}
