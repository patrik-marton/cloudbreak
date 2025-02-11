package com.sequenceiq.flow.rotation.chain;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sequenceiq.cloudbreak.rotation.secret.RotationFlowExecutionType;
import com.sequenceiq.cloudbreak.rotation.secret.SecretType;
import com.sequenceiq.flow.reactor.api.event.BaseFlowEvent;
import com.sequenceiq.flow.rotation.serialization.SecretTypeListDeserializer;
import com.sequenceiq.flow.rotation.serialization.SecretTypeListSerializer;

public class SecretRotationFlowChainTriggerEvent extends BaseFlowEvent {

    @JsonSerialize(using = SecretTypeListSerializer.class)
    @JsonDeserialize(using = SecretTypeListDeserializer.class)
    private final List<SecretType> secretTypes;

    private final RotationFlowExecutionType executionType;

    @JsonCreator
    public SecretRotationFlowChainTriggerEvent(@JsonProperty("selector") String selector,
            @JsonProperty("resourceId") Long resourceId,
            @JsonProperty("resourceCrn") String resourceCrn,
            @JsonProperty("secretTypes") List<SecretType> secretTypes,
            @JsonProperty("executionType") RotationFlowExecutionType executionType) {
        super(selector, resourceId, resourceCrn);
        this.secretTypes = secretTypes;
        this.executionType = executionType;
    }

    public List<SecretType> getSecretTypes() {
        return secretTypes;
    }

    public RotationFlowExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public boolean equalsEvent(BaseFlowEvent other) {
        return isClassAndEqualsEvent(SecretRotationFlowChainTriggerEvent.class, other,
                event -> Objects.equals(secretTypes, event.secretTypes) &&
                        Objects.equals(executionType, event.executionType));
    }

    @Override
    public String toString() {
        return "SecretRotationFlowChainTriggerEvent{" +
                "secretTypes=" + secretTypes +
                ", executionType=" + executionType +
                ", resourceId=" + getResourceId() +
                ", resourceCrn='" + getResourceCrn() +
                '}';
    }
}
