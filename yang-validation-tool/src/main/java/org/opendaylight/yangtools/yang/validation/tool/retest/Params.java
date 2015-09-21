/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool.retest;

import java.io.File;
import java.net.URISyntaxException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;

final class Params {

    @Arg(dest = "yang-source-dir")
    private File yangSourceDir;

    static ArgumentParser getParser() throws URISyntaxException {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("jar_file_name");
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

    public boolean isValid() {
        if (yangSourceDir == null) {
            return false;
        }
        if (!yangSourceDir.exists()) {
            System.err.println("Yang source directory has to exist");
            return false;
        }
        if (!yangSourceDir.canRead()) {
            System.err.println("Yang source directory has to be readable");
            return false;
        }
        if (yangSourceDir.list().length == 0) {
            System.err.printf("Yang source directory '%s' does't contain any model%n", yangSourceDir.getPath());
            return false;
        }

        return true;
    }

    public File getYangSourceDir() {
        return yangSourceDir;
    }
}
