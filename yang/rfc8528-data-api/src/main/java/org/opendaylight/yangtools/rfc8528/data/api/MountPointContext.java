/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

/**
 * A context of either an explicit (RFC8528 Schema Mount instance) or implicit (system root). It encapsulates a data
 * {@link org.opendaylight.yangtools.yang.model.api.EffectiveModelContext} and information resident in
 * {@code schema-mounts} within this hierarchy.
 *
 * <p>
 * Note this interface should be part of yang-data-api, as it really defines how a NormalizedNode-containerized data
 * operates w.r.t. mount points. Further evolution is expected.
 */
/*
 * FIXME: 7.0.0: consider yang-data-api integration
 *
 * The above note does not give the subject enough attention. RFC8528 redefines the YANG data metamodel is significant
 * ways in that it ties it with RFC8525/RFC7895. The content of 'schema-mounts' is critical to interpreting
 * inter-mountpoint data, notably in XML/JSON parsers, which need to be able to correctly infer their mode of
 * encapsulation (nested mount points).
 *
 * Integration with DataTree is questionable here, as MountPointNode has enough information for InMemoryDataTree to
 * operate efficiently -- all it needs to is switch the resolution root.
 *
 * On the other hand, requiring that a YANG data world is identified by MountPointIdentifer (which is QName) adds
 * interpretation flexibility to SchemaContext which is currently hard-coded. For example operations which require
 * encapsulation of an entire tree have to assume that a SchemaContext has a NodeIdentifier which it uses for the name
 * of its top-level node. MountPointIdentifier solves this, as it can easily be converted to a unique world identifier.
 *
 * That allows, for example NETCONF to specify the root data identifier, properly matching the data it receives from
 * the device to the native datastore format. Rehosting root identifier means rehosting top-level nodes of
 * a ContainerNode, which is a simple and relatively-inexpensive operation (i.e. O(N) where N is the number of
 * top-level nodes).
 *
 * To support this case, MountPointContext really wants to be Identifiable<MountPointIdentifier>, where it would also
 * provide a 'default NodeIdentifier getRootIdentifier()' method. In PathArgument contexts, MountPointIdentifier is
 * directly usable anyway.
 *
 * 6.0.0-timeframe review:
 *
 * The idea with Identifiable is not really that grand, as it goes against our desire to peel identifiers from nodes,
 * as detailed in YANGTOOLS-1074. This will end up meaning that the root of a NormalizedNode tree does not have to have
 * an identifier and very much can live on its own -- solving both the SchemaContext name problem as well as NETCONF
 * interoperability.
 */
@Beta
public interface MountPointContext extends EffectiveModelContextProvider {
    /**
     * Attempt to acquire a {@link MountPointContextFactory} to resolve schemas for the purposes of interpreting
     * this mount point. An empty result indicates the mount point is not attached.
     *
     * @param label Mount point label, as defined via the use of {@code mount-point} statement
     * @return An optional handler for mount point data
     * @throws NullPointerException if label is null
     */
    Optional<MountPointContextFactory> findMountPoint(@NonNull MountPointIdentifier label);
}
