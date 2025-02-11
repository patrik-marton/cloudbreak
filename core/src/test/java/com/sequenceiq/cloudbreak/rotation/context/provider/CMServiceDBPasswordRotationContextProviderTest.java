package com.sequenceiq.cloudbreak.rotation.context.provider;

import static com.sequenceiq.cloudbreak.rotation.CloudbreakSecretRotationStep.CM_SERVICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sequenceiq.cloudbreak.api.endpoint.v4.database.base.DatabaseType;
import com.sequenceiq.cloudbreak.cmtemplate.configproviders.AbstractRdsRoleConfigProvider;
import com.sequenceiq.cloudbreak.core.bootstrap.service.ClusterDeletionBasedExitCriteriaModel;
import com.sequenceiq.cloudbreak.core.bootstrap.service.container.postgres.PostgresConfigService;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.dto.StackDto;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.rotation.ExitCriteriaProvider;
import com.sequenceiq.cloudbreak.rotation.context.CMServiceConfigRotationContext;
import com.sequenceiq.cloudbreak.rotation.secret.RotationContext;
import com.sequenceiq.cloudbreak.rotation.secret.step.SecretRotationStep;
import com.sequenceiq.cloudbreak.service.GatewayConfigService;
import com.sequenceiq.cloudbreak.service.rdsconfig.AbstractRdsConfigProvider;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;
import com.sequenceiq.cloudbreak.service.stack.StackDtoService;

@ExtendWith(MockitoExtension.class)
public class CMServiceDBPasswordRotationContextProviderTest {

    private static final DatabaseType TEST_DB_TYPE = DatabaseType.HUE;

    @Mock
    private StackDtoService stackService;

    @Mock
    private RdsConfigService rdsConfigService;

    @Mock
    private GatewayConfigService gatewayConfigService;

    @Mock
    private PostgresConfigService postgresConfigService;

    @Mock
    private AbstractRdsRoleConfigProvider rdsRoleConfigProvider;

    @Mock
    private AbstractRdsConfigProvider rdsConfigProvider;

    @Mock
    private ExitCriteriaProvider exitCriteriaProvider;

    @InjectMocks
    private CMServiceDBPasswordRotationContextProvider underTest;

    @Test
    public void testGetContext() throws IllegalAccessException {
        when(exitCriteriaProvider.get(any())).thenReturn(ClusterDeletionBasedExitCriteriaModel.nonCancellableModel());
        FieldUtils.writeDeclaredField(underTest, "rdsRoleConfigProviders", List.of(rdsRoleConfigProvider), true);
        FieldUtils.writeDeclaredField(underTest, "rdsConfigProviders", List.of(rdsConfigProvider), true);
        Cluster cluster = mockCluster();
        StackDto stackDto = mockStack(cluster);
        RDSConfig rdsConfig = mockRdsConfig();
        GatewayConfig gatewayConfig = mockGwConfig();
        when(rdsConfigProvider.getRdsType()).thenReturn(TEST_DB_TYPE);
        when(rdsRoleConfigProvider.dbType()).thenReturn(TEST_DB_TYPE);
        when(rdsConfigProvider.getDbUser()).thenReturn("user");
        when(rdsRoleConfigProvider.dbUserKey()).thenReturn("userconfig");
        when(rdsRoleConfigProvider.dbPasswordKey()).thenReturn("passwordconfig");
        when(rdsRoleConfigProvider.getServiceType()).thenReturn(TEST_DB_TYPE.name());
        when(rdsConfigService.findByClusterId(any())).thenReturn(Set.of(rdsConfig));
        when(rdsConfigService.getClustersUsingResource(any())).thenReturn(Set.of(cluster));
        when(stackService.getByCrn(anyString())).thenReturn(stackDto);
        when(gatewayConfigService.getPrimaryGatewayConfig(any())).thenReturn(gatewayConfig);

        Map<SecretRotationStep, RotationContext> contexts = underTest.getContexts("resource");

        assertEquals(2, ((CMServiceConfigRotationContext) contexts.get(CM_SERVICE)).getCmServiceConfigTable().cellSet().size());
    }

    private static StackDto mockStack(Cluster cluster) {
        StackDto stackDto = mock(StackDto.class);
        when(stackDto.getCluster()).thenReturn(cluster);
        when(stackDto.getResourceCrn()).thenReturn("resource");
        return stackDto;
    }

    private static GatewayConfig mockGwConfig() {
        GatewayConfig gatewayConfig = mock(GatewayConfig.class);
        when(gatewayConfig.getHostname()).thenReturn("host");
        return gatewayConfig;
    }

    private static Cluster mockCluster() {
        Cluster cluster = mock(Cluster.class);
        when(cluster.getId()).thenReturn(1L);
        return cluster;
    }

    private static RDSConfig mockRdsConfig() {
        RDSConfig rdsConfig = mock(RDSConfig.class);
        when(rdsConfig.getConnectionUserNameSecret()).thenReturn("userpath");
        when(rdsConfig.getConnectionPasswordSecret()).thenReturn("passpath");
        when(rdsConfig.getType()).thenReturn(TEST_DB_TYPE.name());
        return rdsConfig;
    }
}
