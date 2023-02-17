/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Top-level view of a tree of {@link NormalizedNode}s. The view is guaranteed to be consistent, except for application
 * of modicum of sanity around of {@code type union}, {@code require-instance} and {@code mount-point} YANG constructs.
 * These are allowed to acknowledge, in principle, there is no globally-consistent view of data available due to the
 * distributed nature of real-world systems. In particular:
 * <ul>
 *   <li>{@code require-instance} may be ignored and be inconsistent if they point outside of this particular tree</li>
 *   <li>related, {@code type union} leaves are allowed to have been parsed without the regard to the datastore contents
 *       and may require re-interpretation to make them accurate to the contents of a particular datastore given a point
 *       in time. Most notably unions having a {@code require-instance} are allowed to interpret data at hand as if the
 *       datastore contents referenced <em>are</em> present</li>
 *   <li>
 * </ul>
 */
public sealed interface NormalizedTree extends NormalizedData
        // FIXME: also MountPoint
        permits NormalizedDatastore, NormalizedNotification, NormalizedInput, NormalizedOutput, NormalizedYangData {

}
