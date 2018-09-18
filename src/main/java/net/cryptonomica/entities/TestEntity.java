package net.cryptonomica.entities;


import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Entity // -> net.cryptonomica.service.OfyService
public class TestEntity implements Serializable {

    /* types:
     * see: https://cloud.google.com/appengine/docs/standard/java/datastore/entities (see: The following value types are supported:)
     * */

    @Id
    private String entityName;
    @Index
    private Double doubleProperty;
    @Index
    private Boolean booleanProperty;
    @Index
    private String stringProperty;
    // Up to 1 megabyte, not indexed
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Text
    private Text textProperty;
    //
    // Up to 1500 bytes
    // ShortBlob contains an array of bytes no longer than DataTypeUtils.MAX_SHORT_BLOB_PROPERTY_LENGTH.
    // Unlike Blob, ShortBlobs are indexed by the datastore and can therefore be filtered and sorted on in queries.
    // If your data is too large to fit in a ShortBlob use Blob instead.
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/ShortBlob
    @Index
    private ShortBlob shortBlobProperty;
    //
    // Up to 1 megabyte, not indexed
    // To store files, particularly files larger than this 1MB limit, look at the Blobstore API.
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Blob
    private Blob blobProperty;
    @Index
    private Date dateProperty;
    @Index
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/PostalAddress
    private PostalAddress postalAddressProperty;
    @Index
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/PhoneNumber
    private PhoneNumber phoneNumberProperty;
    @Index
    // An e-mail address datatype. Makes no attempt at validation.
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Email
    private Email emailProperty;
    @Index
    // User represents a specific user, represented by the combination of an email address
    // and a specific Google Apps domain (which we call an authDomain). For normal Google login, authDomain will be set to "gmail.com".
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/users/User
    private User userProperty;
    @Index
    // An instant messaging handle. Includes both an address and its protocol. The protocol value is either a standard IM scheme
    // (legal scheme values are defined by IMHandle.Scheme or a URL identifying the IM network for the protocol (e.g. http://aim.com/).
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/IMHandle
    private IMHandle imHandleProperty;
    @Index
    // A Link is a URL of limited length.
    //In addition to adding the meaning of URL onto a String, a Link can also be longer than a Text value, with a limit of 2083 characters.
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Link
    private Link linkProperty;
    @Index
    // A tag, ie a descriptive word or phrase. Entities may be tagged by users, and later returned by a queries for that tag.
    // Tags can also be used for ranking results (frequency), photo captions, clustering, activity, etc.
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Category
    private Category categoryProperty;
    @Index
    // A user-provided integer rating for a piece of content. Normalized to a 0-100 scale.
    // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Rating
    private Rating ratingProperty;
    //
    // Collections and arrays are embedded normally, and can contain nested classes. You are encouraged to initialize collections in your constructor
    // If you do not initialize collections, Objectify will take a best guess at the appropriate type based on the field
    // (eg List will be initialized to ArrayList).
    private ArrayList<String> arrayListProperty;
    //
    // Objectify will store a Map<String, ?> as an EmbeddedEntity in the datastore
    // In order to use non-String keys with Maps, you may specify the @Stringify annotation
    private Map<String, String> mapProperty;
    @Index
    private Date entityCreatedOn;
    @Index
    private User entityCreatedBy;
    @Index
    private Integer entityCreatedOnYear;
    @Index
    private Integer entityCreatedOnMonth;
    @Index
    private Integer entityCreatedOnDay;

    /* ---- Constructor */

