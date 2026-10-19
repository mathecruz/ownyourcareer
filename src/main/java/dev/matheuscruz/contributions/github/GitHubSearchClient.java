package dev.matheuscruz.contributions.github;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "github-api")
public interface GitHubSearchClient {

    @GET
    @Path("/search/issues")
    @Produces(MediaType.APPLICATION_JSON)
    GitHubSearchResponse searchIssues(
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("Accept") String accept,
            @QueryParam("q") String query,
            @QueryParam("per_page") int perPage);
}
