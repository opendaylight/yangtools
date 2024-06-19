/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * {@link java.lang.ClassLoader} support for Binding-related runtime-loaded code. This package provides one core class,
 * {@link BindingClassLoader}, which allows lookup of compile-time-generated Binding classes for the purpose of
 * referencing them within code generators and which serves as the ClassLoader holding runtime-generated codecs.
 *
 * <p>
 * While the interfaces and classes in this package may be publicly accessible, they are an implementation detail and
 * may change incompatibly at any time.
 */
package org.opendaylight.mdsal.binding.loader;