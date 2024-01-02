/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Base interfaces for identifying and source of YANG and YIN models. Two main entry points are
 * <ol>
 *   <li>{@link SourceIdentifier}, i.e. how a model source is known</li>
 *   <li>{@link SourceRepresentation}, i.e. its format</li>
 * </ol>
 */
package org.opendaylight.yangtools.yang.model.api.source;