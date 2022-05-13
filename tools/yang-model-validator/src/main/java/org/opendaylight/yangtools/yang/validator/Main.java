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
 *  -K, --no-warning-for-unkeyed-lists
 *                        do not add warnings about unkeyed lists with config true.
 */
@SuppressWarnings({"checkstyle:LoggerMustBeSlf4j", "checkstyle:LoggerFactoryClassParameter"})
public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final ch.qos.logback.classic.Logger LOG_ROOT =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    private static final int MB = 1024 * 1024;

    private static final String FEATURES = "features";
    private static final String MODULE_NAME = "module-name";
    private static final String OUTPUT = "output";
    private static final String PATH = "path";
    private static final String RECURSIVE = "recursive";
    private static final String DEBUG = "debug";
    private static final String QUIET = "quiet";
    private static final String VERBOSE = "verbose";
    private static final String LIST_WARNING_OFF = "no-warning-for-unkeyed-lists";

    private Main() {
        // Hidden on purpose
    }

    public static void main(final String[] args) {
        final ArgumentParser parser = getParser();
        final Namespace arguments = parser.parseArgsOrFail(args);

        final String[] outputValues = arguments.get(OUTPUT);
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

        final boolean warnForUnkeyedLists = !arguments.getBoolean(LIST_WARNING_OFF);

        final List<String> yangLibDirs = initYangDirsPath(arguments);
        final List<String> yangFiles = new ArrayList<>();
        final String[] moduleNameValues = arguments.get(MODULE_NAME);
        if (moduleNameValues != null) {
            yangFiles.addAll(Arrays.asList(moduleNameValues));
        }
//        yangFiles.addAll(Arrays.asList(arguments.getArgs())); //I'm don't quite understand this part

        final Set<QName> supportedFeatures = initSupportedFeatures(arguments);

        runSystemTest(yangLibDirs, yangFiles, supportedFeatures, arguments.getBoolean(RECURSIVE),
            warnForUnkeyedLists);

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
        final String[] path = arguments.get(PATH);
        if (path != null) {
            for (final String pathArg : path) {
                yangDirs.addAll(Arrays.asList(pathArg.split(":")));
            }
        }
        return yangDirs;
    }

    private static Set<QName> initSupportedFeatures(final Namespace arguments) {
        Set<QName> supportedFeatures = null;
        final String[] features = arguments.get(FEATURES);
        if (features != null) {
            supportedFeatures = new HashSet<>();
            for (final String pathArg : features) {
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

    static ArgumentParser getParser() {
        final ArgumentParser parser = ArgumentParsers.newFor("jar_file_name").addHelp(true).build()
                .description("Main class of Yang parser system test.");

        parser.addArgument("-f", "--features")
                .help("features is a string in the form [feature(,feature)*] and feature is a string in the form "
                        + "[($namespace?revision=$revision)$local_name]. This option is used to prune the data model "
                        + "by removing all nodes that are defined with a \"if-feature\".")
                .dest(FEATURES);
        parser.addArgument("-m", "--module-name")
                .help("validate yang model by module name.")
                .dest(MODULE_NAME)
                .type(String[].class);
        parser.addArgument("-o", "--output")
                .help("path to output file for logs. Output file will be overwritten.")
                .dest(OUTPUT);
        parser.addArgument("-p", "--path")
                .help("path is a colon (:) separated list of directories to search for yang modules.")
                .dest(PATH)
                .type(String[].class);
        parser.addArgument("-r", "--recursive")
                .help("recursive search of directories specified by -p option.")
                .dest(RECURSIVE)
                .action(Arguments.storeTrue());
        parser.addArgument("-d", "--debug")
                .help("add debug output.")
                .dest(DEBUG)
                .action(Arguments.storeTrue());
        parser.addArgument("-q", "--quiet")
                .help("completely suppress output.")
                .dest(QUIET)
                .action(Arguments.storeTrue());
        parser.addArgument("-v", "--verbose")
                .help("shows details about the results of test running.")
                .dest(QUIET)
                .action(Arguments.storeTrue());
        parser.addArgument("-K", "--no-warning-for-unkeyed-lists")
                .help("do not add warnings about unkeyed lists with config true.")
                .dest(LIST_WARNING_OFF)
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
