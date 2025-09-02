package com.hinote.shared.protocol;

import java.util.List;

public class UndoOperationProtocol {
    private List<String> operationIds;

    public List<String> getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(List<String> operationIds) {
        this.operationIds = operationIds;
    }
}