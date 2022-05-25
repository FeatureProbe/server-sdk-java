package com.featureprobe.sdk.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;

import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Serve {

    private Integer select;

    private Split split;

    public Serve() {
    }

    public Serve(Integer select) {
        this.select = select;
    }

    public Serve(Split split) {
        this.split = split;
    }

    public HitResult evalIndex(FPUser user, String toggleKey) {
        if (Objects.nonNull(select)) {
            return new HitResult(true, Optional.of(select), Optional.empty());
        }
        return split.findIndex(user, toggleKey);
    }


    public Integer getSelect() {
        return select;
    }

    public void setSelect(Integer select) {
        this.select = select;
    }

    public Split getSplit() {
        return split;
    }

    public void setSplit(Split split) {
        this.split = split;
    }
}
