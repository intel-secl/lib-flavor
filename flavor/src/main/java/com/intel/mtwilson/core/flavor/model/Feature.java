/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import java.util.List;

/**
 * 
 * @author ssbangal
 */
public class Feature {
    private AES_NI AES_NI;
    private TXT TXT;
    private TPM TPM;

    public AES_NI getAES_NI() {
        return AES_NI;
    }

    public void setAES_NI(AES_NI AES_NI) {
        this.AES_NI = AES_NI;
    }

    public TPM getTPM() {
        return TPM;
    }

    public void setTPM(TPM TPM) {
        this.TPM = TPM;
    }

    public TXT getTXT() {
        return TXT;
    }

    public void setTXT(TXT TXT) {
        this.TXT = TXT;
    }

    @Override
    public String toString() {
        return "ClassPojo [TPM - " + (TPM != null ? TPM : "") + ", TXT - " + (TXT != null ? TXT : "") + ", AES-NI - " + (AES_NI != null ? AES_NI : "") + "]";
    }

    public static class TXT {
        private boolean enabled;

        public boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "ClassPojo [enabled - " + enabled + "]";
        }
    }


    public static class AES_NI {
        private boolean enabled;

        public boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "ClassPojo [enabled - " + enabled + "]";
        }
    }


    public static class TPM {
        private boolean enabled;
        private String version;
        private List<String> pcrBanks;
        
        public boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getPcrBanks() {
            return pcrBanks;
        }

        public void setPcrBanks(List<String> pcrBanks) {
            this.pcrBanks = pcrBanks;
        }
        
        @Override
        public String toString() {
            return "Pojo [enabled - " + enabled + ", version - " + (version != null ? version : "") + "]";
        }
    }

}
