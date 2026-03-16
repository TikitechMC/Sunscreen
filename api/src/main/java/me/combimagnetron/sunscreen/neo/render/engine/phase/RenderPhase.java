package me.combimagnetron.sunscreen.neo.render.engine.phase;

import com.google.common.graph.Traverser;
import it.unimi.dsi.fastutil.Pair;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.sunscreen.neo.property.*;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.element.ElementContainer;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import me.combimagnetron.sunscreen.neo.render.engine.cache.RenderCache;
import me.combimagnetron.sunscreen.neo.render.engine.encode.MapEncoderFactory;
import me.combimagnetron.sunscreen.neo.render.engine.grid.EncodedRenderChunk;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public interface RenderPhase<N extends RenderPhase<? extends RenderPhase<?>>> {

    @NotNull Class<@NotNull N> nextType();

    @NotNull Pair<N, RenderContext> advance(@NotNull RenderContext renderContext);

    static @NotNull Process start(@NotNull Collection<ElementLike<?>> unfilteredElements,
            @NotNull SunscreenUser<?> user) {
        return new Process(unfilteredElements, user);
    }

    class Process implements RenderPhase<Draw> {
        private final Map<Meta, List<ModernElement<?, ?>>> elementsByScale = new HashMap<>();
        private final List<ElementLike<?>> elementList = new LinkedList<>();
        private final SunscreenUser<?> user;
        private final Viewport viewport;

        public Process(Collection<ElementLike<?>> unfilteredElements, SunscreenUser<?> user) {
            this.elementList.addAll(unfilteredElements);
            this.viewport = user.screenInfo().viewport();
            this.user = user;
            for (ElementLike<?> unfilteredElement : unfilteredElements) {
                if (!(unfilteredElement instanceof ModernElement<?,?> modernElement)) continue; // add way to retreive it somehow still
                BigDecimal scale = modernElement.scale().rounded();
                Z z = modernElement.z();
                Visibility visibility = modernElement.visibility();
                if (visibility.hide()) continue;
                for (Property<?, ?> property : modernElement.properties()) {
                    if (!(property instanceof RelativeMeasure.RelativeMeasureGroup<?> group)) continue;
                    if (group.value() != null) continue;
                    group.finish(user.screenInfo().viewport());
                }
                List<ModernElement<?, ?>> list = elementsByScale.computeIfAbsent(new Meta(scale, z.value()), (_) -> new ArrayList<>());
                list.add(modernElement);
            }
        }

        @Override
        public @NotNull Class<Draw> nextType() {
            return Draw.class;
        }

        public @NotNull Traverser<ElementLike<?>> treeTraverser() {
            return Traverser.forTree(elementLike -> elementLike instanceof ElementContainer<?> container
                    ? container.children()
                    : List.of());
        }

        public @NotNull Iterable<ElementLike<?>> treeIterator(@NotNull Selector selector) {
            List<ElementLike<?>> elementLikes = new LinkedList<>();
            if (selector == Selector.VISIBLE) {
                elementLikes.addAll(elementList.stream().filter(e -> HoverHelper.in(e, viewport)).toList());
            } else if (selector == Selector.ALL) {
                elementLikes.addAll(elementList);
            }
            return treeTraverser().breadthFirst(elementLikes);
        }

        @Override
        public @NotNull Pair<Draw, RenderContext> advance(@NonNull RenderContext renderContext) {
            Iterable<ElementLike<?>> tree = treeIterator(Selector.VISIBLE);
            return Pair.of(new Draw(elementsByScale, viewport, user), renderContext.withTree(tree));
        }

        public enum Selector {
            VISIBLE, CHANGED, ALL
        }

    }

    class Draw implements RenderPhase<Encode> {
        private final Map<Meta, Canvas> canvasses = new HashMap<>();
        private final Map<Meta, List<ModernElement<?, ?>>> scaleSeparated;
        private final SunscreenUser<?> user;
        private final Viewport viewport;

        public Draw(Map<Meta, List<ModernElement<?, ?>>> scaleSeparated, Viewport viewport, SunscreenUser<?> user) {
            this.viewport = viewport;
            this.scaleSeparated = scaleSeparated;
            this.user = user;
        }

        @Override
        public @NotNull Class<Encode> nextType() {
            return Encode.class;
        }

        @Override
        public @NotNull Pair<Encode, RenderContext> advance(@NonNull RenderContext renderContext) {
            for (Map.Entry<Meta, List<ModernElement<?, ?>>> entry : scaleSeparated.entrySet()) {
                Meta meta = entry.getKey();
                BigDecimal scale = meta.bigDecimal();
                Vec2i viewportVec = viewport.currentView();
                Vec2i canvasSize = Vec2i.of((viewportVec.x() + 127) & ~127, (viewportVec.y() + 127) & ~127);
                canvasses.computeIfAbsent(meta, _ -> Canvas.empty(canvasSize));
                for (ModernElement<?, ?> elementLike : entry.getValue()) {
                    if (!(elementLike instanceof ModernElement<?, ?> element)) continue;
                    GraphicLike<?> graphics = element.render(Size.fixed(canvasSize), renderContext);
                    Vec2i positionVec = PropertyHelper.vectorOrThrow(elementLike.position(), Vec2i.class);
                    canvasses.computeIfPresent(meta,
                        (_, canvas) -> canvas.place(graphics.bufferedColorSpace(), positionVec));
                }
            }
            

            RenderCache cache = renderContext.renderCache();
            for (BigDecimal scale : cache.scales()) {
                if (scaleSeparated.keySet().stream().map(Meta::bigDecimal).toList().contains(scale)) {
                    continue;
                }
                renderContext.markedForRemoval().addAll(cache.idsByScale(scale));
            }
            return Pair.of(new Encode(user), renderContext.withStart(canvasses));
        }

    }

    class Encode implements RenderPhase<Send> {
        private static final int CHUNK_SIZE = 128;
        private final SunscreenUser<?> user;

        public Encode(SunscreenUser<?> user) {
            this.user = user;
        }

        @Override
        public @NotNull Class<Send> nextType() {
            return Send.class;
        }

        @Override
        public @NotNull Pair<Send, RenderContext> advance(@NonNull RenderContext renderContext) {
            List<ProcessedRenderChunk> changedChunks = new ArrayList<>();
            for (Map.Entry<Meta, Canvas> entry : renderContext.start().entrySet()) {
                Meta meta = entry.getKey();
                BigDecimal scale = meta.bigDecimal();
                Canvas canvas = entry.getValue();
                Vec2i canvasSize = canvas.size();
                int chunksX = canvasSize.x() / CHUNK_SIZE;
                int chunksY = canvasSize.y() / CHUNK_SIZE;
                RenderCache cache = renderContext.renderCache();

                for (int x = 0; x < chunksX; x++) {
                    for (int y = 0; y < chunksY; y++) {
                        int startX = x * CHUNK_SIZE;
                        int startY = y * CHUNK_SIZE;
                        int width = Math.min(CHUNK_SIZE, canvasSize.x() - startX);
                        int height = Math.min(CHUNK_SIZE, canvasSize.y() - startY);

                        BufferedColorSpace sub = canvas.bufferedColorSpace().sub(startX, startY, width, height);
                        Vec3f gridPos = Vec3f.of((float) (-1 * (x - 2.62)), (float) (-1 *(y - 1.265)), meta.z());
                        ProcessedRenderChunk newChunk = new ProcessedRenderChunk(sub, gridPos, scale);
                        Integer id = cache.byPosAndScale(scale, gridPos);
                        boolean exists = id != null;

                        if (!exists || renderContext.chunkChangedAt(newChunk, id)) {
                            changedChunks.add(newChunk);

                        }

                    }
                }
            }

            Collection<EncodedRenderChunk> encodedChunks = encodeChunks(changedChunks, renderContext.renderCache());
            return Pair.of(new Send(encodedChunks, user),
                    renderContext);
        }

        private @NotNull Collection<EncodedRenderChunk> encodeChunks(List<ProcessedRenderChunk> changed, RenderCache cache) {
            return changed.stream().map(chunk -> {
                try {
                    byte[] bytes = MapEncoderFactory.encode(chunk);
                    EncodedRenderChunk encodedRenderChunk = new EncodedRenderChunk(bytes, chunk.position(), chunk.scale(), chunk.bufferedColorSpace());
                    Integer id = cache.byPosAndScale(chunk.scale(), chunk.position());
                    boolean exists = id != null;
                    if (exists) cache.update(encodedRenderChunk, id);
                    return encodedRenderChunk;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to encode chunk at pos=" + chunk.position() + " scale=" + chunk.scale(), e);
                }
            }).toList();
        }

    }

    class Send implements RenderPhase<Empty> {
        private final Collection<EncodedRenderChunk> chunks;
        private final SunscreenUser<?> user;

        public Send(Collection<EncodedRenderChunk> chunks, SunscreenUser<?> user) {
            this.chunks = chunks;
            this.user = user;
        }

        @Override
        public @NotNull Class<Empty> nextType() {
            return Empty.class;
        }

        @Override
        public @NotNull Pair<Empty, RenderContext> advance(@NonNull RenderContext renderContext) {
            RenderCache renderCache = renderContext.renderCache();
            PlatformProtocolIntermediate intermediate = SunscreenLibrary.library().intermediate();
            final Location location = user.eyeLocation();
            intermediate.bundleDelimiter(user);
            for (Integer i : renderContext.markedForRemoval()) {
                renderContext.renderCache().remove(i, user);
            }
            for (EncodedRenderChunk chunk : chunks) {
                Integer mapId = renderCache.byPosAndScale(chunk.scale(), chunk.position());
                if (mapId == null) {
                    mapId = renderCache.next(chunk);
                    intermediate.spawnAndFillItemFrame(user, location, chunk.data(), mapId);
                } else {
                    intermediate.updateMap(user, mapId, chunk.data());
                }
            }
            intermediate.bundleDelimiter(user);
            renderContext.markedForRemoval().clear();
            return Pair.of(new Empty(), renderContext);
        }

    }

    class Empty implements RenderPhase<Process> {

        @Override
        public @NotNull Class<Process> nextType() {
            return Process.class;
        }

        @Override
        public @NotNull Pair<Process, RenderContext> advance(@NonNull RenderContext renderContext) {
            return Pair.of(null, renderContext.clear());
        }

    }

    record Meta(@NotNull BigDecimal bigDecimal, float z) {

    }

}
