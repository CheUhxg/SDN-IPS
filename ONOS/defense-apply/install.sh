#!/bin/bash
mvn clean install
cd target
onos-app 127.0.0.1 reinstall! defense-apply-1.0.oar
