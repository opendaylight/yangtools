/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An SPI-level interface to find the schema source for a particular YANG module, as packaged in the final artifact.
 * The module must be part of the current resolution context.
 */
@Beta
@FunctionalInterface
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_INTERFACE", justification = "Migration to new place")
public interface ModuleResourceResolver extends org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver {

}
