package net.rayfall.eyesniper2.skrayfall.holograms;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.DocumentationId;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import net.rayfall.eyesniper2.skrayfall.Core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Create Timed Hologram")
@Description({"[NOTE] Client-side holograms require ProtocolLib",
        "Set hologram by:",
        "* Text",
        "* Lines",
        "* Floating Item",
        "* Timespan",
        "* Location",
        "* Clientside",
        "Create floating text at a location"})
@RequiredPlugins({"Holographic Displays", "Protocollib"})
@DocumentationId("EffCreateTimedHolograms")
public class EffTimedClientSideHolo extends Effect {

    // display hologram %string% at %location% to %player% for %timespan%

    private Expression<String> text;
    private Expression<Timespan> time;
    private Expression<Location> loc;
    private Expression<Player> player;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exp, int arg1, Kleenean arg2, ParseResult arg3) {
        text = (Expression<String>) exp[0];
        loc = (Expression<Location>) exp[1];
        player = (Expression<Player>) exp[2];
        time = (Expression<Timespan>) exp[3];
        return true;
    }

    @Override
    public String toString(@Nullable Event arg0, boolean arg1) {
        return null;
    }

    @Override
    protected void execute(Event evt) {
        final Hologram hologram = HologramsAPI.createHologram(Core.plugin, loc.getSingle(evt));
        VisibilityManager visibilityManager = hologram.getVisibilityManager();
        visibilityManager.showTo(player.getSingle(evt));
        visibilityManager.setVisibleByDefault(false);
        String core = text.getSingle(evt).replace("\"", "");
        while (core.indexOf(";") != -1) {
            String line = core.substring(0, core.indexOf(";"));
            core = core.substring(core.indexOf(";") + 1);
            if (line.startsWith("ItemStack:")) {
                line = line.substring(line.indexOf(":") + 1);
                int meta = 0;
                if (line.contains(":")) {
                    try {
                        meta = Integer.parseInt(line.substring(line.indexOf(":") + 1));
                    } catch (NumberFormatException exception) {
                        Skript.error("Meta data could not be parsed correctly!");
                        continue;
                    }

                    line = line.substring(0, line.indexOf(":"));
                }
                ItemStack stack = new ItemStack(Material.AIR, 1);
                try {
                    Material mat = Material.valueOf(line.toUpperCase().replace(" ", "_"));
                    stack = new ItemStack(mat, 1);
                    if (meta != 0) {
                        stack = new ItemStack(mat, 1, (byte) meta);
                    }
                } catch (IllegalArgumentException exception) {
                    Skript.error("A item under that name does not exsist!");
                    continue;
                }

                hologram.appendItemLine(stack);
            } else {
                hologram.appendTextLine(line);
            }
        }
        hologram.appendTextLine(core);

        Bukkit.getScheduler().runTaskLater(Core.plugin, new Runnable() {
            @Override
            public void run() {
                hologram.delete();
            }
        }, time.getSingle(evt).getTicks_i());
    }
}
