package com.sequenceiq.freeipa.service.freeipa.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import com.googlecode.jsonrpc4j.JsonRpcClientException;
import com.sequenceiq.freeipa.client.FreeIpaClient;
import com.sequenceiq.freeipa.client.FreeIpaClientException;
import com.sequenceiq.freeipa.client.FreeIpaClientRunnable;
import com.sequenceiq.freeipa.client.model.Host;
import com.sequenceiq.freeipa.client.model.IpaServer;
import com.sequenceiq.freeipa.kerberosmgmt.exception.DeleteException;
import com.sequenceiq.freeipa.service.freeipa.FreeIpaClientRetryService;

@ExtendWith(MockitoExtension.class)
public class HostDeletionServiceTest {

    private static final int NOT_FOUND = 4001;

    @Mock
    private FreeIpaClient freeIpaClient;

    @Mock
    private FreeIpaClientRetryService retryService;

    @InjectMocks
    private HostDeletionService underTest;

    @BeforeEach
    public void init() throws FreeIpaClientException {
        lenient().doAnswer(invocation -> {
            invocation.getArgument(0, FreeIpaClientRunnable.class).run();
            return null;
        }).when(retryService).retryWhenRetryableWithoutValue(any(FreeIpaClientRunnable.class));
    }

    @Test
    public void expectFreeIpaClientExceptionIfHostCollectionFails() throws FreeIpaClientException {
        when(freeIpaClient.findAllHost()).thenThrow(new FreeIpaClientException("error"));
        assertThrows(FreeIpaClientException.class, () -> underTest.removeHosts(freeIpaClient, Set.of("testHost")));
    }

    @Test
    public void expectDeleteExceptionIfHostCollectionFails() throws FreeIpaClientException {
        when(freeIpaClient.findAllHost()).thenThrow(new FreeIpaClientException("error"));
        assertThrows(DeleteException.class, () -> underTest.deleteHostsWithDeleteException(freeIpaClient, Set.of("testHost")));
    }

    @Test
    public void successfulDeletionIfNoHostReturned() throws FreeIpaClientException {
        when(freeIpaClient.findAllHost()).thenReturn(Set.of());
        Set<String> hosts = Set.of("host1", "host2");

        Pair<Set<String>, Map<String, String>> result = underTest.removeHosts(freeIpaClient, hosts);

        assertTrue(result.getSecond().isEmpty());
        assertEquals(0, result.getFirst().size());
    }

    @Test
    public void successfulDeletionIfOneHostReturned() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        Host host = new Host();
        host.setFqdn("host1");
        when(freeIpaClient.findAllHost()).thenReturn(Set.of(host));

        Pair<Set<String>, Map<String, String>> result = underTest.removeHosts(freeIpaClient, hosts);

