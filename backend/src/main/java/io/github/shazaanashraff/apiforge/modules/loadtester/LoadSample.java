package io.github.shazaanashraff.apiforge.modules.loadtester;

record LoadSample(long requestedAt, long responseTimeMs, int statusCode, boolean error) {}
