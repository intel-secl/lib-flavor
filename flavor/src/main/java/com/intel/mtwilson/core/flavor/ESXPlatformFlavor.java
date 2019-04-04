/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import static com.intel.dcsg.cpg.crypto.DigestAlgorithm.SHA1;
import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ssbangal
 */
public class ESXPlatformFlavor extends PlatformFlavor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ESXPlatformFlavor.class);
    private HostManifest hostManifest;
    private HostInfo hostInfo;
    private X509AttributeCertificate tagCertificate;

    public ESXPlatformFlavor(HostManifest hostManifest) {
        this.hostInfo = hostManifest.getHostInfo();
        this.hostManifest = hostManifest;
    }

    public ESXPlatformFlavor(HostManifest hostReport, X509AttributeCertificate tagCertificate) {
        this.hostInfo = hostReport.getHostInfo();
        this.hostManifest = hostReport;
        this.tagCertificate = tagCertificate;
    }

    public ESXPlatformFlavor(X509AttributeCertificate tagCertificate) {
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
    public Collection<String> getFlavorPartNames() throws Exception {
        List<String> flavorParts = new ArrayList(Arrays.asList(PLATFORM.getValue(), OS.getValue(), HOST_UNIQUE.getValue(), SOFTWARE.getValue()));
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails;

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

        pcrDetails = PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), Arrays.asList(22), false);
        for (Map.Entry<DigestAlgorithm, Map<PcrIndex, PcrEx>> digestAlgorithmEntry : pcrDetails.entrySet()) {
            Map<PcrIndex, PcrEx> digestAlgorigthmValue = digestAlgorithmEntry.getValue();
            for (Map.Entry<PcrIndex, PcrEx> pcrIndexEntry : digestAlgorigthmValue.entrySet()) {

                PcrIndex key = pcrIndexEntry.getKey();
                PcrEx value = pcrIndexEntry.getValue();

                if (key.toInteger() == 22 && !value.getValue().equalsIgnoreCase(Sha1Digest.ZERO.toHexString())) {
                    log.debug("ESXPlatformFlavor: Adding support for asset tag flavor.");
                    flavorParts.add(ASSET_TAG.getValue());
                    break;
                }
            }
        }

        return flavorParts;

    }

    /**
     * Helper function to calculate the list of PCRs for the flavor part specified based
     * on the version of the TPM. TPM 2.0 support is available since ISecL v1.2
     *
     * @param tpmVersion Version of the TPM in the host
     * @param flavorPartName Name of the flavor part.
     * @return List of PCRs for the specified flavor part and TPM version.
     */
    private List<Integer> getPcrList(String tpmVersion, String flavorPartName) {
        List<Integer> pcrs = new ArrayList<>();
        boolean isTpm20 = tpmVersion != null && tpmVersion.equals("2.0");
        switch (FlavorPart.valueOf(flavorPartName.toUpperCase())) {
            case PLATFORM:
                if(isTpm20)
                    pcrs.addAll(Arrays.asList(0, 17, 18));
                else
                    pcrs.addAll(Arrays.asList(0, 17));
                break;
            case OS:
                if(isTpm20)
                    pcrs.addAll(Arrays.asList(19, 20, 21)); // 2.0 mappings
                else
                    pcrs.addAll(Arrays.asList(18, 19, 20));
                break;
            case HOST_UNIQUE:
                if(isTpm20)
                    pcrs.addAll(Arrays.asList(20, 21));
                else
                    pcrs.addAll(Arrays.asList(19));
                break;
            case ASSET_TAG:
                pcrs.addAll(Arrays.asList(22));
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
                if ("2.0".equalsIgnoreCase(tpmVersion)){
                    eventLogRequired = true;
                }
                else {
                    eventLogRequired = false;
                }
                break;
            case OS:
                eventLogRequired = true;
                break;
            case HOST_UNIQUE:
                eventLogRequired = true;
                break;
            case ASSET_TAG:
                eventLogRequired = false;
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
     * @throws PlatformFlavorException
     */
    private String getPlatformFlavor() throws PlatformFlavorException {
        try {
            List<Integer> platformPcrs = getPcrList(hostInfo.getTpmVersion(), PLATFORM.getValue());
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), PLATFORM.getValue());
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails =
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), platformPcrs, includeEventLog);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, PLATFORM.getValue(), null),
                    PlatformFlavorUtil.getHardwareSectionDetails(hostInfo),
                    pcrDetails,
                    null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of PLATFORM flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    static final List<String> hostSpecificModules = Arrays.asList("commandLine.", "componentName.imgdb.tgz", "componentName.onetime.tgz");
    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the OS Kernel of
     * a host.
     *
     * @return OS flavor as a JSON document.
     * @throws Exception
     */
    private String getOsFlavor() throws Exception {
        List<String> modulesToExclude = hostSpecificModules;
        try {
            List<Integer> osPcrs = getPcrList(hostInfo.getTpmVersion(), OS.getValue());
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), OS.getValue());
            // Need to remove the commandLine module from the whitelist as it is evaluated
            // in host specific flavor.
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.excludeModulesFromEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), osPcrs, includeEventLog),
                    modulesToExclude);
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
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails = PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), hostUniquePcrs, includeEventLog);
            pcrDetails = PlatformFlavorUtil.includeModulesToEventLog(pcrDetails, hostSpecificModules);

            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, HOST_UNIQUE.getValue(), null),
                    null,
                    pcrDetails,
                    null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of HOST_UNIQUE flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    /**
     * Retrieves the asset tag part of the flavor including the certificate and
     * all the key-value pairs that are part of the certificate.
     *
     * @return
     * @throws Exception
     */
    private String getAssetTagFlavor() throws PlatformFlavorException {
        try {
            if (tagCertificate == null)
                throw new PlatformFlavorException(ErrorCode.INVALID_INPUT, "Tag certificate is not specified");

            // calculate the expected PCR 22 value based on tag certificate hash
            Sha1Digest tagCertificateHash = Sha1Digest.digestOf(tagCertificate.getEncoded());
            String expectedPcrValue = Sha1Digest.ZERO.extend(tagCertificateHash).toHexString();

            // Add the expected PCR 22 value to respective hash maps
            Map<PcrIndex, PcrEx> pcr22Map = new HashMap();
            pcr22Map.put(PcrIndex.PCR22, new PcrEx(expectedPcrValue, null));
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails = new HashMap();
            pcrDetails.put(SHA1, pcr22Map);

            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, ASSET_TAG.getValue(), null),
                    null,
                    pcrDetails,
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