package com.async.mail.endpoint.rest.controller.dto;

import java.util.List;

public record CourseListResponse(List<CourseResponse> data, Meta meta) {}
