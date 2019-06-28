/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.wml.manifest.xml.Dir;
import com.intel.wml.manifest.xml.Manifest;
import com.intel.wml.manifest.xml.ManifestType;
import com.intel.wml.manifest.xml.Symlink;
import com.intel.wml.measurement.xml.DirectoryMeasurementType;
import com.intel.wml.measurement.xml.FileMeasurementType;
import com.intel.wml.measurement.xml.MeasurementType;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author ddhawal
 */
public class FlavorToManifestConverter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorToManifestConverter.class);

    public static String getManifestXML(Flavor flavor) throws JsonProcessingException, JAXBException {
        log.debug("Input flavor for converter - {}", Flavor.serialize(flavor));
        Manifest manifest = getManifestFromFlavor(flavor);
        JAXB manifestJaxb = new JAXB();
        String manifestString = manifestJaxb.write(manifest);
        log.debug("Output manifest from converter - {}", manifestString);
        return manifestString;
    }

    private static Manifest getManifestFromFlavor(Flavor flavor) throws JsonProcessingException {
        log.debug("Input flavor for converter - {}", Flavor.serialize(flavor));
        Manifest manifest = new Manifest();
        manifest.setDigestAlg(flavor.getMeta().getDescription().getDigestAlgorithm().name());
        manifest.setLabel(flavor.getMeta().getDescription().getLabel());
        manifest.setUuid(flavor.getMeta().getId());
        manifest.getManifests().addAll(getManifestTypes(flavor.getSoftware().getMeasurements().values()));
        return manifest;
    }

    private static ArrayList<ManifestType> getManifestTypes(Collection<MeasurementType> measurements) {
        ArrayList<ManifestType> manifests = new ArrayList<ManifestType>();
        for(MeasurementType measureType : measurements) {
            manifests.add(getManifestType(measureType));
        }
        return manifests;
    }

    private static ManifestType getManifestType(MeasurementType measureType) {
        ManifestType manifestType;
        if(measureType instanceof FileMeasurementType) {
            manifestType = new com.intel.wml.manifest.xml.File();
        } else if(measureType instanceof DirectoryMeasurementType) {
            manifestType = new Dir();
            ((Dir) manifestType).setExclude(((DirectoryMeasurementType) measureType).getExclude());
            ((Dir) manifestType).setFilterType(((DirectoryMeasurementType) measureType).getFilterType());
            ((Dir) manifestType).setInclude(((DirectoryMeasurementType) measureType).getInclude());
        } else {
            manifestType = new Symlink();
        }
        manifestType.setPath(measureType.getPath());
        manifestType.setSearchType(measureType.getSearchType());
        return manifestType;
    }
}
