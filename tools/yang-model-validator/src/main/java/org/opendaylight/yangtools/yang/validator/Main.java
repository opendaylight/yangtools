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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of Yang parser system test.
 *
 * <p>
 * Type yang-system-test -h for usage.
 */
@SuppressWarnings({"checkstyle:LoggerMustBeSlf4j", "checkstyle:LoggerFactoryClassParameter"})
public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final ch.qos.logback.classic.Logger LOG_ROOT =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    private static final int MB = 1024 * 1024;

    private static final String YANG_MODEL = "yang-model";
    private static final String PATH = "path";
    private static final String RECURSIVE = "recursive";
    private static final String UNKEYED_LIST_WARNING_ON = "warning-for-unkeyed-lists";
    private static final String OUTPUT = "output";
    private static final String MODULE_NAME = "module-name";
    private static final String FEATURES = "features";
    private static final String DEBUG = "debug";
    private static final String QUIET = "quiet";
    private static final String VERBOSE = "verbose";

    private Main() {
        // Hidden on purpose
    }

    public static void main(final String[] args) {
        final ArgumentParser parser = getParser();
        final Namespace arguments = parser.parseArgsOrFail(args);

        final String outputValues = arguments.get(OUTPUT);
        if (outputValues != null) {
            setOutput(outputValues);
        }

        LOG_ROOT.setLevel(Level.WARN);
        if (arguments.getBoolean(DEBUG)) {
            LOG_ROOT.setLevel(Level.DEBUG);
        } else if (arguments.getBoolean(VERBOSE)) {
            LOG_ROOT.setLevel(Level.INFO);
        } else if (arguments.getBoolean(QUIET)) {
            LOG_ROOT.detachAndStopAllAppenders();
        }

        final List<String> yangLibDirs = initYangDirsPath(arguments);
        final List<String> yangFiles = new ArrayList<>();
        final List<String> moduleNameValues = arguments.get(MODULE_NAME);
        if (moduleNameValues != null) {
            yangFiles.addAll(moduleNameValues);
        }
        if (arguments.get(YANG_MODEL) != null) {
            yangFiles.addAll(arguments.get(YANG_MODEL));
        }

        final Set<QName> supportedFeatures = initSupportedFeatures(arguments);

        runSystemTest(yangLibDirs, yangFiles, supportedFeatures, arguments.getBoolean(RECURSIVE),
                arguments.getBoolean(UNKEYED_LIST_WARNING_ON));

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
            final Set<QName> supportedFeatures, final boolean recursiveSearch, final boolean warnForUnkeyedLists) {
        LOG.info("Yang model dirs: {} ", yangLibDirs);
        LOG.info("Yang model files: {} ", yangFiles);
        LOG.info("Supported features: {} ", supportedFeatures);

        EffectiveModelContext context = null;

        printMemoryInfo("start");
        final Stopwatch stopWatch = Stopwatch.createStarted();

        try {
            context = SystemTestUtils.parseYangSources(yangLibDirs, yangFiles, supportedFeatures,
                    recursiveSearch, warnForUnkeyedLists);
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

    private static List<String> initYangDirsPath(final Namespace arguments) {
        final List<String> yangDirs = new ArrayList<>();
        final List<String> path = arguments.get(PATH);
        if (path != null) {
            yangDirs.addAll(path);
        }
        return yangDirs;
    }

    private static Set<QName> initSupportedFeatures(final Namespace arguments) {
        final Set<QName> supportedFeatures = new HashSet<>();
        final List<String> features = arguments.get(FEATURES);
        if (features != null) {
            supportedFeatures.addAll(createQNames(features.toArray(new String[0])));
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

    private static ArgumentParser getParser() {
        final var parser = ArgumentParsers.newFor("yang-system-test").addHelp(true).build()
                .description("Main class of Yang parser system test.");
        parser.addArgument(YANG_MODEL)
                .help("yang file(s) to validate")
                .dest(YANG_MODEL)
                .nargs("*")
                .metavar(YANG_MODEL)
                .type(String.class);
        parser.addArgument("-p", "--path")
                .help("path is a space separated list of directories to search for yang modules")
                .dest(PATH)
                .nargs("+")
                .type(String.class);
        parser.addArgument("-r", "--recursive")
                .help("recursive search of directories specified by -p option")
                .dest(RECURSIVE)
                .action(Arguments.storeTrue());
        parser.addArgument("-W", "--warning-for-unkeyed-lists")
                .help("add warnings about unkeyed lists with config true")
                .dest(UNKEYED_LIST_WARNING_ON)
                .action(Arguments.storeTrue());
        parser.addArgument("-o", "--output")
                .help("path to output file for logs. Output file will be overwritten")
                .dest(OUTPUT);
        parser.addArgument("-m", "--module-name")
                .help("validate yang model by module name.")
                .dest(MODULE_NAME)
                .nargs("+")
                .type(String.class);
        parser.addArgument("-f", "--features").help(
                """
                features is a space separated list strings in the form [($namespace?revision=$revision)$local_name].
                This option is used to prune the data model by removing all nodes that are defined with a "if-feature".
                """)
                .dest(FEATURES)
                .nargs("+")
                .type(String.class);
        final var group = parser.addMutuallyExclusiveGroup("logging")
                .description("exclusive group for logging parameters");
        group.addArgument("-d", "--debug")
                .help("add debug output")
                .dest(DEBUG)
                .action(Arguments.storeTrue());
        group.addArgument("-q", "--quiet")
                .help("completely suppress output")
                .dest(QUIET)
                .action(Arguments.storeTrue());
        group.addArgument("-v", "--verbose")
                .help("shows details about the results of test running")
                .dest(VERBOSE)
                .action(Arguments.storeTrue());
        return parser;
    }

    private static void printMemoryInfo(final String info) {
        LOG.info("Memory INFO [{}]: free {}MB, used {}MB, total {}MB, max {}MB", info,
            Runtime.getRuntime().freeMemory() / MB,
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB,
            Runtime.getRuntime().totalMemory() / MB, Runtime.getRuntime().maxMemory() / MB);
    }
}
