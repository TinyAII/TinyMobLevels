package nl.tinyaii.tinymoblevels.command;

import nl.tinyaii.tinymoblevels.TinyMobLevelsPlugin;
import nl.tinyaii.tinymoblevels.data.MobLevel;
import nl.tinyaii.tinymoblevels.util.Messages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * 怪物命令（双语）：
 *  /怪物 等级                   → 查当前等级配置
 *  /怪物 召唤 <等级> [数量]       → (管理) 召唤指定等级怪（你面前）
 *  /怪物 召唤 <类型> <等级> [数量] → (管理) 召唤指定类型+等级
 *  /怪物 重载                    → (管理) 重载配置
 */
public class MobCommand implements CommandExecutor {

    private final TinyMobLevelsPlugin plugin;

    public MobCommand(TinyMobLevelsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isAdmin(CommandSender s) {
        if (s.hasPermission("moblevels.admin") || s.isOp()) return true;
        s.sendMessage(Messages.color(plugin.getConfig().getString("messages.no-permission", "&c你没有权限这么做。")));
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("仅玩家可用。"); return true; }
        Player p = (Player) sender;
        if (args.length == 0) { help(p); return true; }
        switch (args[0]) {
            case "等级": case "level": case "info":
                showConfig(p);
                return true;
            case "召唤": case "spawn":
                if (!isAdmin(p)) return true;
                return spawn(p, args);
            case "重载": case "reload":
                if (!isAdmin(p)) return true;
                plugin.reloadConfig();
                plugin.getLevelConfig().reload();
                p.sendMessage(Messages.color(plugin.getConfig().getString("messages.reloaded", "&a配置已重载。")));
                return true;
            default:
                help(p);
                return true;
        }
    }

    private void help(Player p) {
        p.sendMessage(Messages.color("&e===== 怪物等级帮助 ====="));
        p.sendMessage(Messages.color("&7/怪物 等级 &e- &7查看等级配置"));
        p.sendMessage(Messages.color("&7/怪物 召唤 <等级> [数量] &e- &7(管理)召唤面前随机怪"));
        p.sendMessage(Messages.color("&7/怪物 召唤 <类型> <等级> [数量] &e- &7(管理)召唤指定怪"));
        p.sendMessage(Messages.color("&7/怪物 重载 &e- &7(管理)重载配置"));
    }

    private void showConfig(Player p) {
        String source = plugin.getConfig().getString("settings.level-source", "random");
        p.sendMessage(Messages.color("&e===== 怪物等级配置 ====="));
        p.sendMessage(Messages.color("&7等级来源: &e" + ("random".equalsIgnoreCase(source) ? "随机" : "离出生点距离")));
        p.sendMessage(Messages.color("&7等级范围: &e1-10 级"));
        p.sendMessage(Messages.color("&7头顶显示: &e" + (plugin.getConfig().getBoolean("settings.level-display", true) ? "开" : "关")));
        p.sendMessage(Messages.color("&7金币掉落: &e" + (plugin.getConfig().getBoolean("economy.enabled", true) ? "开" : "关")
                + (plugin.getEcoBridge().isAvailable() ? " &a(经济已连接)" : " &c(未连接经济插件)")));
        p.sendMessage(Messages.color("&7经验随等级: &e" + (plugin.getConfig().getBoolean("exp.enabled", false) ? "开" : "关")));
    }

    private boolean spawn(Player p, String[] args) {
        if (args.length < 2) { p.sendMessage(Messages.color("&c用法: /怪物 召唤 <等级> [数量] | /怪物 召唤 <类型> <等级> [数量]")); return true; }
        Location loc = p.getLocation();
        try {
            if (args.length >= 3 && isEntityType(args[1])) {
                // 指定类型: /怪物 召唤 <类型> <等级> [数量]
                EntityType type = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
                int level = Integer.parseInt(args[2]);
                int count = args.length >= 4 ? Integer.parseInt(args[3]) : 1;
                return spawnTyped(p, type, level, count, loc);
            }
            // 默认: 生成等级怪（随机敌对类型，或当前附近的敌对）
            int level = Integer.parseInt(args[1]);
            int count = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
            EntityType type = defaultType(level);
            return spawnTyped(p, type, level, count, loc);
        } catch (NumberFormatException e) {
            p.sendMessage(Messages.color("&c等级/数量必须是整数。"));
            return true;
        }
    }

    private boolean isEntityType(String s) {
        try {
            EntityType.valueOf(s.toUpperCase(Locale.ROOT));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private EntityType defaultType(int level) {
        // 低级偏僵尸/骷髅，高级混合
        EntityType[] types = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.CREEPER, EntityType.WITCH, EntityType.ENDERMAN};
        return types[Math.min(level - 1, types.length - 1)];
    }

    private boolean spawnTyped(Player p, EntityType type, int level, int count, Location loc) {
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            LivingEntity ent = (LivingEntity) loc.getWorld().spawnEntity(loc, type);
            if (ent != null) {
                plugin.getMobLevelListener().applyLevel(ent, level);
                spawned++;
            }
        }
        p.sendMessage(Messages.color(plugin.getConfig().getString(
                "messages.spawn-ok", "&a已召唤 {count} 只 {level} 级 {mob}")
                .replace("{count}", String.valueOf(spawned))
                .replace("{level}", String.valueOf(level))
                .replace("{mob}", type.name())));
        return true;
    }
}
