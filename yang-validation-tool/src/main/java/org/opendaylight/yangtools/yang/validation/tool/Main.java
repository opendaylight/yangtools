/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.util.Arrays;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class Main {

    static class Params {

        static String yangSourcePath = "\\yang-sources";
        static File defaultYangSourceDirectory = new File(yangSourcePath);

        @Arg(dest = "yang-source-dir")
        public File yangSourceDir;

        static ArgumentParser getParser() {
            final ArgumentParser parser = ArgumentParsers.newArgumentParser("yang validation tool");
            parser.description("Yang models validation tool.");

            parser.addArgument("--yang-source-dir")
                .type(File.class)
                .help("Directory containing yang models which will be parsed.").dest("yang-source-dir")
                .dest("yang-source-dir")
                .setDefault(defaultYangSourceDirectory);

            return parser;
        }

        void validate() {
            if (yangSourceDir != null) {
                checkArgument(yangSourceDir.exists(), "Yang source directory has to exist");
                checkArgument(yangSourceDir.isDirectory(), "Yang source directory has to be a directory");
                checkArgument(yangSourceDir.canRead(), "Yang source directory has to be readable");
                //checkArgument(Iterables.isEmpty(Arrays.asList(yangSourceDir.listFiles())), "Yang source directory must not be emtpy");
            }
        }
    }

    public static void main(String[] args) {
        final Params params = parseArgs(args, Params.getParser());
        params.validate();

        final YangParserImpl yangParser = new YangParserImpl();
        final File[] yangModels = params.yangSourceDir.listFiles();
        yangParser.parseFiles(Arrays.asList(yangModels));
    }

    private static Params parseArgs(final String[] args, final ArgumentParser parser) {
        final Params opt = new Params();
        try {
            parser.parseArgs(args, opt);
            return opt;
        } catch (final ArgumentParserException e) {
            parser.handleError(e);
        }
        System.exit(1);
        return null;
    }
}
