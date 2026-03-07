package me.combimagnetron.sunscreen.neo.render.engine.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.render.engine.grid.RenderChunk;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class RenderCache {
    private final static int MIN_MAP_ID = Integer.MAX_VALUE - 500;
    private final AtomicInteger mapIdCounter = new AtomicInteger(MIN_MAP_ID);
    private final Table<BigDecimal, Vec3f, Integer> scaleToIdByPositionTable = HashBasedTable.create();
    private final Int2ObjectMap<RenderChunk> idToChunkMap = new Int2ObjectArrayMap<>();
    private final Object2IntMap<RenderChunk> invertedIdToChunkMap = new Object2IntArrayMap<>();
    private final Int2IntMap idToHashcodeMap = new Int2IntArrayMap();

    public void add(@NotNull RenderChunk renderChunk, int mapId) {
        scaleToIdByPositionTable.put(renderChunk.scale(), renderChunk.position(), mapId);
        put(mapId, renderChunk);
        idToHashcodeMap.put(mapId, renderChunk.contentHash());
    }

    public int next(@NotNull RenderChunk renderChunk) {
        int id = mapIdCounter.getAndIncrement();
        add(renderChunk, id);
        return id;
    }

    public void remove(BigDecimal scale) {
        Collection<Integer> ids = scaleToIdByPositionTable.row(scale).values();
        for (int id : ids) {
            remove(id);
        }
        scaleToIdByPositionTable.row(scale).clear();
    }

    public boolean changed(@NotNull RenderChunk renderChunk, int mapId) {
        return renderChunk.contentHash() != idToHashcodeMap.get(mapId);
    }

    public void update(@NotNull RenderChunk renderChunk, int mapId) {
        remove(mapId);
        add(renderChunk, mapId);
    }

    public Integer byPosAndScale(BigDecimal scale, @NotNull Vec3f vec3f) {
        return scaleToIdByPositionTable.get(scale, vec3f);
    }

    public @NotNull Collection<BigDecimal> scales() {
        return scaleToIdByPositionTable.rowKeySet();
    }

    public @NotNull Collection<Integer> idsByScale(@NotNull BigDecimal scale) {
        return scaleToIdByPositionTable.row(scale).values();
    }

    public boolean isEmpty() {
        return scaleToIdByPositionTable.isEmpty();
    }

    public @Nullable RenderChunk get(int mapId) {
        return idToChunkMap.get(mapId);
    }

    public Integer id(@Nullable RenderChunk chunk) {
        return invertedIdToChunkMap.getInt(chunk);
    }

    private void put(int id, @NotNull RenderChunk chunk) {
        idToChunkMap.put(id, chunk);
        invertedIdToChunkMap.put(chunk, id);
    }

    public void remove(int id) {
        RenderChunk chunk = idToChunkMap.remove(id);
        if (chunk != null) invertedIdToChunkMap.removeInt(chunk);
        idToHashcodeMap.remove(id);
    }

    public void remove(int id, @NotNull SunscreenUser<?> user) {
        remove(id);
        SunscreenLibrary.library().intermediate().removeEntity(user, id);
        SunscreenLibrary.library().intermediate().removeEntity(user, id - 500);
    }

    public @Nullable RenderChunk get(BigDecimal scale, @NotNull Vec3f position) {
        Integer id = scaleToIdByPositionTable.get(scale, position);
        if (id == null) return null;
        return idToChunkMap.get((int) id);
    }

    public record CachedChunkMetadata(@NotNull BigDecimal scale, float zIndex) {

    }

}
