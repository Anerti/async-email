package com.async.mail.file.hash;

import com.async.mail.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
