package com.async.mail.endpoint.rest.controller.dto;

import java.util.List;

public record DataListResponse(List<DataResponse> data, Meta meta) {}
