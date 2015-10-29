/*
 * Copyright (c) 2015, Salesforce.com, Inc.
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

package com.desk.java.apiclient;

import com.desk.java.apiclient.model.CaseLock;
import com.desk.java.apiclient.service.ArticleService;
import com.desk.java.apiclient.service.CaseService;
import com.desk.java.apiclient.service.CompanyService;
import com.desk.java.apiclient.service.CustomFieldsService;
import com.desk.java.apiclient.service.CustomerService;
import com.desk.java.apiclient.service.FilterService;
import com.desk.java.apiclient.service.GroupService;
import com.desk.java.apiclient.service.InboundMailboxService;
import com.desk.java.apiclient.service.LabelService;
import com.desk.java.apiclient.service.MacroService;
import com.desk.java.apiclient.service.OutboundMailboxService;
import com.desk.java.apiclient.service.PermissionService;
import com.desk.java.apiclient.service.SiteService;
import com.desk.java.apiclient.service.TopicService;
import com.desk.java.apiclient.service.TwitterUserService;
import com.desk.java.apiclient.service.UserService;
import com.desk.java.apiclient.util.ApiTokenSigningInterceptor;
import com.desk.java.apiclient.util.DeskClientUtils;
import com.desk.java.apiclient.util.ISO8601DateAdapter;
import com.desk.java.apiclient.util.OAuthSigningInterceptor;
import com.desk.java.apiclient.util.RetrofitHttpOAuthConsumer;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import retrofit.CallAdapter;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import static com.desk.java.apiclient.DeskClient.AuthType.OAUTH;

/**
 * <p>
 *     Client which interfaces with the Desk API.
 * </p>
 *
 * Created by Matt Kranzler on 4/27/15.
 * Copyright (c) 2015 Desk.com. All rights reserved.
 */
public class DefaultDeskClient implements DeskClient {

    private final String hostname;
    private final String apiToken;
    private final String consumerKey;
    private final String consumerSecret;
    private final String accessToken;
    private final String accessTokenSecret;
    private final Cache responseCache;
    private final List<Interceptor> applicationInterceptors;
    private final List<Interceptor> networkInterceptors;
    private final AuthType authType;

    private Retrofit restAdapter;
    private RetrofitHttpOAuthConsumer oAuthConsumer;
    private UserService userService;
    private SiteService siteService;
    private LabelService labelService;
    private CustomFieldsService customFieldsService;
    private GroupService groupService;
    private MacroService macroService;
    private OutboundMailboxService outboundMailboxService;
    private FilterService filterService;
    private CaseService caseService;
    private CompanyService companyService;
    private CustomerService customerService;
    private PermissionService permissionService;
    private TwitterUserService twitterUserService;
    private TopicService topicService;
    private ArticleService articleService;
    private InboundMailboxService inboundMailboxService;

    DefaultDeskClient(Builder builder) {
        this.authType = builder.authType;
        this.apiToken = builder.apiToken;
        this.hostname = builder.hostname;
        this.consumerKey = builder.consumerKey;
        this.consumerSecret = builder.consumerSecret;
        this.accessToken = builder.accessToken;
        this.accessTokenSecret = builder.accessTokenSecret;
        this.responseCache = builder.responseCache;
        this.applicationInterceptors = builder.applicationInterceptors;
        this.networkInterceptors = builder.networkInterceptors;
        this.oAuthConsumer = createOAuthConsumer();

        Retrofit.Builder retrofitBuilder = createRestAdapter();
        if (builder.callAdapters != null && !builder.callAdapters.isEmpty()) {
            for (CallAdapter.Factory callAdapter : builder.callAdapters) {
                retrofitBuilder.addCallAdapterFactory(callAdapter);
            }
        }

        this.restAdapter = retrofitBuilder.build();
    }

    @Override
    public String getHostname() {
        return this.hostname;
    }

    @Override
    public String getUrl(String path) {
        return DeskClientUtils.buildUrl(hostname, path);
    }

