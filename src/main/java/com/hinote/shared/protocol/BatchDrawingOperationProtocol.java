package com.hinote.shared.protocol;

import java.util.List;

public class BatchDrawingOperationProtocol {
    private List<DrawingOperationProtocol> operations;

    public List<DrawingOperationProtocol> getOperations() {
        return operations;
    }

    public void setOperations(List<DrawingOperationProtocol> operations) {
        this.operations = operations;
    }
}