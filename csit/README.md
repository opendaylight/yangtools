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

INSTALL NECESSARY PACKAGES FROM REQUIREMENT FILE:
    pip install -r requirements.txt

INSTALL JAVA:
    sudo apt install openjdk-17-jdk openjdk-17-jre
