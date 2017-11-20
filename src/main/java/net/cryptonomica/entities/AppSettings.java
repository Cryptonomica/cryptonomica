package net.cryptonomica.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * Used to store in Datastore API keys, passwords ect - data that needed by the App,
 * but should not be exposed in open source code
 * Entities should be created only manually in DS by admins
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache // ! should be cashed
public class AppSettings {
    @Id
    private String name; //
    private String value;
    private String info;
    @Index
    private Date changed;
    private Integer integerValue;

    /* --- Constructors: */
    public AppSettings() {
    }

    /* --- Getters and Setters: */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getChanged() {
        return changed;
    }

    public void setChanged(Date changed) {
        this.changed = changed;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }
}
