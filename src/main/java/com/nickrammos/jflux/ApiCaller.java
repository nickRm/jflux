package com.nickrammos.jflux;

import java.io.IOException;

/**
 * Convenience class, used to easily call the InfluxDB API while centralizing exception handling.
 */
final class ApiCaller {

    /**
     * Calls the API method.
     *
     * @param apiMethod the method to call
     *
     * @throws IllegalStateException if the API is not reachable
     */
    void callApi(IOThrowingRunnable apiMethod) {
        try {
            apiMethod.run();
        } catch (IOException e) {
            throw new IllegalStateException("Connection to InfluxDB lost", e);
        }
    }

    /**
     * Calls the API method.
     *
     * @param apiMethod the method to call
     * @param <T>       type of the call's result
     *
     * @return the call's result
     *
     * @throws IllegalStateException if the API is not reachable
     */
    <T> T callApi(IOThrowingSupplier<T> apiMethod) {
        try {
            return apiMethod.get();
        } catch (IOException e) {
            throw new IllegalStateException("Connection to InfluxDB lost", e);
        }
    }

    /**
     * Convenience interface for runnables that throw IOExceptions.
     */
    interface IOThrowingRunnable {

        void run() throws IOException;
    }

    /**
     * Convenience interface for suppliers that throw IOExceptions.
     *
     * @param <T> type of the return value
     */
    interface IOThrowingSupplier<T> {

        T get() throws IOException;
    }
}
