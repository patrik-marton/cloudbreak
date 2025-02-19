package com.sequenceiq.flow.rotation.chain;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.flow.core.chain.FlowEventChainFactory;
import com.sequenceiq.flow.core.chain.config.FlowTriggerEventQueue;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.rotation.event.SecretRotationTriggerEvent;
import com.sequenceiq.flow.rotation.status.event.RotationStatusChangeTriggerEvent;

@Component
public class SecretRotationFlowEventChainFactory implements FlowEventChainFactory<SecretRotationFlowChainTriggerEvent> {

    @Override
    public String initEvent() {
        return EventSelectorUtil.selector(SecretRotationFlowChainTriggerEvent.class);
    }

    @Override
    public FlowTriggerEventQueue createFlowTriggerEventQueue(SecretRotationFlowChainTriggerEvent event) {
        Queue<Selectable> flowEventChain = new ConcurrentLinkedQueue<>();
        flowEventChain.add(RotationStatusChangeTriggerEvent.fromChainTrigger(event, true));
        event.getSecretTypes().forEach(secretType -> {
            flowEventChain.add(SecretRotationTriggerEvent.fromChainTrigger(event, secretType));
        });
        flowEventChain.add(RotationStatusChangeTriggerEvent.fromChainTrigger(event, false));
        return new FlowTriggerEventQueue(getName(), event, flowEventChain);
    }
}
