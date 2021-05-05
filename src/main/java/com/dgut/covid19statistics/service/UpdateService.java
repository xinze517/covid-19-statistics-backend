package com.dgut.covid19statistics.service;

import java.util.concurrent.CompletableFuture;

public interface UpdateService {

    boolean downLoadFiles();

    CompletableFuture<Boolean> insertRecoveredGlobalToDB();

    CompletableFuture<Boolean> insertConfirmedGlobalToDB();

    CompletableFuture<Boolean> insertDeathsGlobalToDB();

}
