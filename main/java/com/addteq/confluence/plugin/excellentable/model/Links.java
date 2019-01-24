/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author vikashkumar
 */
@XmlRootElement(name = "excellentableShare")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Links {
        private String base;
        private String first;
        private String next;
        private String previous;
        private String last;
        private String self;

    public Links() {
    }

    public Links(String base, String first, String next, String previous, String last, String self) {
        this.base = base;
        this.first = first;
        this.next = next;
        this.previous = previous;
        this.last = last;
        this.self = self;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

}
