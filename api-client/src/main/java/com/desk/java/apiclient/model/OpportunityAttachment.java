/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of Salesforce.com, Inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.desk.java.apiclient.model;

import java.util.Date;

/**
 * Created by Matt Kranzler on 1/14/16.
 * Copyright (c) 2016 Desk.com. All rights reserved.
 */
public class OpportunityAttachment extends OpportunityActivity {

    private String fileName;
    private String contentType;
    private int size;
    private String url;
    private Date erasedAt;

    public int getUploadedById() {
        return (links != null && links.getUploadedBy() != null) ? links.getUploadedBy().getLinkId() : 0;
    }

    public int getActivityId() {
        return (links != null && links.getActivity() != null) ? links.getActivity().getLinkId() : 0;
    }

    public int getOpportunityId() {
        return (links != null && links.getOpportunity() != null) ? links.getOpportunity().getLinkId() : 0;
    }

    public Date getErasedAt() {
        return erasedAt;
    }

    public void setErasedAt(Date erasedAt) {
        this.erasedAt = erasedAt;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fn) {
        this.fileName = fn;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String ct) {
        this.contentType = ct;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int s) {
        this.size = s;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String u) {
        this.url = u;
    }

    public String getFileExtension() {
        String fn = getFileName();
        int start = fn.lastIndexOf(".");

        if (start >= 0) {
            int end = fn.length();
            String ext = fn.substring(start + 1, end);
            return ext.toUpperCase();
        } else {
            return "";
        }
    }
}
