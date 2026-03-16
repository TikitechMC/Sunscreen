package me.combimagnetron.sunscreen.neo.theme.color;

import me.combimagnetron.sunscreen.neo.graphic.color.Color;

public interface ColorSchemes {

    ColorScheme BASIC_DARK = ColorScheme.basicDark(Color.hex("#151D28"), Color.hex("#090A14"), Color.hex("#10141F"));

    ColorScheme EDITOR = ColorScheme.scheme(
            ColorScheme.ColorMode.DARK,
            Color.hex("#858585"),
            Color.hex("#0D0D0D"),
            Color.hex("#1B1B1B")
    )
            .color(ColorKey.colorKey("#highlight"), Color.hex("#FFFFFF"))
            .color(ColorKey.colorKey("#widget_top"), Color.hex("#858585"))
            .color(ColorKey.colorKey("#widget_middle"), Color.hex("#3D3D3D"))
            .color(ColorKey.colorKey("#widget_bottom"), Color.hex("#272727"))
            .color(ColorKey.colorKey("#label_main"), Color.hex("#FFFFFF"))
            .color(ColorKey.colorKey("#label_secondary"), Color.hex("#858585"));

}
