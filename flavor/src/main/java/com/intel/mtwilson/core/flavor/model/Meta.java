/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;


/**
 * 
 * @author ssbangal
 */
public class Meta {
    private Schema schema;
    private String id;
    private Author author;
    private String realm;
    private String vendor;
    private Description description;  

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
        

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    
    @Override
    public String toString() {
        return "Meta [schema - " + (schema != null ? schema.toString() : "") + 
                ", id - " + (id != null ? id : "") + 
                ", author - " + (author != null ? author.toString() : "") + 
                ", realm - " + (realm != null ? realm : "") + 
                ", description - " + (description != null ? description.toString() : "") + "]";
    }

    public static class Schema {
        private String uri;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return "Schema [uri - " + (uri != null ? uri : "") + "]";
        }
    }

    public static class Author {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "Author [email - " + (email != null ? email : "") + "]";
        }
    }
}
