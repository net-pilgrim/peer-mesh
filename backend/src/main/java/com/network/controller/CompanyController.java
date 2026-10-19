package com.network.controller;

import com.network.model.Company;
import com.network.repository.NetworkRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.List;

@Controller("/api/companies")
@ExecuteOn(TaskExecutors.BLOCKING)
public class CompanyController {

    private final NetworkRepository repository;

    public CompanyController(NetworkRepository repository) {
        this.repository = repository;
    }

    @Get
    public List<Company> listCompanies() {
        return repository.getCompanies();
    }

    @Get("/{handleOrId}")
    public HttpResponse<Company> getCompany(@PathVariable String handleOrId) {
        return repository.getCompany(handleOrId)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::notFound);
    }
}
