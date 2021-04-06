package com.fanplayiot.core.remote.pojo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NameLogo {
    private int id;
    @NonNull
    private String name;
    private String logo;

    public NameLogo(int id, @NonNull String name, @Nullable String logo) {
        this.id = id;
        this.name = name;
        this.logo = logo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NonNull String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
