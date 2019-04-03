/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;

/**
 * 
 * @author ssbangal
 */
public class Description {
    private String flavorPart;
    private String source; // Indicates the source host from which the flavor would be created.
    private String label;
    private String ipAddress;
    private String biosName;
    private String biosVersion;
    private String osName;
    private String osVersion;
    private String vmmName;
    private String vmmVersion;
    private String tpmVersion;
    private String hardwareUuid;
    private String comment;
    private String tbootInstalled;
    private DigestAlgorithm digestAlgorithm;

    public String getFlavorPart() {
        return flavorPart;
    }

    public void setFlavorPart(String flavorPart) {
        this.flavorPart = flavorPart;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getOsName() {
        return osName;
    }

    public void setOsName(String os_name) {
        this.osName = os_name;
    }

    public String getBiosName() {
        return biosName;
    }

    public void setBiosName(String bios_name) {
        this.biosName = bios_name;
    }

    public String getVmmName() {
        return vmmName;
    }

    public void setVmmName(String vmm_name) {
        this.vmmName = vmm_name;
    }

    public String getVmmVersion() {
        return vmmVersion;
    }

    public void setVmmVersion(String vmm_version) {
        this.vmmVersion = vmm_version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBiosVersion() {
        return biosVersion;
    }

    public void setBiosVersion(String bios_version) {
        this.biosVersion = bios_version;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String os_version) {
        this.osVersion = os_version;
    }

    public String getTpmVersion() {
        return tpmVersion;
    }

    public void setTpmVersion(String tpm_version) {
        this.tpmVersion = tpm_version;
    }

    public String getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(String hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public String getTbootInstalled() {
        return tbootInstalled;
    }

    public void setTbootInstalled(String tbootInstalled) {
        this.tbootInstalled = tbootInstalled;
    }

    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    @Override
    public String toString() {
        return "Pojo [flavorPart - " + (flavorPart != null ? flavorPart : "")  + 
                ", label - " + (label != null ? label : "")  + 
                ", os_name - " + (osName != null ? osName : "")  + 
                ", bios_name - " + (biosName != null ? biosName : "")  + 
                ", comment - " + (comment != null ? comment : "") + 
                ", bios_version - " + (biosVersion != null ? biosVersion : "")  + 
                ", os_version - " + (osVersion != null ? osVersion : "")  + "]" +
                ", tbootInstalled - " + (tbootInstalled != null ? tbootInstalled : "")  + "]" +
                ", digest_algorithm - " + (digestAlgorithm != null ? digestAlgorithm.algorithm() : "")  + "]";
    }
}

