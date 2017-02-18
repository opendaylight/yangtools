/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * API exposed by generator plugins, i.e. pluggable pieces of code which want to create code (and other) files from an
 * {@link org.opendaylight.yangtools.yang.model.api.EffectiveModelContext}.
 *
 * <p>
 * The primary entry point is {@link org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory}, which needs
 * to be implemented for bootstrapping a particular generator. Such implementations are discovered using normal
 * discovery methods, for example they <b>should</b> always be published as {@link java.util.ServiceLoader} services.
 */
package org.opendaylight.yangtools.plugin.generator.api;