package com.hkucs.groupproject.model;

public abstract class Model {
    protected String modelId;
    protected String modelName;
    protected Integer totalTokenConsumed;

    public Model() {
    }

    public int getTotalTokenConsumed() {
        return totalTokenConsumed;
    }

    public void setTotalTokenConsumed(Integer totalTokenConsumed) {
        this.totalTokenConsumed = totalTokenConsumed;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
