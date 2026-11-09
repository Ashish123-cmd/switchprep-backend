package com.switchmate.applications.dto;

public record ApplicationView(ApplicationResponse application, CompactJobResponse job, boolean saved) {
}