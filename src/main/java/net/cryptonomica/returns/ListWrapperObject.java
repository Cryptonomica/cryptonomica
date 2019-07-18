package net.cryptonomica.returns;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

public class ListWrapperObject implements Serializable {

    private Integer listSize;
    private List list;

    /* --- Constructor */
    public ListWrapperObject() {
    }

    public ListWrapperObject(List list) {
        this.listSize = list.size();
        this.list = list;
    }

    /* ---- to String */

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /* --- Getters and Setters */

    public Integer getListSize() {
        return listSize;
    }

    public void setListSize(Integer listSize) {
        this.listSize = listSize;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

}
