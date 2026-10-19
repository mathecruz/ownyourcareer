package dev.matheuscruz.contributions.github;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "github-api")
public interface GitHubIssuesClient {

    @POST
    @Path("/repos/{owner}/{repo}/issues")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    GitHubIssueResponse createIssue(
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("Accept") String accept,
            @PathParam("owner") String owner,
            @PathParam("repo") String repo,
            GitHubIssueRequest issue);
}
