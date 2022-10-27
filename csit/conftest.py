# Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

def pytest_addoption(parser):
    parser.addoption('--BUNDLEFOLDER', action='store', default="", help='Get BUNDLEFOLDER from shell args')
    parser.addoption('--BUNDLE_URL', action='store', default="", help='Get BUNDLE_URL from shell args')
    parser.addoption('--CONTROLLER', action='store', default="", help='Get CONTROLLER from shell args')
    parser.addoption('--CONTROLLER_USER', action='store', default="", help='Get CONTROLLER_USER from shell args')
    parser.addoption('--GERRIT_BRANCH', action='store', default="", help='Get GERRIT_BRANCH from shell args')
    parser.addoption('--GERRIT_PROJECT', action='store', default="", help='Get GERRIT_PROJECT from shell args')
    parser.addoption('--GERRIT_REFSPEC', action='store', default="", help='Get GERRIT_REFSPEC from shell args')
    parser.addoption('--JAVA_HOME', action='store', default="", help='Get JAVA_HOME from shell args')
    parser.addoption('--JDKVERSION', action='store', default="", help='Get JDKVERSION from shell args')
    parser.addoption('--JENKINS_WORKSPACE', action='store', default="", help='Get JENKINS_WORKSPACE from shell args')
    parser.addoption('--MININET1', action='store', default="", help='Get MININET1 from shell args')
    parser.addoption('--MININET2', action='store', default="", help='Get MININET2 from shell args')
    parser.addoption('--MININET3', action='store', default="", help='Get MININET3 from shell args')
    parser.addoption('--MININET4', action='store', default="", help='Get MININET4 from shell args')
    parser.addoption('--MININET5', action='store', default="", help='Get MININET5 from shell args')
    parser.addoption('--MININET', action='store', default="", help='Get MININET from shell args')
    parser.addoption('--MININET_USER', action='store', default="", help='Get MININET_USER from shell args')
    parser.addoption('--NEXUSURL_PREFIX', action='store', default="", help='Get NEXUSURL_PREFIX from shell args')
    parser.addoption('--NUM_ODL_SYSTEM', action='store', default="", help='Get NUM_ODL_SYSTEM from shell args')
    parser.addoption('--NUM_TOOLS_SYSTEM', action='store', default="", help='Get NUM_TOOLS_SYSTEM from shell args')
    parser.addoption('--ODL_STREAM', action='store', default="", help='Get ODL_STREAM from shell args')
    parser.addoption('--ODL_SYSTEM_1_IP', action='store', default="", help='Get ODL_SYSTEM_1_IP from shell args')
    parser.addoption('--ODL_SYSTEM_IP', action='store', default="", help='Get ODL_SYSTEM_IP from shell args')
    parser.addoption('--ODL_SYSTEM_USER', action='store', default="", help='Get ODL_SYSTEM_USER from shell args')
    parser.addoption('--SUITES', action='store', default="", help='Get SUITES from shell args')
    parser.addoption('--TOOLS_SYSTEM_IP', action='store', default="", help='Get TOOLS_SYSTEM_IP from shell args')
    parser.addoption('--TOOLS_SYSTEM_USER', action='store', default="", help='Get TOOLS_SYSTEM_USER from shell args')
    parser.addoption('--USER_HOME', action='store', default="", help='Get USER_HOME from shell args')
    parser.addoption('--IS_KARAF_APPL', action='store', default="", help='Get IS_KARAF_APPL from shell args')
    parser.addoption('--WORKSPACE', action='store', default="", help='Get WORKSPACE from shell args')
