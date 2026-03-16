package me.combimagnetron.sunscreen.bench;

import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.render.engine.encode.MapEncoderIHateMyselfMore;
import me.combimagnetron.sunscreen.neo.render.engine.encode.MapEncoderSimd;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, batchSize = 1)
@Measurement(iterations = 5, batchSize = 1)
@Fork(value = 1, jvmArgsAppend = {"--add-modules=jdk.incubator.vector"})
public class MapEncoderBenchmark {

    private ProcessedRenderChunk renderChunk;

    @Setup(Level.Trial)
    public void setup() {
        BufferedColorSpace bufferedColorSpace = Canvas.url("https://i.imgur.com/6BLwWdu.png").bufferedColorSpace();
        renderChunk = new ProcessedRenderChunk(bufferedColorSpace, Vec3f.of(1.56f, 2.85f, 0f), new BigDecimal("0.56"));
    }

    @Benchmark
    public byte[] encodeScalar() {
        MapEncoderIHateMyselfMore encoder = new MapEncoderIHateMyselfMore(renderChunk);
        return encoder.bytes().toByteArray();
    }

    @Benchmark
    public byte[] encodeSimd() {
        MapEncoderSimd encoder = new MapEncoderSimd(renderChunk);
        return encoder.bytes().toByteArray();
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
            .include(MapEncoderBenchmark.class.getSimpleName())
            .build()
        ).run();
    }
}
