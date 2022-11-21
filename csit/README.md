# YANGTOOLS CSIT TEST
This project is aimed to testing yang files using yang validator tool.

## Installing

*Prerequisite:*  The followings are required for building test:

- JDK17
- Python 2.8+

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

SET JAVA PATH:
    update JAVA variable in pytest_lib.py depending on your system (line 9)

## Running test

RUN TEST:
    python yangtools_test.py

DELETE PREVIOUS LOGS:
    program will ask for deleting previous logs in ~/yangtools_csit_test/yang_validator_logs (y/n)

SELECT TEST:
    type choice and press enter:
    0:  ALL
    1:  ALL standard
    2:  ALL vendor
    3:  standard/ietf/RFC
    4:  standard/ieee
    5:  standard/iana
    6:  vendor/ciena

After this step test will start.

## Logs

LOCATION:
    ~/yangtools_csit_test/yang_validator_logs

FILES:
    All JAVA error in yangvalidator will produce one log file.
        For example "following-components-yang-model-validator--yangtools-system-txt.1669020980.753.log"
    Name of these logs are modified with prefix depending on JAVA error.
    Txt files will be generated containing list of all models that are not passed test.







