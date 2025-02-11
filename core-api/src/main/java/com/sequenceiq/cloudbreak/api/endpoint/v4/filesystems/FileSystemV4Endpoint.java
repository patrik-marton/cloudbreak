package com.sequenceiq.cloudbreak.api.endpoint.v4.filesystems;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.cloudbreak.api.endpoint.v4.filesystems.responses.FileSystemParameterV4Responses;
import com.sequenceiq.cloudbreak.auth.security.internal.AccountId;
import com.sequenceiq.cloudbreak.doc.ControllerDescription;
import com.sequenceiq.cloudbreak.doc.OperationDescriptions.FileSystemOpDescription;
import com.sequenceiq.cloudbreak.jerseyclient.RetryAndMetrics;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RetryAndMetrics
@Consumes(MediaType.APPLICATION_JSON)
@Path("/v4/{workspaceId}/file_systems")
@Api(value = "/v4/{workspaceId}/file_systems", description = ControllerDescription.FILESYSTEMS_V4_DESCRIPTION, protocols = "http,https",
        consumes = MediaType.APPLICATION_JSON)
public interface FileSystemV4Endpoint {

    @GET
    @Path("parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = FileSystemOpDescription.FILE_SYSTEM_PARAMETERS, produces = MediaType.APPLICATION_JSON, nickname = "getFileSystemParameters")
    FileSystemParameterV4Responses getFileSystemParameters(
            @PathParam("workspaceId") Long workspaceId,
            @NotNull @QueryParam("blueprintName") String blueprintName,
            @NotNull @QueryParam("clusterName") String clusterName,
            @QueryParam("accountName") String accountName,
            @NotNull @QueryParam("storageName") String storageName,
            @NotNull @QueryParam("fileSystemType") String fileSystemType,
            @QueryParam("attachedCluster") @DefaultValue("false") Boolean attachedCluster,
            @QueryParam("secure") @DefaultValue("false") Boolean secure);

    @GET
    @Path("parameters_internal")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = FileSystemOpDescription.FILE_SYSTEM_PARAMETERS_INTERNAL, produces = MediaType.APPLICATION_JSON,
            nickname = "getFileSystemParametersInternal")
    FileSystemParameterV4Responses getFileSystemParametersInternal(
            @PathParam("workspaceId") Long workspaceId,
            @NotNull @QueryParam("blueprintName") String blueprintName,
            @NotNull @QueryParam("clusterName") String clusterName,
            @QueryParam("accountName") String accountName,
            @NotNull @QueryParam("storageName") String storageName,
            @NotNull @QueryParam("fileSystemType") String fileSystemType,
            @QueryParam("attachedCluster") @DefaultValue("false") Boolean attachedCluster,
            @QueryParam("secure") @DefaultValue("false") Boolean secure,
            @QueryParam("accountId") @AccountId String accountId);
}
