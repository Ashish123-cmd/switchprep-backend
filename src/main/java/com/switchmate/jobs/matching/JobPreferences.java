package com.switchmate.jobs.matching;

import java.util.Set;

public record JobPreferences(Set<String> preferredRoles, Set<String> preferredLocations) {
}