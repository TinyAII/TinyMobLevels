package nl.tinyaii.tinymoblevels;

import nl.tinyaii.tinymoblevels.data.LevelConfig;
import nl.tinyaii.tinymoblevels.data.MobLevel;
import nl.tinyaii.tinymoblevels.util.EcoBridge;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class TinyMobLevelsPlugin extends JavaPlugin {

    private LevelConfig levelConfig;
    private MobLevel mobLevel;
    private EcoBridge ecoBridge;
    private nl.tinyaii.tinymoblevels.listener.MobLevelListener mobLevelListener;

    @Override
    public void onEnable() {
        // TinyAII 品牌横幅 —— 必须在所有初始化逻辑之前输出（与 AutoBackup 完全一致）
        getLogger().info(" _____ _                _    ___ ___");
        getLogger().info("|_   _(_)_ __  _   _   / \\  |_ _|_ _|");
        getLogger().info("  | | | | '_ \\| | | | / _ \\  | | | |");
        getLogger().info("  | | | | | | | |_| |/ ___ \\ | | | |");
        getLogger().info("  |_| |_|_| |_|\\__, /_/   \\_\\___|___|");
        getLogger().info("               |___/");
        getLogger().info("TinyMobLevels 怪物等级系统 v" + getDescription().getVersion() + " - TinyAII 出品");

        saveDefaultConfig();
        levelConfig = new LevelConfig(this);
        mobLevel = new MobLevel(new NamespacedKey(this, "mob_level"));
        ecoBridge = new EcoBridge(this);
        mobLevelListener = new nl.tinyaii.tinymoblevels.listener.MobLevelListener(this);

        getServer().getPluginManager().registerEvents(mobLevelListener, this);
        getCommand("怪物").setExecutor(new nl.tinyaii.tinymoblevels.command.MobCommand(this));

        // 定时兜底刷新：每 10 tick（0.5秒）刷新所有分级怪头顶血量+视线隐藏（处理持续掉血/玩家移动后视线变化）
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.World world : getServer().getWorlds()) {
                for (org.bukkit.entity.LivingEntity ent : world.getLivingEntities()) {
                    int lv = mobLevel.getLevel(ent);
                    if (lv < 1) continue;
                    mobLevelListener.refreshDisplay(ent, lv);
                }
            }
        }, 40L, 10L);

        getLogger().info("怪物等级系统已启用。等级来源=" + getConfig().getString("settings.level-source", "random")
                + " 经济=" + (ecoBridge.isAvailable() ? "已连接" : "未连接"));
    }

    @Override
    public void onDisable() {
    }

    public LevelConfig getLevelConfig() { return levelConfig; }
    public MobLevel getMobLevel() { return mobLevel; }
    public EcoBridge getEcoBridge() { return ecoBridge; }
    public nl.tinyaii.tinymoblevels.listener.MobLevelListener getMobLevelListener() { return mobLevelListener; }
}