        assertTrue(result.getSecond().isEmpty());
        assertEquals(1, result.getFirst().size());
        assertEquals(host.getFqdn(), result.getFirst().iterator().next());
        verify(freeIpaClient).deleteHost(eq("host1"));
    }

    @Test
    public void successfulDeletionIfAllHostReturned() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        Host host1 = new Host();
        host1.setFqdn("host1");
        Host host2 = new Host();
        host2.setFqdn("host2");
        when(freeIpaClient.findAllHost()).thenReturn(Set.of(host1, host2));

        Pair<Set<String>, Map<String, String>> result = underTest.removeHosts(freeIpaClient, hosts);

        assertTrue(result.getSecond().isEmpty());
        assertEquals(2, result.getFirst().size());
        assertTrue(result.getFirst().contains(host1.getFqdn()));
        assertTrue(result.getFirst().contains(host2.getFqdn()));
        verify(freeIpaClient).deleteHost(eq("host1"));
        verify(freeIpaClient).deleteHost(eq("host2"));
    }

    @Test
    public void testOneFailedDeletion() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        Host host1 = new Host();
        host1.setFqdn("host1");
        Host host2 = new Host();
        host2.setFqdn("host2");
        when(freeIpaClient.findAllHost()).thenReturn(Set.of(host1, host2));
        when(freeIpaClient.deleteHost(host1.getFqdn())).thenReturn(host1);
        when(freeIpaClient.deleteHost(host2.getFqdn())).thenThrow(new FreeIpaClientException("not handled"));

        Pair<Set<String>, Map<String, String>> result = underTest.removeHosts(freeIpaClient, hosts);

        assertEquals(1, result.getFirst().size());
        assertEquals(1, result.getSecond().size());
        assertTrue(result.getFirst().contains(host1.getFqdn()));
        assertTrue(result.getSecond().keySet().contains(host2.getFqdn()));
        assertTrue(result.getSecond().values().contains("not handled"));
    }

    @Test
    public void testOneAlreadyDeleted() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        Host host1 = new Host();
        host1.setFqdn("host1");
        Host host2 = new Host();
        host2.setFqdn("host2");
        when(freeIpaClient.findAllHost()).thenReturn(Set.of(host1, host2));
        String message = "already deleted";
        when(freeIpaClient.deleteHost(host1.getFqdn())).thenReturn(host1);
        when(freeIpaClient.deleteHost(host2.getFqdn())).thenThrow(new FreeIpaClientException(message, new JsonRpcClientException(NOT_FOUND, message, null)));

        Pair<Set<String>, Map<String, String>> result = underTest.removeHosts(freeIpaClient, hosts);

        assertEquals(2, result.getFirst().size());
        assertEquals(0, result.getSecond().size());
        assertTrue(result.getFirst().contains(host1.getFqdn()));
        assertTrue(result.getFirst().contains(host2.getFqdn()));
    }

    public void expectDeleteExceptionWhenOneFailedDeletion() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        Host host1 = new Host();
        host1.setFqdn("host1");
        Host host2 = new Host();
        host2.setFqdn("host2");
        when(freeIpaClient.findAllHost()).thenReturn(Set.of(host1, host2));
        when(freeIpaClient.deleteHost(host2.getFqdn())).thenThrow(new FreeIpaClientException("not handled"));

        assertThrows(FreeIpaClientException.class, () -> underTest.deleteHostsWithDeleteException(freeIpaClient, hosts));
    }

    @Test
    public void successfulRemoveServersIfOneServerHostReturned() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        IpaServer ipaServer = new IpaServer();
        ipaServer.setCn("host1");
        when(freeIpaClient.findAllServers()).thenReturn(Set.of(ipaServer));

        Pair<Set<String>, Map<String, String>> result = underTest.removeServers(freeIpaClient, hosts);

        assertTrue(result.getSecond().isEmpty());
        assertEquals(1, result.getFirst().size());
        assertEquals(ipaServer.getCn(), result.getFirst().iterator().next());
        verify(freeIpaClient).deleteServer(eq("host1"));
    }

    @Test
    public void successfulRemoveServersIfAllHostReturned() throws FreeIpaClientException {
        Set<String> hosts = Set.of("host1", "host2");
        IpaServer ipaServer1 = new IpaServer();
        ipaServer1.setCn("host1");
        IpaServer ipaServer2 = new IpaServer();
        ipaServer2.setCn("host2");
        when(freeIpaClient.findAllServers()).thenReturn(Set.of(ipaServer1, ipaServer2));

        Pair<Set<String>, Map<String, String>> result = underTest.removeServers(freeIpaClient, hosts);

        assertTrue(result.getSecond().isEmpty());
        assertEquals(2, result.getFirst().size());
        assertTrue(result.getFirst().contains(ipaServer1.getCn()));
        assertTrue(result.getFirst().contains(ipaServer2.getCn()));
        verify(freeIpaClient).deleteServer(eq("host1"));
        verify(freeIpaClient).deleteServer(eq("host2"));
    }
}