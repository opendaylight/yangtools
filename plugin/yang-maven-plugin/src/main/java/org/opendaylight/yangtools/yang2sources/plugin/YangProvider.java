/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;

@FunctionalInterface
@VisibleForTesting
interface YangProvider {

    List<FileState> addYangsToMetaInf(MavenProject project, Collection<YangTextSource> modelsInProject)
        throws IOException;
}
