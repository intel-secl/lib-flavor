package com.intel.mtwilson.core.flavor.model;

public class SignedFlavor {
    private Flavor flavor;
    private String signature;

    public SignedFlavor() {
        this.flavor = null;
        this.signature = null;
    }

    public SignedFlavor(Flavor flavor, String signature) {
        this.flavor = flavor;
        this.signature = signature;
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    public String getSignature() { return signature; }

    public void setSignature(String signature) { this.signature = signature; }
}
