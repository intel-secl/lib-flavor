/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.mtwilson.core.flavor.common.ErrorCode;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorException;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.model.HostInfo;
import com.intel.mtwilson.core.common.model.PcrIndex;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;

/**
 *
 * @author ssbangal
 */
public class RHELPlatformFlavor extends PlatformFlavor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RHELPlatformFlavor.class);
    private HostManifest hostManifest;
    private HostInfo hostInfo;
    private X509AttributeCertificate tagCertificate;
    
    private final List<String> PLATFORM_MODULES = Arrays.asList("LCP_DETAILS_HASH", "BIOSAC_REG_DATA", "OSSINITDATA_CAP_HASH", "STM_HASH", "MLE_HASH", "NV_INFO_HASH", "tb_policy", "CPU_SCRTM_STAT", "HASH_START", "SINIT_PUBKEY_HASH", "LCP_AUTHORITIES_HASH");
    private final List<String> OS_MODULES = Arrays.asList("vmlinuz");
    private final List<String> HOST_UNIQUE_MODULES = Arrays.asList("initrd", "LCP_CONTROL_HASH");

    public RHELPlatformFlavor(HostManifest hostReport) {
        this.hostInfo = hostReport.getHostInfo();
        this.hostManifest = hostReport;
    }

    public RHELPlatformFlavor(HostManifest hostReport, X509AttributeCertificate tagCertificate) {
        this.hostInfo = hostReport.getHostInfo();
        this.hostManifest = hostReport;
        this.tagCertificate = tagCertificate;
    }

    public RHELPlatformFlavor(X509AttributeCertificate tagCertificate) {
        this.tagCertificate = tagCertificate;
    }

    @Override
    public String getFlavorPart(String flavorPartName) throws Exception {
        try {
            switch (FlavorPart.valueOf(flavorPartName.toUpperCase())) {
                case PLATFORM:
                    return getPlatformFlavor();
                case OS:
                    return getOsFlavor();
                case ASSET_TAG:
                    return getAssetTagFlavor();
                case HOST_UNIQUE:
                    return getHostUniqueFlavor();
                default:
                    throw new PlatformFlavorException(ErrorCode.UNKNOWN_FLAVOR_PART, "Unknown flavor part specified by the user");
            }
        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    @Override
    public Collection<String> getFlavorPartNames() throws Exception{
        List<String> flavorParts = new ArrayList(Arrays.asList(PLATFORM.getValue(), OS.getValue(), HOST_UNIQUE.getValue(), SOFTWARE.getValue()));

        // For each of the flavor parts, check what PCRs are required and if those required PCRs are present in the host report.
        Iterator iterator = flavorParts.iterator();
        while (iterator.hasNext()) {
            String nextElement = (String) iterator.next();
            List<Integer> pcrList = getPcrList(hostInfo.getTpmVersion(), nextElement);
            boolean pcrExists = PlatformFlavorUtil.pcrExists(hostManifest.getPcrManifest(), pcrList);
            if (!pcrExists) {
                iterator.remove();
            }
        }

        // If asset tag is configured, add it to the list of flavor parts.
        if (tagCertificate != null)
            flavorParts.add(ASSET_TAG.getValue());

        return flavorParts;

    }

    /**
     * Helper function to calculate the list of PCRs for the flavor part specified based
     * on the version of the TPM.
     *
     * @param tpmVersion Version of the TPM in the host
     * @param flavorPartName Name of the flavor part.
     * @return List of PCRs for the specified flavor part and TPM version.
     */
    private List<Integer> getPcrList(String tpmVersion, String flavorPartName) {
        List<Integer> pcrs = new ArrayList<>();

        switch (FlavorPart.valueOf(flavorPartName.toUpperCase())) {
            case PLATFORM:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    pcrs.addAll(Arrays.asList(0, 17, 18));
                } else {
                    pcrs.addAll(Arrays.asList(0, 17));
                }
                break;
            case OS:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    pcrs.add(17);
                } else {
                    pcrs.add(18);
                }
                break;
            case HOST_UNIQUE:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    pcrs.addAll(Arrays.asList(17, 18));
                } else {
                    pcrs.add(19);
                }
                break;
            case SOFTWARE:
            default:
                break;
        }

        return pcrs;
    }

    /**
     * Helper function to determine if the event log associated with the PCR
     * should be included in the flavor for the specified flavor part.
     *
     * @param tpmVersion Version of the TPM in the host
     * @param flavorPartName Name of the flavor part.
     * @return Boolean flag indicating whether the event log should be included or not.
     */
    private boolean eventLogRequired(String tpmVersion, String flavorPartName) {
        boolean eventLogRequired = false;

        switch (FlavorPart.valueOf(flavorPartName.toUpperCase())) {
            case PLATFORM:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    eventLogRequired = true;
                } else {
                    eventLogRequired = false;
                }
                break;
            case OS:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    eventLogRequired = true;
                } else {
                    eventLogRequired = false;
                }
                break;
            case HOST_UNIQUE:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    eventLogRequired = true;
                } else {
                    eventLogRequired = true;
                }
                break;
            case SOFTWARE:
            default:
                break;
        }

        return eventLogRequired;
    }

    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the PLATFORM of a
     * host.
     *
     * @return PLATFORM flavor as a JSON document.
     * @throws Exception
     */
    private String getPlatformFlavor() throws Exception {
        try {
            List<Integer> platformPcrs = getPcrList(hostInfo.getTpmVersion(), PLATFORM.getValue());
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), PLATFORM.getValue());
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.includeModulesToEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), platformPcrs, includeEventLog),
                    PLATFORM_MODULES);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, PLATFORM.getValue(), null),
                    PlatformFlavorUtil.getHardwareSectionDetails(hostInfo),
                    filteredPcrDetails,
                    null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of PLATFORM flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the OS Kernel of
     * a host.
     *
     * @return OS flavor as a JSON document.
     * @throws Exception
     */
    private String getOsFlavor() throws Exception {
        try {
            List<Integer> osPcrs = getPcrList(hostInfo.getTpmVersion(), OS.getValue());
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), OS.getValue());
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.includeModulesToEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), osPcrs, includeEventLog),
                    OS_MODULES);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, OS.getValue(), null),
                    null,
                    filteredPcrDetails,
                    null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of OS flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the unique part
     * of the PCR configurations of a host. These include PCRs/modules getting
     * extended to PCRs that would vary from host to host.
     *
     * @return HostUnique flavor as a JSON document.
     * @throws Exception
     */
    private String getHostUniqueFlavor() throws Exception {
        try {
            List<Integer> hostUniquePcrs = getPcrList(hostInfo.getTpmVersion(), HOST_UNIQUE.getValue());
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), HOST_UNIQUE.getValue());
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.includeModulesToEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), hostUniquePcrs, includeEventLog),
                    HOST_UNIQUE_MODULES);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, HOST_UNIQUE.getValue(), null),
                    null,
                    filteredPcrDetails,
                    null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of HOST_UNIQUE flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    /**
     * Retrieves the asset tag part of the flavor including the certificate and all the key-value pairs
     * that are part of the certificate.
     * @return
     * @throws Exception
     */
    private String getAssetTagFlavor() throws PlatformFlavorException {
        try {
            if (tagCertificate == null)
                throw new PlatformFlavorException(ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED, ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED.getMessage());
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, ASSET_TAG.getValue(), null),
                    null,
                    null,
                    PlatformFlavorUtil.getExternalConfigurationDetails(hostManifest, tagCertificate));
            return Flavor.serialize(flavor);
        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of ASSET_TAG flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }
}
