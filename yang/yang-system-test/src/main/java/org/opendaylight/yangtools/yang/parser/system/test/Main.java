/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.system.test;

import ch.qos.logback.classic.Level;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of Yang parser system test.
 *
 * <p>
 * yang-system-test [-f features] [-h help] [-p path] [-v verbose] yangFiles...
 *  -f,--features &lt;arg&gt;   features is a string in the form
 *                        [feature(,feature)*] and feature is a string in the form
 *                        [($namespace?revision=$revision)$local_name].
 *                        This option is used to prune the data model by removing
 *                        all nodes that are defined with a "if-feature".
 *  -h,--help             print help message and exit.
 *  -p,--path &lt;arg&gt;       path is a colon (:) separated list of directories
 *                        to search for yang modules.
 *  -r, --recursive       recursive search of directories specified by -p option
 *  -v,--verbose          shows details about the results of test running.
 *
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final int MB = 1024 * 1024;

    private static Options createOptions() {
        final Options options = new Options();

        final Option help = new Option("h", "help", false, "print help message and exit.");
        help.setRequired(false);
        options.addOption(help);

        final Option path = new Option("p", "path", true,
                "path is a colon (:) separated list of directories to search for yang modules.");
        path.setRequired(false);
        options.addOption(path);

        final Option recursiveSearch = new Option("r", "recursive", false,
                "recursive search of directories specified by -p option.");
        recursiveSearch.setRequired(false);
        options.addOption(recursiveSearch);

        final Option verbose = new Option("v", "verbose", false, "shows details about the results of test running.");
        verbose.setRequired(false);
        options.addOption(verbose);

        final Option feature = new Option(
                "f",
                "features",
                true,
                "features is a string in the form [feature(,feature)*] and feature is a string in the form "
                        + "[($namespace?revision=$revision)$local_name]. This option is used to prune the data model "
                        + "by removing all nodes that are defined with a \"if-feature\".");
        feature.setRequired(false);
        options.addOption(feature);
        return options;
    }

    public static void main(final String[] args) {
        final HelpFormatter formatter = new HelpFormatter();
        final Options options = createOptions();
        final CommandLine arguments = parseArguments(args, options, formatter);

        if (arguments.hasOption("help")) {
            printHelp(options, formatter);
            return;
        }

        if (arguments.hasOption("verbose")) {
            setLoggingLevel(Level.DEBUG);
        } else {
            setLoggingLevel(Level.ERROR);
        }

        final List<String> yangLibDirs = initYangDirsPath(arguments);
        final List<String> yangFiles = Arrays.asList(arguments.getArgs());
        final HashSet<QName> supportedFeatures = initSupportedFeatures(arguments);

        runSystemTest(yangLibDirs, yangFiles, supportedFeatures, arguments.hasOption("recursive"));
    }

    private static void setLoggingLevel(final Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static void runSystemTest(final List<String> yangLibDirs, final List<String> yangFiles,
            final HashSet<QName> supportedFeatures, final boolean recursiveSearch) {
        LOG.info("Yang model dirs: {} ", yangLibDirs);
        LOG.info("Yang model files: {} ", yangFiles);
        LOG.info("Supported features: {} ", supportedFeatures);

        SchemaContext context = null;

        printMemoryInfo("start");
        final Stopwatch stopWatch = Stopwatch.createStarted();

        try {
            context = SystemTestUtils.parseYangSources(yangLibDirs, yangFiles, supportedFeatures, recursiveSearch);
        } catch (final Exception e) {
            LOG.error("Failed to create SchemaContext.", e);
            System.exit(1);
        }

        stopWatch.stop();
        LOG.info("Elapsed time: {}", stopWatch);
        printMemoryInfo("end");
        LOG.info("SchemaContext resolved Successfully. {}", context);
        Runtime.getRuntime().gc();
        printMemoryInfo("after gc");
    }

    private static List<String> initYangDirsPath(final CommandLine arguments) {
        final List<String> yangDirs = new ArrayList<>();
        if (arguments.hasOption("path")) {
            for (final String pathArg : arguments.getOptionValues("path")) {
                yangDirs.addAll(Arrays.asList(pathArg.split(":")));
            }
        }
        return yangDirs;
    }

    private static HashSet<QName> initSupportedFeatures(final CommandLine arguments) {
        HashSet<QName> supportedFeatures = null;
        if (arguments.hasOption("features")) {
            supportedFeatures = new HashSet<>();
            for (final String pathArg : arguments.getOptionValues("features")) {
                supportedFeatures.addAll(createQNames(pathArg.split(",")));
            }
        }
        return supportedFeatures;
    }

    private static Collection<? extends QName> createQNames(final String[] featuresArg) {
        final HashSet<QName> qnames = new HashSet<>();
        for (final String featureStr : featuresArg) {
            qnames.add(QName.create(featureStr));
        }

        return qnames;
    }

    private static CommandLine parseArguments(final String[] args, final Options options,
            final HelpFormatter formatter) {
        final CommandLineParser parser = new BasicParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            LOG.error("Failed to parse command line options.", e);
            printHelp(options, formatter);

            System.exit(1);
        }

        return cmd;
    }

    private static void printHelp(final Options options, final HelpFormatter formatter) {
        formatter.printHelp("yang-system-test [-f features] [-h help] [-p path] [-v verbose] yangFiles...", options);
    }

    private static void printMemoryInfo(final String info) {
        LOG.info("Memory INFO [{}]: free {}MB, used {}MB, total {}MB, max {}MB", info,
            Runtime.getRuntime().freeMemory() / MB,
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB,
            Runtime.getRuntime().totalMemory() / MB, Runtime.getRuntime().maxMemory() / MB);
    }
}
