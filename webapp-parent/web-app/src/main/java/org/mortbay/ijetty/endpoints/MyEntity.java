package org.mortbay.ijetty.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mortbay.ijetty.config.ObjectMapperProvider;
import org.mortbay.ijetty.config.ToString;

public class MyEntity {
    private String name;
    private String value;

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

    @Override
    public String toString() {
        return ToString.create(this);
    }
}