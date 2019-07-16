/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rcf8528.data.util;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointChild;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNodeFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNodeFactoryResolver;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;

/**
 * Abstract base class for dynamic resolvers.
 */
@Beta
public class AbstractMountPointNodeFactoryResolver implements MountPointNodeFactoryResolver {

    @Override
    public MountPointNodeFactory resolveMountPoint(final Map<ContainerName, MountPointChild> libraryContainers,
            final MountPointChild schemaMounts) throws YangParserException {


        // TODO Auto-generated method stub
        return null;
    }

//    private void writeInline(final @NonNull MountPointStreamWriter mountWriter, final Inline resolver)
//            throws IOException {
//        for (Entry<ContainerName, MountPointChild> entry : yangLib.entrySet()) {
//            final Optional<LibraryContext> optLibContext = resolver.findSchemaForLibrary(entry.getKey());
//            if (!optLibContext.isPresent()) {
//                LOG.debug("YANG Library context for mount point {} container {} not found", getIdentifier(),
//                    entry.getKey());
//                continue;
//            }
//
//            final LibraryContext libContext = optLibContext.get();
//            final NormalizedNode<?, ?> data = entry.getValue().normalizeTo(libContext.getLibraryContainerSchema());
//            if (!(data instanceof ContainerNode)) {
//                throw new IOException("Invalid non-container " + data);
//            }
//
//            final MountPointNodeFactory factory;
//            try {
//                factory = libContext.bindTo((ContainerNode) data);
//            } catch (YangParserException e) {
//                throw new IOException("Failed to assemble context for " + data, e);
//            }
//
//            writeTo(mountWriter, factory);
//            return;
//        }
//
//        LOG.warn("Failed to create a dynamic context for mount point {}, ignoring its data", getIdentifier());
//    }

    //
//  /**
//   * A resolver which can resolve the SchemaContext for use with mount point data based on the
//   * {@code ietf-yang-library} content of the mountpoint itself. This process requires two steps:
//   * <ul>
//   *   <li>{@link #findSchemaForLibrary(ContainerName)} is invoked to acquire a SchemaContext in which to interpret
//   *       one of the possible {@code ietf-yang-library} top-level containers.
//   *   </li>
//   *   <li>The container is normalized based on the returned context by the user of this interface and then
//   *       {@link LibraryContext#bindTo(ContainerNode)} is invoked to acquire the MountPointMetadata.
//   *   </li>
//   * </ul>
//   */
//  public interface Inline extends MountPointNodeFactoryResolver {
//      @NonNullByDefault
//      interface LibraryContext {
//          /**
//           * Return a SchemaContext capable of parsing the content of YANG Library.
//           *
//           * @return A SchemaContext instance
//           */
//          SchemaContext getLibraryContainerSchema();
//
//          /**
//           * Assemble the SchemaContext for specified normalized YANG Library top-level container.
//           *
//           * @param container Top-level YANG Library container
//           * @return An assembled SchemaContext
//           * @throws NullPointerException if container is null
//           * @throws YangParserException if the schema context cannot be assembled
//           */
//          MountPointNodeFactory bindTo(ContainerNode container) throws YangParserException;
//      }
//
//      /**
//       * Return the schema in which YANG Library container content should be interpreted.
//       *
//       * <p>
//       * Note this schema is not guaranteed to contain any augmentations, hence parsing could fail.
//       *
//       * @param containerName Top-level YANG Library container name
//       * @return The LibraryContext to use when interpreting the specified YANG Library container, or empty
//       * @throws NullPointerException if container is null
//       */
//      Optional<LibraryContext> findSchemaForLibrary(@NonNull ContainerName containerName);
//  }
//
//  /**
//   * A resolver which has static knowledge of the SchemaContext which should be used to interpret mount point data.
//   * Instances of this interface should be used in contexts where the mount point data is expected not to contain
//   * required {@code ietf-yang-library} data, for example due to filtering.
//   */
//  @NonNullByDefault
//  interface SharedSchema extends MountPointNodeFactoryResolver {
//
//      MountPointNodeFactory getSchema();
//  }
}
