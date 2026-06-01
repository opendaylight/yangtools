/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool;

import java.io.File;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Params {
    private static final Logger LOG = LoggerFactory.getLogger(Params.class);

    @Arg(dest = "yang-source-dir")
    private File yangSourceDir;

    static ArgumentParser getParser() {
        final ArgumentParser parser = ArgumentParsers.newFor("jar_file_name").addHelp(true).build();
        parser.description("Validation Tool for Yang Models")
            .formatUsage();

        parser.addArgumentGroup("Required arguments")
            .addArgument("--yang-source-dir")
            .type(File.class)
            .required(true)
            .help("directory containing yang models which will be parsed")
            .dest("yang-source-dir")
            .metavar("");

        return parser;
    }

    File[] listFiles() {
        if (yangSourceDir == null) {
            return null;
        }
        if (!yangSourceDir.exists()) {
            LOG.error("Yang source directory has to exist");
            return null;
        }
        if (!yangSourceDir.canRead()) {
            LOG.error("Yang source directory has to be readable");
            return null;
        }
        final var files = yangSourceDir.listFiles();
        if (files == null) {
            LOG.error("Yang source directory {} is not a directory or cannot be read", yangSourceDir.getPath());
            return null;
        }
        if (files.length == 0) {
            LOG.error("Yang source directory {} doesn't contain any model", yangSourceDir.getPath());
            return null;
        }
        return files;
    }
}
