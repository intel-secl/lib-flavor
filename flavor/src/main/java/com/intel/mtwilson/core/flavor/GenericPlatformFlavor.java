/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor;

import com.intel.mtwilson.core.flavor.common.ErrorCode;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorException;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.ASSET_TAG;

/**
 *
 * @author ssbangal
 */
public class GenericPlatformFlavor extends PlatformFlavor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenericPlatformFlavor.class);
    private X509AttributeCertificate tagCertificate;
    private String vendor;

    public GenericPlatformFlavor(String vendor, X509AttributeCertificate tagCertificate) {
        this.vendor = vendor;
        this.tagCertificate = tagCertificate;
    }

    @Override
    public String getFlavorPart(String flavorPartName) throws Exception {
        try {
            switch (FlavorPart.valueOf(flavorPartName.toUpperCase())) {
                case ASSET_TAG:
                    return getAssetTagFlavor();
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
        return new ArrayList(Arrays.asList(ASSET_TAG.getValue()));
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
            Flavor flavor = new Flavor(PlatformFlavorUtil.getMetaSectionDetails(null, tagCertificate, ASSET_TAG.getValue(), vendor), null, null, PlatformFlavorUtil.getExternalConfigurationDetails(null, tagCertificate));
            return Flavor.serialize(flavor);

        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of Host Aggregated flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

}
