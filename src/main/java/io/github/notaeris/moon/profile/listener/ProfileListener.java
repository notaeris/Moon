package io.github.notaeris.moon.profile.listener;

import io.github.notaeris.moon.MoonPlugin;
import io.github.notaeris.moon.profile.Profile;
import io.github.notaeris.moon.profile.ProfileElement;
import io.github.notaeris.moon.punishment.Punishment;
import io.github.notaeris.moon.punishment.PunishmentElement;
import io.github.notaeris.moon.punishment.type.PunishmentType;
import io.github.notaeris.moon.tag.Tag;
import io.github.notaeris.moon.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.Objects;

public class ProfileListener implements Listener {

    private final MoonPlugin plugin = MoonPlugin.getPlugin(MoonPlugin.class);

    private final ProfileElement profileElement = this.plugin
            .getMoonBootstrap().getElementHandler()
            .findElement(ProfileElement.class);

    private final PunishmentElement punishmentElement = this.plugin
            .getMoonBootstrap().getElementHandler()
            .findElement(PunishmentElement.class);

    @Deprecated
    @EventHandler
    public void onPreJoin(PlayerPreLoginEvent event) {
        Profile.getProfileMap().computeIfAbsent(event.getUniqueId(), Profile::new);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        try {
            Punishment punishment = this.punishmentElement.findPunishment(player.getUniqueId());

            if (punishment.findPunishmentType(PunishmentType.BAN).isActive()) {
                player.kickPlayer(punishment.getReason());
            }
        } catch (NullPointerException ignored) {
        }

        Profile profile = this.profileElement.findProfile(player.getUniqueId());
        PermissionAttachment permissionAttachment = player.addAttachment(this.plugin);

        for (String permission : profile.getGrant().getPermissions()) {
            permissionAttachment.setPermission(permission, true);
        }

        player.recalculatePermissions();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileElement.findProfile(player.getUniqueId());

        try {
            String tagHandler = Objects.equals(profile.getTagGrant().getPrefix(), "") ? "" : profile.getTagGrant().getPrefix();

            event.setFormat(CC.translate(tagHandler + " &f" + profile.getGrant().getPrefix()
                    + player.getName() + "&7: &f" + event.getMessage()));
        } catch (IndexOutOfBoundsException exception) {
            event.setFormat(CC.translate(profile.getGrant().getPrefix()
                    + player.getName() + "&7: &f" + event.getMessage()));
        }
    }
}
