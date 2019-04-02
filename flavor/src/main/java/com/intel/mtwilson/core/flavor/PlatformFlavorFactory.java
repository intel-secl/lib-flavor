/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor;

import com.intel.mtwilson.core.flavor.common.ErrorCode;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorException;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import java.net.MalformedURLException;

/**
 * Factory class for the PlatformFlavor which is responsible for instantiating
 * an appropriate implementation class based on the target host.
 *
 * @author ssbangal
 */
public class PlatformFlavorFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlatformFlavorFactory.class);

    /**
     * Parses the connection string of the target host and determines the type
     * of the host and instantiates the appropriate PlatformFlavor
     * implementation.
     *
     * @param hostManifest HostManifest object that contains the details of the
     * host along with the PCR information.
     * @return Instance of the PlatformFlavor implementation class.
     * @throws PlatformFlavorException
     * @throws MalformedURLException
     */
    public PlatformFlavor getPlatformFlavor(HostManifest hostManifest, X509AttributeCertificate tagCertificate) throws PlatformFlavorException, MalformedURLException {

        try {
            if (hostManifest != null && hostManifest.getHostInfo() != null) {
                log.info("getPlatformFlavor: creating platform flavor instance for {}", hostManifest.getHostInfo().getOsName());
                switch (hostManifest.getHostInfo().getOsName().trim().toUpperCase()) {
                    case "RHEL":
                    case "REDHATENTERPRISESERVER":
                        return new RHELPlatformFlavor(hostManifest, tagCertificate);
                    case "WINDOWS":
                    case "MICROSOFT WINDOWS SERVER 2016 DATACENTER":
                        return new WindowsPlatformFlavor(hostManifest, tagCertificate);
                    case "VMWARE ESXI":
                        return new ESXPlatformFlavor(hostManifest, tagCertificate);
                    default:
                        throw new PlatformFlavorException(ErrorCode.UN_SUPPORTED_OS, "Operating system on the host system is currently not supported.");
                }
            }
            return null;

        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during determining the type of the target system.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    public PlatformFlavor getPlatformFlavor(String vendor,X509AttributeCertificate tagCertificate) throws PlatformFlavorException, MalformedURLException {

        try {
            
            if (tagCertificate == null){
                log.error("getPlatformFlavor: Invalid tag certificate specified.");
                throw new PlatformFlavorException(ErrorCode.INVALID_INPUT, "Invalid tag certificate specified");
            }
                
            log.info("getPlatformFlavor: creating generic platform flavor for tag certificate with host hardware UUID {}", tagCertificate.getSubject());
            return new GenericPlatformFlavor(vendor, tagCertificate);

        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during determining the type of the target system.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

}