    public TestEntity() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.entityCreatedOnYear = localDate.getYear();
        this.entityCreatedOnMonth = localDate.getMonthValue();
        this.entityCreatedOnDay = localDate.getDayOfMonth();
    }

    /* ---- Getters and Setters */

    // (!) custom:
    public void setEntityCreatedOn(Date entityCreatedOn) {
        this.entityCreatedOn = entityCreatedOn;
        LocalDate localDate = this.entityCreatedOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.entityCreatedOnYear = localDate.getYear();
        this.entityCreatedOnMonth = localDate.getMonthValue();
        this.entityCreatedOnDay = localDate.getDayOfMonth();
    }

    // >>>

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(Double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    public Boolean getBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(Boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Text getTextProperty() {
        return textProperty;
    }

    public void setTextProperty(Text textProperty) {
        this.textProperty = textProperty;
    }

    public ShortBlob getShortBlobProperty() {
        return shortBlobProperty;
    }

    public void setShortBlobProperty(ShortBlob shortBlobProperty) {
        this.shortBlobProperty = shortBlobProperty;
    }

    public Blob getBlobProperty() {
        return blobProperty;
    }

    public void setBlobProperty(Blob blobProperty) {
        this.blobProperty = blobProperty;
    }

    public Date getDateProperty() {
        return dateProperty;
    }

    public void setDateProperty(Date dateProperty) {
        this.dateProperty = dateProperty;
    }

    public PostalAddress getPostalAddressProperty() {
        return postalAddressProperty;
    }

    public void setPostalAddressProperty(PostalAddress postalAddressProperty) {
        this.postalAddressProperty = postalAddressProperty;
    }

    public PhoneNumber getPhoneNumberProperty() {
        return phoneNumberProperty;
    }

    public void setPhoneNumberProperty(PhoneNumber phoneNumberProperty) {
        this.phoneNumberProperty = phoneNumberProperty;
    }

    public Email getEmailProperty() {
        return emailProperty;
    }

    public void setEmailProperty(Email emailProperty) {
        this.emailProperty = emailProperty;
    }

    public User getUserProperty() {
        return userProperty;
    }

    public void setUserProperty(User userProperty) {
        this.userProperty = userProperty;
    }

    public IMHandle getImHandleProperty() {
        return imHandleProperty;
    }

    public void setImHandleProperty(IMHandle imHandleProperty) {
        this.imHandleProperty = imHandleProperty;
    }

    public Link getLinkProperty() {
        return linkProperty;
    }

    public void setLinkProperty(Link linkProperty) {
        this.linkProperty = linkProperty;
    }

    public Category getCategoryProperty() {
        return categoryProperty;
    }

    public void setCategoryProperty(Category categoryProperty) {
        this.categoryProperty = categoryProperty;
    }

    public Rating getRatingProperty() {
        return ratingProperty;
    }

    public void setRatingProperty(Rating ratingProperty) {
        this.ratingProperty = ratingProperty;
    }

    public ArrayList<String> getArrayListProperty() {
        return arrayListProperty;
    }

    public void setArrayListProperty(ArrayList<String> arrayListProperty) {
        this.arrayListProperty = arrayListProperty;
    }

    public Map<String, String> getMapProperty() {
        return mapProperty;
    }

    public void setMapProperty(Map<String, String> mapProperty) {
        this.mapProperty = mapProperty;
    }

    public Date getEntityCreatedOn() {
        return entityCreatedOn;
    }

    public User getEntityCreatedBy() {
        return entityCreatedBy;
    }

    public void setEntityCreatedBy(User entityCreatedBy) {
        this.entityCreatedBy = entityCreatedBy;
    }


    public Integer getEntityCreatedOnYear() {
        return entityCreatedOnYear;
    }

    public void setEntityCreatedOnYear(Integer entityCreatedOnYear) {
        this.entityCreatedOnYear = entityCreatedOnYear;
    }

    public Integer getEntityCreatedOnMonth() {
        return entityCreatedOnMonth;
    }

    public void setEntityCreatedOnMonth(Integer entityCreatedOnMonth) {
        this.entityCreatedOnMonth = entityCreatedOnMonth;
    }

    public Integer getEntityCreatedOnDay() {
        return entityCreatedOnDay;
    }

    public void setEntityCreatedOnDay(Integer entityCreatedOnDay) {
        this.entityCreatedOnDay = entityCreatedOnDay;
    }
}
