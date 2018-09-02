package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API key for services
 * Cryptonomica can provide API key to services that can consume data from Cryptonomica data base
 */
@Entity // -> net.cryptonomica.service.OfyService
public class ApiKey
        implements Serializable { //

    @Id
    private String serviceName;

    @Index
    private String apiKey; // case sensitive, by default: RandomStringUtils.randomAlphanumeric(33)

    @Index
    private Date validUntil;

    @Index
    private Date entityCreated;

    @Index
    private Email createdBy;

    /* permissions to use API methods: */
    @Index
    private Boolean getPGPPublicKeyByFingerprintWithApiKey;

    @Index
    private Boolean createPromoCodeWithApiKey;

    @Index
    private Integer discountInPercentForPromoCodes;

    private List<String> permissions;

    @Index
    private Link revocationWebhookUrl;
    @Index
    private Boolean revocationWebhookRegistered;

    /* --- Constructors */

    public ApiKey() {
        // this.apiKey = RandomStringUtils.randomAlphanumeric(33);
        this.entityCreated = new Date();
        this.permissions = new ArrayList<>();
    }

    /* --- Methods */

    public Boolean addPermission(String permissionName) {
        if (permissionName == null || permissionName.isEmpty()) {
            throw new IllegalArgumentException("permissionName is null or empty");
        }
        return this.permissions.add(permissionName);
    }

    /* --- getters and setters */

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }

    public Email getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Email createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getGetPGPPublicKeyByFingerprintWithApiKey() {
        return getPGPPublicKeyByFingerprintWithApiKey;
    }

    public void setGetPGPPublicKeyByFingerprintWithApiKey(Boolean getPGPPublicKeyByFingerprintWithApiKey) {
        this.getPGPPublicKeyByFingerprintWithApiKey = getPGPPublicKeyByFingerprintWithApiKey;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean getCreatePromoCodeWithApiKey() {
        return createPromoCodeWithApiKey;
    }

    public void setCreatePromoCodeWithApiKey(Boolean createPromoCodeWithApiKey) {
        this.createPromoCodeWithApiKey = createPromoCodeWithApiKey;
    }

    public Integer getDiscountInPercentForPromoCodes() {
        return discountInPercentForPromoCodes;
    }

    public void setDiscountInPercentForPromoCodes(Integer discountInPercentForPromoCodes) {
        this.discountInPercentForPromoCodes = discountInPercentForPromoCodes;
    }

    public Link getRevocationWebhookUrl() {
        return revocationWebhookUrl;
    }

    public void setRevocationWebhookUrl(Link revocationWebhookUrl) {
        this.revocationWebhookUrl = revocationWebhookUrl;
    }

    public Boolean getRevocationWebhookRegistered() {
        return revocationWebhookRegistered;
    }

    public void setRevocationWebhookRegistered(Boolean revocationWebhookRegistered) {
        this.revocationWebhookRegistered = revocationWebhookRegistered;
    }

}
