package com.github.kjetilv.flopp.kernel.files;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.MemorySegmentSource;
import com.github.kjetilv.flopp.kernel.Partition;
import com.github.kjetilv.flopp.kernel.Shape;

import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

sealed abstract class AbstractFileChannelMemorySegmentSource
    implements MemorySegmentSource
    permits FullMemorySegmentSource, PartialMemorySegmentSource {

    private final Path path;

    private final RandomAccessFile randomAccessFile;

    private final Shape shape;

    private final FileChannel channel;

    private final Arena arena;

    AbstractFileChannelMemorySegmentSource(Path path, Shape shape) {
        this.path = Objects.requireNonNull(path, "path");
        this.randomAccessFile = openRandomAccess(path);
        this.shape = Objects.requireNonNull(shape, "shape");
        this.channel = randomAccessFile.getChannel();
        this.arena = Arena.ofAuto();
    }

    @Override
    public final LineSegment get(Partition partition) {
        long length = partition.length(shape);
        if (length < 0) {
            throw new IllegalStateException("Invalid length " + length + ": " + partition);
        }
        return lineSegment(partition, length);
    }

    @Override
    public final void close() {
        try {
            channel.close();
            randomAccessFile.close();
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to close", e);
        }
    }

    protected MemorySegment memorySegment(long offset, long length) {
        try {
            return channel.map(READ_ONLY, offset, length, arena);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not open " + offset + "+" + length, e);
        }
    }

    protected abstract LineSegment lineSegment(Partition partition, long length);

    private RandomAccessFile openRandomAccess(Path path) {
        try {
            return new RandomAccessFile(path.toFile(), "r");
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not access file", e);
        }
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + path + "]";
    }
}
