#!/usr/bin/env sh

rm rocrate.zip
zip -j rocrate.zip ../ro-crate-metadata/ro-crate-metadata.json
zip -ru rocrate.zip ../../reference-openbis-export