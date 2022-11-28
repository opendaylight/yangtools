# YANGTOOLS CSIT TEST
This project is aimed to testing yang files using yang validator tool.

## Installing

*Prerequisite:*  The followings are required for building test:

- JDK17
- JAVA_HOME
- Python 3.8+

GET THE CODE:

USING HTTPS:
    git clone "https://git.opendaylight.org/gerrit/yangtools"

USING SSH:
    git clone ssh://{USERNAME}@git.opendaylight.org:29418/yangtools

NAVIGATE TO:
    cd ~/yangtools/csit

INSTALL VIRTUAL ENVIROMENT PACKAGE:
    sudo apt install python3-virtualenv

CREATE NEW VIRTUAL ENVIROMENT:
    virtualenv venv

ACTIVATE VIRTUAL ENVIROMENT:
    source venv/bin/activate

INSTALL JAVA:
    sudo apt install openjdk-17-jdk openjdk-17-jre

SET JAVA_HOME:
    export JAVA_HOME enviroment variable

## Running test

RUN TEST:
    Usage examples:
    python3 yangtools_test.py -l y -m /src/main/yang/vendor/
    python3 yangtools_test.py --log=y --models=/src/main/yang/vendor/

    [log]
    -l, --log=y/n    for delete previous logs in ~/yangtools_csit_test/yang_validator_logs/ (omitting will not delete previous logs)

    [models]
    -m, --models=/path/to/models/    select directory for testing (omitting will test all models)

    models option examples:
    /src/main/yang/standard/
    /src/main/yang/vendor/
    /src/main/yang/standard/ietf/
    /src/main/yang/standard/ieee/
    /src/main/yang/standard/iana/
    /src/main/yang/vendor/ciena/

## Logs

LOCATION:
    ~/yangtools_csit_test/yang_validator_logs

FILES:
    for each model not pass in yangvalidator, one log file will be created.
        For example "following-components-yang-model-validator--yangtools-system-txt.1669020980.753.log"
    Name of these logs are modified with prefix depending on JAVA error.
    Txt files will be generated containing list of all models that not passed test.