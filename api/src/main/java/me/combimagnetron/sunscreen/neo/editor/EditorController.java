package me.combimagnetron.sunscreen.neo.editor;

import com.google.common.collect.ImmutableMap;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.element.EditorElements;
import me.combimagnetron.sunscreen.neo.editor.element.EditorNameElement;
import me.combimagnetron.sunscreen.neo.editor.element.PageNameElement;
import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.neo.editor.project.EditorProject;
import me.combimagnetron.sunscreen.neo.editor.template.EditorMenuTemplate;
import me.combimagnetron.sunscreen.neo.editor.template.EditorStartOverviewMenuTemplate;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualPage;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualTheme;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.Argument;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ArgumentInfo;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.LabelElement;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Visibility;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EditorController {
    private final Map<ElementConstructionProvider<?>, List<Argument<?>>> providerArgumentMap = new HashMap<>();
    private final LinkedHashMap<Identifier, VirtualPage> pages = new LinkedHashMap<>();
    private final LinkedList<Identifier> pagesList = new LinkedList<>();
    private final SunscreenUser<?> user;
    private RenderContext renderContext = new RenderContext(null, null, List.of());
    private ElementConstructionProvider<?> provider;
    private VirtualTheme theme;
    private ActiveMenu active;
    private Identifier selectedPage;

    public EditorController(SunscreenUser<?> user) {
        this.user = user;
    }

    public void start() {
        active = user.open(new EditorStartOverviewMenuTemplate(this)).menu();
    }

    public void open(@NotNull EditorProject project) {
        active.show(new EditorMenuTemplate(this));
    }

    public @NotNull List<VirtualPage> pages() {
        return pagesList.stream().map(this::page).toList();
    }

    public void select(int index) {
        select(pagesList.get(index));
    }

    public @NotNull RenderContext clearedContext() {
        return renderContext.clear();
    }

    public @NotNull RenderContext context() {
        return renderContext;
    }

    public @NotNull VirtualTheme theme() {
        return theme;
    }

    public @NotNull EditorController theme(@NotNull VirtualTheme theme) {
        this.theme = theme;
        this.renderContext = renderContext.withComponents(List.of(theme));
        return this;
    }

    public @NotNull EditorController page(@NotNull VirtualPage page) {
        pages.put(page.identifier(), page);
        pagesList.add(page.identifier());
        return this;
    }

    public @Nullable VirtualPage selected() {
        return page(selectedPage);
    }

    public @NotNull EditorController select(@NotNull Identifier identifier) {
        this.selectedPage = identifier;
        return this;
    }

    public @NotNull EditorController select(@NotNull VirtualPage page) {
        return select(page.identifier());
    }

    public @Nullable VirtualPage page(@NotNull Identifier identifier) {
        return pages.get(identifier);
    }

    public void editor(@NotNull Identifier identifier, @NotNull String displayName, @NotNull ModernTheme theme) {
        this.theme = new VirtualTheme(theme.identifier());
        for (ThemeDecorator decorator : theme.decorators()) {
            this.theme.decorator(decorator);
            theme(this.theme);
        }
        active.show(new EditorMenuTemplate(this));
    }

    public void elementSetup(@NotNull ElementConstructionProvider<?> provider) {
        try {
            showElementSetup(provider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @Nullable ElementConstructionProvider<?> provider() {
        return provider;
    }

    public @NotNull Map<ElementConstructionProvider<?>, List<Argument<?>>> providerArgumentMap() {
        return providerArgumentMap;
    }

    public @NotNull ActiveMenu menu() {
        return active;
    }

    @SuppressWarnings("unchecked")
    private void showElementSetup(@NotNull ElementConstructionProvider<?> provider) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Identifier layoutId = Identifier.of("new_element/wizard");
        Layout<?> layout = (Layout<?>) active.element(layoutId);
        layout.visibility(Visibility.visible());
        int y = 11;
        for (ArgumentInfo.TypeInfo typeInfo : provider.arguments().argumentTypes()) {
            Argument<?> argument = typeInfo.type().getConstructor().newInstance();
            providerArgumentMap.computeIfAbsent(provider, (e) -> new ArrayList<>());
            providerArgumentMap.get(provider).add(argument);
            Collection<ModernElement<?, Canvas>> elements = (Collection<ModernElement<?, Canvas>>) argument.fields(Vec2i.of(0, y + 11));
            for (ModernElement<?, Canvas> element : elements) {
                layout.addUnchecked(element);
                LabelElement labelElement = Elements.label(Identifier.of("label_" + element.identifier().key()), Text.vanilla(typeInfo.displayName())).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(2, y + 1)));
                layout.addUnchecked(labelElement);
                Vec2i size = element.render(null, active.loop().context()).size();
                y += 11 + size.y();
            }
        }
        this.provider = provider;
    }

    public void removeElementSetup() {
        Identifier layoutId = Identifier.of("new_element/wizard");
        Layout<?> layout = (Layout<?>) active.element(layoutId);
        for (ModernElement<?, Canvas> child : List.copyOf(layout.children())) {
            if (child.identifier().key().string().contains("new_element")) continue;
            layout.remove(child.identifier());
        }
        layout.visibility(Visibility.hidden());
    }

}
