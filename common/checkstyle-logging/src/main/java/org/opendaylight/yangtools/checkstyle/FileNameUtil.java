/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.checkstyle;

import com.google.common.base.Optional;
import java.io.File;
import java.nio.file.Path;

/**
 * Utility to convert absolute file name to path relative to project.
 *
 * <p>Current implementation use a sad heuristic based on detecting a pom.xml.
 * This is of course sub-optimal to say the very least.  Improvements welcome.
 *
 * @see <a href="https://groups.google.com/forum/#!topic/checkstyle-devel/Rfwx81YhVQk">checkstyle-devel list thread</a>
 */
public class FileNameUtil {

    static File getPathRelativeToMavenProjectRootIfPossible(File absoluteFile) {
        return getOptionalPathRelativeToMavenProjectRoot(absoluteFile).or(absoluteFile);
    }

    static Optional<File> getOptionalPathRelativeToMavenProjectRoot(File absoluteFile) {
        if (!absoluteFile.isAbsolute()) {
            return Optional.of(absoluteFile);
        }
        File projectRoot = absoluteFile;
        while (!isProjectRootDir(projectRoot) && projectRoot.getParentFile() != null) {
            projectRoot = projectRoot.getParentFile();
        }
        if (isProjectRootDir(projectRoot)) {
            Path absolutePath = absoluteFile.toPath();
            Path basePath = projectRoot.toPath();
            Path relativePath = basePath.relativize(absolutePath);
            return Optional.of(relativePath.toFile());
        }
        return Optional.absent();
    }

    private static boolean isProjectRootDir(File file) {
        return new File(file, "pom.xml").exists();
    }

}
