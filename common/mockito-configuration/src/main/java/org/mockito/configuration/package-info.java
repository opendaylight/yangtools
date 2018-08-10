/*
 * Copyright (c) 2016 Red Hat and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Black magic Mockito utility.
 *
 * <p>This artifact is "black magic" because by merely depending on it (by its
 * presence on the classpath, without ever using any of its classes), all usages
 * of core Mockito.mock() in your existing tests will suddenly behave
 * differently automagically all by themselves: They will throw
 * ThrowsUnstubbedMethodException instead of returning null etc. as in default
 * Mockito. (This "magic" is also the technical reason why this is package
 * org.mockito.configuration instead of org.opendaylight.*)
 *
 * <p>TODO Point to the "other/new artifact with more sane Mockito helper, with
 * explicit API.
 *
 * @see org.mockito.configuration.MockitoConfiguration
 * @see org.mockito.configuration.IMockitoConfiguration
 */
package org.mockito.configuration;