    @Override
    public String signUrl(String url) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        if (OAUTH == authType) {
            return oAuthConsumer.sign(url);
        } else {
            return url;
        }
    }

    @Override
    public void clearResponseCache() {
        if (responseCache != null) {
            try {
                responseCache.evictAll();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @NotNull
    @Override
    public UserService users() {
        if (userService == null) {
            userService = restAdapter.create(UserService.class);
        }
        return userService;
    }

    @NotNull
    @Override
    public SiteService sites() {
        if (siteService == null) {
            siteService = restAdapter.create(SiteService.class);
        }
        return siteService;
    }

    @NotNull
    @Override
    public LabelService labels() {
        if (labelService == null) {
            labelService = restAdapter.create(LabelService.class);
        }
        return labelService;
    }

    @NotNull
    @Override
    public CustomFieldsService customFields() {
        if (customFieldsService == null) {
            customFieldsService = restAdapter.create(CustomFieldsService.class);
        }
        return customFieldsService;
    }

    @NotNull
    @Override
    public GroupService groups() {
        if (groupService == null) {
            groupService = restAdapter.create(GroupService.class);
        }
        return groupService;
    }

    @NotNull
    @Override
    public MacroService macros() {
        if (macroService == null) {
            macroService = restAdapter.create(MacroService.class);
        }
        return macroService;
    }

    @NotNull
    @Override
    public OutboundMailboxService outboundMailboxes() {
        if (outboundMailboxService == null) {
            outboundMailboxService = restAdapter.create(OutboundMailboxService.class);
        }
        return outboundMailboxService;
    }

    @NotNull
    @Override
    public FilterService filters() {
        if (filterService == null) {
            filterService = restAdapter.create(FilterService.class);
        }
        return filterService;
    }

    @NotNull
    @Override
    public CaseService cases() {
        if (caseService == null) {
            caseService = restAdapter.create(CaseService.class);
        }
        return caseService;
    }

    @NotNull
    @Override
    public CompanyService companies() {
        if (companyService == null) {
            companyService = restAdapter.create(CompanyService.class);
        }
        return companyService;
    }

    @NotNull
    @Override
    public CustomerService customers() {
        if (customerService == null) {
            customerService = restAdapter.create(CustomerService.class);
        }
        return customerService;
    }

    @NotNull
    @Override
    public PermissionService permissions() {
        if (permissionService == null) {
            permissionService = restAdapter.create(PermissionService.class);
        }
        return permissionService;
    }

    @NotNull
    @Override
    public TwitterUserService twitterUsers() {
        if (twitterUserService == null) {
            twitterUserService = restAdapter.create(TwitterUserService.class);
        }
        return twitterUserService;
    }

    @NotNull
    @Override
    public TopicService topics() {
        if (topicService == null) {
            topicService = restAdapter.create(TopicService.class);
        }
        return topicService;
    }

    @NotNull
    @Override
    public ArticleService articles() {
        if (articleService == null) {
            articleService = restAdapter.create(ArticleService.class);
        }
        return articleService;
    }

    @NotNull
    @Override
    public InboundMailboxService inboundMailboxes() {
        if (inboundMailboxService == null) {
            inboundMailboxService = restAdapter.create(InboundMailboxService.class);
        }
        return inboundMailboxService;
    }

    /**
     * Package access for testing purposes
     */
    Retrofit getRestAdapter() {
        return restAdapter;
    }

    private Retrofit.Builder createRestAdapter() {
        return new Retrofit.Builder()
                .baseUrl(getUrl(API_BASE_PATH))
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(createGson()));
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new ISO8601DateAdapter())
                .registerTypeAdapter(CaseLock.class, CaseLock.TYPE_ADAPTER)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();

        // if we have response cache let's use it!
        if (responseCache != null) {
            okHttpClient.setCache(responseCache);
        }

        // add auth interceptors
        switch (authType) {
            case OAUTH:
                if (oAuthConsumer == null) {
                    throw new IllegalStateException("a RetrofitHttpOAuthConsumer must be created before creating OKClient");
                }
                okHttpClient.interceptors().add(new OAuthSigningInterceptor(oAuthConsumer));
                break;
            case API_TOKEN:
                okHttpClient.interceptors().add(new ApiTokenSigningInterceptor(apiToken));
                break;
            default:
                throw new IllegalStateException("AuthType " + authType + " isn't supported.");
        }

        // add all other application interceptors
        if (applicationInterceptors != null && !applicationInterceptors.isEmpty()) {
            okHttpClient.interceptors().addAll(applicationInterceptors);
        }

        // add all other network interceptors
        if (networkInterceptors != null && !networkInterceptors.isEmpty()) {
            okHttpClient.networkInterceptors().addAll(networkInterceptors);
        }

        return okHttpClient;
    }

    private RetrofitHttpOAuthConsumer createOAuthConsumer() {
        if (OAUTH == authType) {
            RetrofitHttpOAuthConsumer consumer = new RetrofitHttpOAuthConsumer(consumerKey, consumerSecret);
            consumer.setTokenWithSecret(accessToken, accessTokenSecret);
            return consumer;
        } else {
            return null;
        }
    }
}
