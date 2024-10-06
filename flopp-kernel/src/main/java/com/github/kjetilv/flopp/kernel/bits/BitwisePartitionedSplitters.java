package com.github.kjetilv.flopp.kernel.bits;

import com.github.kjetilv.flopp.kernel.PartitionedSplitter;
import com.github.kjetilv.flopp.kernel.PartitionedSplitters;
import com.github.kjetilv.flopp.kernel.PartitionedStreams;
import com.github.kjetilv.flopp.kernel.Shape;
import com.github.kjetilv.flopp.kernel.formats.Format;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

final class BitwisePartitionedSplitters implements PartitionedSplitters {

    private final PartitionedStreams streams;

    private final Shape shape;

    BitwisePartitionedSplitters(PartitionedStreams partitionedStreams, Shape shape) {
        this.streams = Objects.requireNonNull(partitionedStreams, "partitionedStreams");
        this.shape = Objects.requireNonNull(shape, "shape");
    }

    @Override
    public <F extends Format<F>> Stream<PartitionedSplitter> splitters(F format) {
        return switch (format.withCharset(shape.charset())) {
            case Format.Csv csv -> streams.streamers()
                .map(streamer ->
                    new BitwiseCsvSplitter(streamer, csv));
            case Format.FwFormat fw -> streams.streamers()
                .map(streamer ->
                    new BitwiseFwSplitter(streamer, fw));
        };
    }

    @Override
    public <F extends Format<F>> Stream<CompletableFuture<PartitionedSplitter>> splitters(
        F format,
        ExecutorService executorService
    ) {
        return switch (format.withCharset(shape.charset())) {
            case Format.Csv csv -> streams.streamers(executorService)
                .map(future ->
                    future.thenApply(streamer ->
                        new BitwiseCsvSplitter(streamer, csv)));
            case Format.FwFormat fw -> streams.streamers(executorService)
                .map(future ->
                    future.thenApply(streamer ->
                        new BitwiseFwSplitter(streamer, fw)));
        };
    }
}
