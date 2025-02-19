package com.sequenceiq.datalake.service.rotation.context.provider;

import static com.sequenceiq.cloudbreak.rotation.CloudbreakSecretType.CLUSTER_CB_CM_ADMIN_PASSWORD;
import static com.sequenceiq.cloudbreak.rotation.secret.step.CommonSecretRotationStep.CLOUDBREAK_ROTATE_POLLING;
import static com.sequenceiq.sdx.rotation.DatalakeSecretType.DATALAKE_CB_CM_ADMIN_PASSWORD;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.rotation.secret.RotationContext;
import com.sequenceiq.cloudbreak.rotation.secret.RotationContextProvider;
import com.sequenceiq.cloudbreak.rotation.secret.SecretType;
import com.sequenceiq.cloudbreak.rotation.secret.context.PollerRotationContext;
import com.sequenceiq.cloudbreak.rotation.secret.step.SecretRotationStep;

@Component
public class DatalakeCMAdminPasswordRotationContextProvider implements RotationContextProvider {

    @Override
    public Map<SecretRotationStep, RotationContext> getContexts(String resourceCrn) {
        return Map.of(CLOUDBREAK_ROTATE_POLLING, new PollerRotationContext(resourceCrn, CLUSTER_CB_CM_ADMIN_PASSWORD));
    }

    @Override
    public SecretType getSecret() {
        return DATALAKE_CB_CM_ADMIN_PASSWORD;
    }
}
