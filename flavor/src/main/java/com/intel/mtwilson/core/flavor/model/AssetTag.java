/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.core.common.tag.model.json.X509AttributeCertificateDeserializer;

/**
 *
 * @author ssbangal
 */
public class AssetTag {

    @JsonDeserialize(using=X509AttributeCertificateDeserializer.class)
    private X509AttributeCertificate tagCertificate;

    public X509AttributeCertificate getTagCertificate() {
        return tagCertificate;
    }

    public void setTagCertificate(X509AttributeCertificate tagCertificate) {
        this.tagCertificate = tagCertificate;
    }
}
