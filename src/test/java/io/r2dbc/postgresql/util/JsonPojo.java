package io.r2dbc.postgresql.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class JsonPojo {
    
    private String name;
    
    private Integer age;
    
    private Date birthdate;
    
    private Boolean isAdmin;
    
    private List<String> profiles;
    
    private Map<String, Object> attrs;
    
    private Object nullable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public Object getNullable() {
        return nullable;
    }

    public void setNullable(Object nullable) {
        this.nullable = nullable;
    }
}
