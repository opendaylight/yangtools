/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
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
 *  -v, --verbose         shows details about the results of test running.
 *  -o, --output          path to output file for logs. Output file will be overwritten.
 *  -m, --module-name     validate yang by module name.
 */
@SuppressWarnings({"checkstyle:LoggerMustBeSlf4j", "checkstyle:LoggerFactoryClassParameter"})
public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final ch.qos.logback.classic.Logger LOG_ROOT =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    private static final int MB = 1024 * 1024;

    private static final Option FEATURE = new Option("f", "features", true,
        "features is a string in the form [feature(,feature)*] and feature is a string in the form "
                + "[($namespace?revision=$revision)$local_name]. This option is used to prune the data model "
                + "by removing all nodes that are defined with a \"if-feature\".");

    private static final Option HELP = new Option("h", "help", false, "print help message and exit.");
    private static final Option MODULE_NAME = new Option("m", "module-name", true,
            "validate yang model by module name.");
    private static final Option OUTPUT = new Option("o", "output", true,
            "path to output file for logs. Output file will be overwritten");
    private static final Option PATH = new Option("p", "path", true,
            "path is a colon (:) separated list of directories to search for yang modules.");
    private static final Option RECURSIVE = new Option("r", "recursive", false,
            "recursive search of directories specified by -p option.");

    private static final Option DEBUG = new Option("d", "debug", false, "add debug output");
    private static final Option QUIET = new Option("q", "quiet", false, "completely suppress output.");
    private static final Option VERBOSE = new Option("v", "verbose", false,
        "shows details about the results of test running.");

    private Main() {
        // Hidden on purpose
    }

    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(HELP);
        options.addOption(PATH);
        options.addOption(RECURSIVE);

        final OptionGroup verbosity = new OptionGroup();
        verbosity.addOption(DEBUG);
        verbosity.addOption(QUIET);
        verbosity.addOption(VERBOSE);
        options.addOptionGroup(verbosity);

        options.addOption(OUTPUT);
        options.addOption(MODULE_NAME);
        options.addOption(FEATURE);
        return options;
    }

    public static void main(final String[] args) {
        final HelpFormatter formatter = new HelpFormatter();
        final Options options = createOptions();
        final CommandLine arguments = parseArguments(args, options, formatter);

        if (arguments.hasOption(HELP.getLongOpt())) {
            printHelp(options, formatter);
            return;
        }

        final String[] outputValues = arguments.getOptionValues(OUTPUT.getLongOpt());
        if (outputValues != null) {
            setOutput(outputValues);
        }

        LOG_ROOT.setLevel(Level.WARN);
        if (arguments.hasOption(DEBUG.getLongOpt())) {
            LOG_ROOT.setLevel(Level.DEBUG);
        } else if (arguments.hasOption(VERBOSE.getLongOpt())) {
            LOG_ROOT.setLevel(Level.INFO);
        } else if (arguments.hasOption(QUIET.getLongOpt())) {
            LOG_ROOT.detachAndStopAllAppenders();
        }

        final List<String> yangLibDirs = initYangDirsPath(arguments);
        final List<String> yangFiles = new ArrayList<>();
        final String[] moduleNameValues = arguments.getOptionValues(MODULE_NAME.getLongOpt());
        if (moduleNameValues != null) {
            yangFiles.addAll(Arrays.asList(moduleNameValues));
        }
        yangFiles.addAll(Arrays.asList(arguments.getArgs()));

        final Set<QName> supportedFeatures = initSupportedFeatures(arguments);

        runSystemTest(yangLibDirs, yangFiles, supportedFeatures, arguments.hasOption("recursive"));

        LOG_ROOT.getLoggerContext().reset();
    }

    private static void setOutput(final String... paths) {
        LOG_ROOT.getLoggerContext().reset();

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%date %level [%thread] [%file:%line] %msg%n");
        encoder.setContext(LOG_ROOT.getLoggerContext());
        encoder.start();

        for (final String path : paths) {
            // create FileAppender
            final FileAppender<ILoggingEvent> logfileOut = new FileAppender<>();
            logfileOut.setAppend(false);
            logfileOut.setFile(path);
            logfileOut.setContext(LOG_ROOT.getLoggerContext());
            logfileOut.setEncoder(encoder);
            logfileOut.start();

            // attach the rolling file appender to the root logger
            LOG_ROOT.addAppender(logfileOut);
        }
    }

    @SuppressFBWarnings({ "DM_EXIT", "DM_GC" })
    @SuppressWarnings("checkstyle:illegalCatch")
    private static void runSystemTest(final List<String> yangLibDirs, final List<String> yangFiles,
            final Set<QName> supportedFeatures, final boolean recursiveSearch) {
        LOG.info("Yang model dirs: {} ", yangLibDirs);
        LOG.info("Yang model files: {} ", yangFiles);
        LOG.info("Supported features: {} ", supportedFeatures);

        EffectiveModelContext context = null;

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

    private static Set<QName> initSupportedFeatures(final CommandLine arguments) {
        Set<QName> supportedFeatures = null;
        if (arguments.hasOption("features")) {
            supportedFeatures = new HashSet<>();
            for (final String pathArg : arguments.getOptionValues("features")) {
                supportedFeatures.addAll(createQNames(pathArg.split(",")));
            }
        }
        return supportedFeatures;
    }

    private static Collection<? extends QName> createQNames(final String[] featuresArg) {
        final Set<QName> qnames = new HashSet<>();
        for (final String featureStr : featuresArg) {
            qnames.add(QName.create(featureStr));
        }

        return qnames;
    }

    @SuppressFBWarnings("DM_EXIT")
    private static CommandLine parseArguments(final String[] args, final Options options,
            final HelpFormatter formatter) {
        final CommandLineParser parser = new DefaultParser();

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
        formatter.printHelp("yang-system-test [OPTION...] YANG-FILE...", options);
    }

    private static void printMemoryInfo(final String info) {
        LOG.info("Memory INFO [{}]: free {}MB, used {}MB, total {}MB, max {}MB", info,
            Runtime.getRuntime().freeMemory() / MB,
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB,
            Runtime.getRuntime().totalMemory() / MB, Runtime.getRuntime().maxMemory() / MB);
    }
}
